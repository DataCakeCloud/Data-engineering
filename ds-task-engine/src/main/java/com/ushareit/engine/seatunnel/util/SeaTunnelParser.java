package com.ushareit.engine.seatunnel.util;

import com.alibaba.fastjson.JSON;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigRenderOptions;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.engine.Context;
import com.ushareit.engine.constant.SourceEnum;
import com.ushareit.engine.param.Table;
import com.ushareit.engine.seatunnel.job.Env;
import com.ushareit.engine.seatunnel.job.JobInfo;
import com.ushareit.engine.seatunnel.adapter.sink.*;
import com.ushareit.engine.seatunnel.adapter.source.*;
import com.ushareit.engine.seatunnel.adapter.transform.Transform;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import java.io.*;
import java.text.MessageFormat;
import java.util.Map;

@Slf4j
public class SeaTunnelParser {
    private static final String SEATUNNEL_LOCAL_SHELL = "execute.sh seatunnel {0} -e local";

    private static final String FILE_CONFIG_PATH = "/tmp/seatunnel_{0}_{1}.conf";

    // 定义渲染选项，去除最外层大括号和行尾逗号
    private static ConfigRenderOptions renderOptions = ConfigRenderOptions.defaults()
            .setOriginComments(false)
            .setComments(false)
            .setJson(false)
            .setOriginComments(false)
            .setFormatted(true);

    public static String getJobInfo(Context context) {
        context.prepare();
        JobInfo jobInfo = new JobInfo();
        //env解析
        jobInfo.setEnv(new Env());
        //source解析
        if (StringUtils.isNotEmpty(context.getSourceType()) && StringUtils.isNotEmpty(context.getSourceConfigStr())) {
            Map source = null;
            String sourceType = context.getSourceType().toLowerCase();
            String uSourceType = sourceType.substring(0, 1).toUpperCase() + sourceType.substring(1);
            switch (SourceEnum.valueOf(uSourceType)) {
                case Mysql:
                    source = new MysqlSourceAdapter(context.getRuntimeConfig(), context.getSourceConfigJson()).adapter();
                    break;
                case Starrocks:
                case Doris:
                    source = new DorisSourceAdapter(context.getRuntimeConfig(), context.getSourceConfigJson()).adapter();
                    break;
                case Clickhouse:
                    source = new ClickhouseSourceAdapter(context.getRuntimeConfig(), context.getSourceConfigJson()).adapter();
                    break;
                case Sqlserver:
                    source = new SqlServerSourceAdapter(context.getRuntimeConfig(), context.getSourceConfigJson()).adapter();
                    break;
                case Oracle:
                    source = new OracleSourceAdapter(context.getRuntimeConfig(), context.getSourceConfigJson()).adapter();
                    break;
                case Postgres:
                case Hologres:
                    source = new PostgreSourceAdapter(context.getRuntimeConfig(), context.getSourceConfigJson()).adapter();
                    break;
                case Iceberg:
                    source = new IcebergSourceAdapter(context.getRuntimeConfig(), context.getSourceConfigJson()).adapter();
                    break;
                case Mongodb:
                    source = new MongoDbAdapter(context.getRuntimeConfig(),context.getSourceConfigJson()).adapter();
                    break;
                case Hana:
                    source = new HanaSourceAdapter(context.getRuntimeConfig(),context.getSourceConfigJson()).adapter();
                    break;
                case Hdfs:
                case Oss:
                case S3:
                case Ks3:
                    source = new HdfsSourceAdapter(context.getRuntimeConfig(),context.getSourceConfigJson(),sourceType).adapter();
                    break;
                case Kafka:
                    source = new KafkaSourceAdapter(context.getRuntimeConfig(),context.getSourceConfigJson(),sourceType).adapter();
                    break;
            }
            jobInfo.setSource(source);

        }
        if(!"oss".equalsIgnoreCase(context.getSourceType())&&!"hdfs".equalsIgnoreCase(context.getSourceType())
                && !"s3".equalsIgnoreCase(context.getSourceType())){
            //transform解析
            Transform transform = new Transform(context);
            jobInfo.setTransform(transform.getTransformSql());
        }

        //sink解析
        if (StringUtils.isNotEmpty(context.getSinkType())) {
            Map sink = ConsoleSinkAdapter.adapter(context.getRuntimeConfig(), context.getSinkConfigJson());
            if (StringUtils.isNotEmpty(context.getSinkConfigStr())) {
                String deType = context.getSinkType().toLowerCase();
                String uDeType = deType.substring(0, 1).toUpperCase() + deType.substring(1);
                switch (SourceEnum.valueOf(uDeType)) {
                    case Mysql:
                        sink = new MysqlSinkAdapter(context.getRuntimeConfig(), context.getSinkConfigJson()).adapter();
                        break;
                    case Doris:
                        sink = new DorisSinkAdapter(context.getRuntimeConfig(), context.getSinkConfigJson()).adapter();
                        break;
                    case Clickhouse:
                        sink = new ClickhouseSinkAdapter(context.getRuntimeConfig(), context.getSinkConfigJson()).adapter();
                        break;
                    case Sqlserver:
                        sink = new SqlServerSinkAdapter(context.getRuntimeConfig(), context.getSinkConfigJson()).adapter();
                        break;
                    case Oracle:
                        sink = new OracleSinkAdapter(context.getRuntimeConfig(), context.getSinkConfigJson()).adapter();
                        break;
                    case Iceberg:
                        sink = new IcebergSinkAdapter(context.getRuntimeConfig(), context.getSinkConfigJson()).adapter();
                }
            }
            jobInfo.setSink(sink);
        }


        Config config = ConfigFactory.parseString(JSON.toJSONString(jobInfo));
        // 将 Config 对象转换为 HOCON 字符串
        return config.root().render(renderOptions);
    }

