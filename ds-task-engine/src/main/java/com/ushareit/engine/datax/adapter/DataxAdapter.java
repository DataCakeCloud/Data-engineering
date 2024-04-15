package com.ushareit.engine.datax.adapter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ushareit.dstask.utils.GsonUtil;
import com.ushareit.engine.Context;
import com.ushareit.engine.constant.SourceEnum;
import com.ushareit.engine.datax.*;
import com.ushareit.engine.datax.doris.DorisWriter;
import com.ushareit.engine.datax.file.FileReader;
import com.ushareit.engine.datax.hana.HanaReader;
import com.ushareit.engine.datax.hdfs.HdfsReader;
import com.ushareit.engine.datax.hdfs.HdfsWriter;
import com.ushareit.engine.datax.mongodb.MongoDBReader;
import com.ushareit.engine.datax.mysql.MysqlReader;
import com.ushareit.engine.datax.mysql.MysqlWriter;
import com.ushareit.engine.datax.oracle.OracleReader;
import com.ushareit.engine.datax.oracle.OracleWriter;
import com.ushareit.engine.datax.postgre.PostgreReader;
import com.ushareit.engine.datax.sqlserver.SqlServerReader;
import com.ushareit.engine.datax.sqlserver.SqlServerWriter;
import com.ushareit.engine.param.Column;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import com.ushareit.engine.datax.Job;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class DataxAdapter {
    private static final String DATAX_SHELL = "execute.sh datax  {0}";

    public static String getJobShell(String jobInfo, Integer bandwidth) throws IOException {
        JSONObject job = JSON.parseObject(jobInfo);
        job.put("core", getBandWith(bandwidth));
        Job jobObject = JSON.parseObject(job.getString("job"), Job.class);
        Map<String, Object> setting = jobObject.getSetting();
        String jvm = (String) setting.getOrDefault("jvm", "-Xms1G -Xmx1G");
        jobInfo=new String(Base64.getEncoder().encode(URLEncoder.encode(job.toJSONString(), "UTF-8").replaceAll("\\+", "%20").getBytes()));
        log.info("JobShell: " + job.toJSONString());
        jobInfo="/*zheshiyigebianmabiaoshi*/" + jobInfo + "/*zheshiyigebianmabiaoshi*/";
        String executorShell=MessageFormat.format(DATAX_SHELL,jobInfo) + "--jvm='"+jvm+"'";
        return executorShell;
    }

    public static String getJobInfo(Context context) {
        context.prepare();
        JobInfo jobInfo = new JobInfo();
        Map<String,Object> setting = JSON.parseObject(context.getRuntimeConfig().getAdvancedParameters().getBatchParams(), Map.class);
        if (setting==null){
            HashMap<String, Object> settingMap = new HashMap<>();
            HashMap<String, Object> speed = new HashMap<>();
            speed.put("channel",2);
            settingMap.put("speed",speed);
            HashMap<String, Object> errorLimit = new HashMap<>();
            errorLimit.put("record",0);
            settingMap.put("errorLimit",errorLimit);
            jobInfo.getJob().setSetting(settingMap);
        }else{
            if (!setting.containsKey("errorLimit")){
                HashMap<String, Object> errorLimit = new HashMap<>();
                errorLimit.put("record",0);
                setting.put("errorLimit",errorLimit);
            }
            jobInfo.getJob().setSetting(setting);
        }
        //source解析
        if (StringUtils.isNotEmpty(context.getSourceType()) && StringUtils.isNotEmpty(context.getSourceConfigStr())) {
            Reader reader = null;
            String sourceType = context.getSourceType().toLowerCase();
            String uSourceType = sourceType.substring(0, 1).toUpperCase() + sourceType.substring(1);
            switch (SourceEnum.valueOf(uSourceType)) {
                case Mysql:
                    reader = new MysqlReader(context);
                    break;
                case Oracle:
                    reader = new OracleReader(context);
                    break;
                case Sqlserver:
                    reader = new SqlServerReader(context);
                    break;
                case Postgres:
                    reader = new PostgreReader(context);
                    break;
                case Hdfs:
                case Iceberg:
                    reader=new HdfsReader(context);
                    break;
                case Mongodb:
                    reader=new MongoDBReader(context);
                    break;
                case Hana:
                    reader = new HanaReader(context);
                    break;
                case S3:
                case Ks3:
                case Oss:
                    reader = new FileReader(context);
                    break;
            }
            jobInfo.getJob().getContent().get(0).setReader(reader);
        }
        if (StringUtils.isNotEmpty(context.getSinkType()) && StringUtils.isNotEmpty(context.getSinkConfigStr())) {
            Writer writer = null;
            String sinkType = context.getSinkType().toLowerCase();
            String uSinkType = sinkType.substring(0, 1).toUpperCase() + sinkType.substring(1);
            switch (SourceEnum.valueOf(uSinkType)) {
                case Mysql:
                    writer = new MysqlWriter(context);
                    break;
                case Oracle:
                    writer = new OracleWriter(context);
                    break;
                case Sqlserver:
                    writer = new SqlServerWriter(context);
                    break;
                case Doris:
                    writer = new DorisWriter(context);
                    break;
                case Iceberg:
                    writer = new HdfsWriter(context);
            }
            jobInfo.getJob().getContent().get(0).setWriter(writer);
        }
        List<Column> columns = context.getRuntimeConfig().getCatalog().getTables().get(0).getColumns();
        AtomicInteger columnIndex = new AtomicInteger();
        List<Content.Transformer> transformers = new ArrayList<>();
        columns.stream().forEach(column -> {
            if (column.getFuncs() != null && column.getFuncs().size()>0){
                Content.Transformer transformer = new Content.Transformer();
                transformer.setName(column.getFuncs().get(0).getFuncName());
                Content.Parameter parameter = new Content.Parameter();
                parameter.setColumnIndex(columnIndex.get());
                parameter.setParas(column.getFuncs().get(0).getFuncParams());
                transformer.setParameter(parameter);
                transformers.add(transformer);
            }
            columnIndex.getAndIncrement();
        });
        jobInfo.getJob().getContent().get(0).setTransformer(transformers);

        return GsonUtil.toJson(jobInfo,false);
    }

    private static HashMap<String, Object> getBandWith(Integer bandwidth) {
        Map<String, Object> core = new HashMap<>();
        Map<String, Object> transport = new HashMap<>();
        Map<String, Object> channel = new HashMap<>();
        Map<String, Object> speed = new HashMap<>();

        speed.put("byte", 1048576 * bandwidth);
        channel.put("speed", speed);
        transport.put("channel", channel);
        core.put("transport", transport);
        return (HashMap<String, Object>) core;
    }

}
