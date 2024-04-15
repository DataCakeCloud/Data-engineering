package com.ushareit.dstask.web.utils;

import com.amazonaws.util.Md5Utils;
import com.ushareit.dstask.bean.CloudResouce;
import com.ushareit.dstask.bean.UserGroup;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.service.impl.TaskServiceImpl;
import com.ushareit.dstask.third.schedule.SchedulerServiceImpl;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import com.ushareit.dstask.utils.SpringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.hive.jdbc.HiveConnection;
import org.apache.hive.jdbc.HiveStatement;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * sparksql parse
 */
@Slf4j
public class SparkSqlParseUtil {

    @Resource
    public SchedulerServiceImpl schedulerService;

    @Value("${gateway.key}")
    private String gatewayKey;

    public static String parse(TaskServiceImpl taskServiceimpl, String sql, String provider, String region) throws Exception {
        try {
            String renderSql = beforeExec(sql);
            ArrayList<String> physicalPlanArr = new ArrayList<>();
            // 包含多段sql的情况
            String[] renderSqlArr = StringUtils.strip(renderSql.trim(), ";").split(";");
            for (String splitSql : renderSqlArr) {
                physicalPlanArr.add(getPhysicalPlanErrorNew(taskServiceimpl, splitSql, region));
//                physicalPlanArr.add(getPhysicalPlanError(taskServiceimpl, splitSql, provider, region));
            }
            return StringUtils.join(physicalPlanArr, "\n\n=====================================多段sql-explain分界线=====================================\n\n");
        } catch (ServiceException e) {
            // render失败是一个结果信息,不属于接口错误,返回失败详情
            if (e.getCodeStr().equals(BaseResponseCodeEnum.SPARKSQL_JINJA_DATA_ERR.name())) {
                return e.getMessage() + "\n\n" + e.getData().toString();
            }
            throw  e;
        }
    }

    public static String beforeExec(String sql) throws Exception {
        // sparksql包含了jinja格式的日期参数,需要先做一次替换,调用调度层的render接口
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SchedulerServiceImpl schedulerService = SpringUtil.getBean(SchedulerServiceImpl.class);

        return schedulerService.render(sql,df.format(new Date()),"");
    }

    public static String getPhysicalPlanError(TaskServiceImpl taskServiceimpl, String sql, String provider, String region) {
        try {
            if (provider == null || provider.isEmpty()) {
                provider = InfTraceContextHolder.get().getUserName();
            }
            String transformedRegion = taskServiceimpl.getTransformedRegion(region);
            String physicalPlan =taskServiceimpl.olapGateWayUtil.explainBySpark(sql, provider, transformedRegion);
            log.info(String.format("get sql physical plan : %s", physicalPlan));
            if (physicalPlan.contains("AnalysisException")){
                return "校验失败:\n"+physicalPlan;
            }
            return "校验成功！！";
        }catch (SQLException e){
            return  "校验失败:\n"+e.getMessage();
        }catch (Exception e){
            throw e;
        }

    }