    public static String getJobShell(Context context) throws IOException {
        String jobInfo = getJobInfo(context);
        return getJobShell(context, jobInfo, context.getExecuteMode());
    }


    public static String getJobShell(Context context, String jobInfo, String executeMode) throws IOException {
        /*String executorShell;
        log.info(" jobconfig is :" + jobInfo);
        String base64Info = new String(Base64.getEncoder().encode(URLEncoder.encode(jobInfo, "UTF-8").replaceAll("\\+", "%20").getBytes()));
        String encodeFlagStr = encodeFlag(base64Info);
        log.info(" encrypt  jobconfig is :" + encodeFlagStr);
        if (local.name().equals(executeMode)) {
            executorShell = MessageFormat.format(SEATUNNEL_LOCAL_SHELL, encodeFlagStr);
        } else {
            String[] args = new String[]{"-e", "cluster", "--objectStoragePath", context.getJarPath(), "--configStr", encodeFlagStr};
            SparkStarterForShareit instance = SparkStarterForShareit.getInstance(args);
            List<String> command = instance.buildCommands();
            executorShell = String.join(" ", command);
        }
        log.info(" result   exectorShell  is :" + executorShell);
        String replace = executorShell.replace("\"", "");
        return replace;*/
        return null;
    }

    private static String createFile(String content, Context context) {
        Table table = context.getRuntimeConfig().getCatalog().getTables().get(0);
        String filePath = MessageFormat.format(FILE_CONFIG_PATH, table.getSourceTable(), table.getTargetTable());
        log.info("filePath is :" + filePath);
        try {
            if (new File(filePath).exists()) {
                FileUtils.deleteDirectory(new File(filePath));
            }
            BufferedWriter out = new BufferedWriter(new FileWriter(filePath));
            out.write(content);
            out.close();
        } catch (Exception e) {
            throw new ServiceException(BaseResponseCodeEnum.SEATUNNEL_WRITE_FILE_FAIL);
        }
        return filePath;
    }

    public static String encodeFlag(String str) {
        return "/*datacakebianma*/" + str + "/*datacakebianma*/";
    }

}