    /**
     * new gateway sql check
     */
    public static String getPhysicalPlanErrorNew(TaskServiceImpl taskServiceimpl, String sql, String region) {
        CloudResouce.DataResource cloudResource = DataCakeConfigUtil.getCloudResourcesService().getCloudResource(region);
        String defaultBb = "default";
        String user = InfTraceContextHolder.get().getUserName();
        String groupId = InfTraceContextHolder.get().getGroupId();
        String tenantName = InfTraceContextHolder.get().getTenantName();
        UserGroup byId = taskServiceimpl.userGroupService.getById(Integer.parseInt(groupId));

        if(byId==null){
            throw new ServiceException(BaseResponseCodeEnum.USER_GROUP_DEATIL_IS_NULL);
        }

        if (StringUtils.isNotEmpty(byId.getDefaultHiveDbName())) {
            defaultBb = byId.getDefaultHiveDbName();
        }
        String gatewayHost = DataCakeConfigUtil.getDataCakeConfig().getGatewayHost();
        try {
            long curTime = System.currentTimeMillis();
            String signStr = String.join(":", InfTraceContextHolder.get().getCurrentGroup(), new SparkSqlParseUtil().gatewayKey, curTime + "");
            String md5 = getMd5(signStr);
            String url = "%s/%s;" +
                    "auth=noSasl;" +
                    "user=%s?kyuubi.engine.type=JDBC;" +
                    "kyuubi.session.cluster.tags=provider:%s,region:%s;" +
                    "kyuubi.engine.jdbc.connection.provider=HiveConnectionProvider;"+
                    "kyuubi.session.group=%s;"+
                    "kyuubi.session.groupId=%s;"+
                    "kyuubi.session.tenant=%s;" +
                    "kyuubi.session.sign=%s;" +
                    "kyuubi.session.openTime=%s;";
            String postUrl = String.format(url, gatewayHost, defaultBb, user, cloudResource.getProvider(), cloudResource.getRegion(), InfTraceContextHolder.get().getCurrentGroup(), byId.getUuid(), tenantName, md5, curTime);
            log.info("postUrl is :" + postUrl);
            HiveConnection conn = (HiveConnection) DriverManager.getConnection(postUrl);
            HiveStatement stmt = (HiveStatement) conn.createStatement();
            boolean success = stmt.execute(sql);
            ResultSet rs = stmt.getResultSet();
            rs.close();
            stmt.close();
            conn.close();
            return "校验成功！！";
        } catch (SQLException e) {
            return "校验失败:\n" + e.getMessage();
        } catch (Exception e) {
            throw e;
        }
    }
    /*
      用正则的方式取写入的表和使用到的表
    */
    private static final String outputsRegex = "(?i)alter\\s+table\\s+([^\\s\\(\\);/-]+)|insert\\s+into\\s+([^\\s\\(\\);/-]+)|msck\\s+repair\\s+table\\s+([^\\s\\(\\);/-]+)|insert\\s+overwrite\\s+table\\s+([^\\s\\(\\);/-]+)";
    private static final String inputsRegex = "\\b(?:from|join)\\b\\s+([\\w.]+)";

    public static List<String> getOutputs(String sql) {
        List<String> outputs = new ArrayList<>();
        Pattern parttern = Pattern.compile(outputsRegex);
        Matcher m = parttern.matcher(sql);
        while (m.find()) {
            if (m.group(1) != null) {
                outputs.add(m.group(1));
                continue;
            }
            if (m.group(2) != null) {
                outputs.add(m.group(2));
                continue;
            }
            if (m.group(3) != null) {
                outputs.add(m.group(3));
                continue;
            }
            if (m.group(4) != null) {
                outputs.add(m.group(4));
                continue;
            }
        }

        return outputs.stream().distinct().collect(Collectors.toList());
    }

    public static List<String> getInputs(String sql) {
        List<String> inputs = new ArrayList<>();
        Pattern pattern = Pattern.compile(inputsRegex, Pattern.CASE_INSENSITIVE);
        Matcher m = pattern.matcher(sql);
        while (m.find()) {
            if (m.group(1) != null) {
                inputs.add(m.group(1));
            }
        }
        //过滤掉不是库.表形式的, 这种是用户自建的视图
        return inputs.stream().filter(x-> x.contains(".")).distinct().collect(Collectors.toList());
    }

    public static String filterSqlComments(String sql) {
        StringBuilder result = new StringBuilder();
        boolean withinQuotes = false;
        boolean withinBlockComment = false;
        boolean withinLineComment = false;

        for (int i = 0; i < sql.length(); i++) {
            if (!withinQuotes && !withinBlockComment && !withinLineComment) {
                if (sql.charAt(i) == '\'') {
                    withinQuotes = true;
                } else if (sql.charAt(i) == '/' && i < sql.length() - 1 && sql.charAt(i + 1) == '*') {
                    withinBlockComment = true;
                } else if (sql.charAt(i) == '-' && i < sql.length() - 1 && sql.charAt(i + 1) == '-') {
                    withinLineComment = true;
                }
            } else if (withinQuotes && sql.charAt(i) == '\'') {
                withinQuotes = false;
            } else if (withinBlockComment && sql.charAt(i) == '*' && i < sql.length() - 1 && sql.charAt(i + 1) == '/') {
                withinBlockComment = false;
                i += 2; // Skip the '/' character
            } else if (withinLineComment && (sql.charAt(i) == '\n' || sql.charAt(i) == '\r')) {
                withinLineComment = false;
            }

            if (!withinBlockComment && !withinLineComment) {
                result.append(sql.charAt(i));
            }
        }
        return result.toString();
    }

    public static String sqlFormat(String sql) {
        return sql.replaceAll("\\s+", " ");
    }

    public static String appendConf2SubimitStr(String subimitStr,String appendConf) {
        String jarRegex = "\\s+-+[^\\s]+\\s+[^\\s-][^\\s]+\\s+([^-\\s][^\\s]+(jar|py))";
        Pattern jarPattern = Pattern.compile(jarRegex);
        Matcher m = jarPattern.matcher(subimitStr);
        if (m.find()){
            String matcherStr = m.group(0);
            String jarPath = m.group(1);
            String[] splits = StringUtils.splitByWholeSeparator(subimitStr,matcherStr);
            String conf =  splits[0] + "  " + StringUtils.splitByWholeSeparator(matcherStr,jarPath)[0];
            String args = splits[1];
            return conf + " " +appendConf + " " + jarPath + " " + args;
        } else {
            return subimitStr + " " + appendConf;
        }
    }

    public static boolean executeHiveSql(String sql,String region,String owner){
        String gatewayHost = DataCakeConfigUtil.getDataCakeConfig().getGatewayHost();
        CloudResouce.DataResource cloudResource = DataCakeConfigUtil.getCloudResourcesService().getCloudResource(region);

        long curTime = System.currentTimeMillis();
        String signStr = String.join(":", InfTraceContextHolder.get().getCurrentGroup(), new SparkSqlParseUtil().gatewayKey, curTime + "");
        String md5 = getMd5(signStr);
        String url = "%s/default;" +
                "auth=noSasl;" +
                "user=%s?kyuubi.engine.type=JDBC;" +
                "kyuubi.session.cluster.tags=provider:%s,region:%s;" +
                "kyuubi.engine.jdbc.connection.provider=HiveConnectionProvider;"+
                "kyuubi.session.group=%s;"+
                "kyuubi.session.groupId=%s;"+
                "kyuubi.session.tenant=%s;" +
                "kyuubi.session.sign=%s;" +
                "kyuubi.session.openTime=%s;";
        try {
            String postUrl = String.format(url, gatewayHost, owner,
                    cloudResource.getProvider(), cloudResource.getRegion(), InfTraceContextHolder.get().getCurrentGroup(),
                    InfTraceContextHolder.get().getUuid(), InfTraceContextHolder.get().getTenantName(), md5, curTime);
            log.info("postUrl is :" + postUrl);
            HiveConnection conn = (HiveConnection) DriverManager.getConnection(postUrl);
            HiveStatement stmt = (HiveStatement) conn.createStatement();
            boolean success = stmt.execute(sql);
            ResultSet rs = stmt.getResultSet();
            rs.close();
            stmt.close();
            conn.close();
            return true;
        } catch (SQLException e) {
            log.error("sql execute faild",e.getMessage());
            return false;
        } catch (Exception e) {
            throw e;
        }
    }

    public static String getMd5(String src) {
        byte[] digest;
        try {
            MessageDigest md5 = MessageDigest.getInstance("md5");
            digest = md5.digest(src.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
        return String.format("%032x", new BigInteger(1, digest));
    }
}
