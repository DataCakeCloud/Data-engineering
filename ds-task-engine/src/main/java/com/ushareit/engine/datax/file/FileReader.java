package com.ushareit.engine.datax.file;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ushareit.engine.Context;
import com.ushareit.engine.datax.Reader;
import com.ushareit.engine.param.ActorProvider;
import com.ushareit.engine.param.RuntimeConfig;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileReader extends Reader {
    public FileReader(Context context){
        context.prepare();
        this.setName("hdfsreader");
        FileReader.Parameter parameter=new FileReader.Parameter();
        this.setParameter(parameter);
        JSONObject sourceParam = JSON.parseObject(context.getRuntimeConfig().getSourceParam()).getJSONObject("provider");
        RuntimeConfig.TaskParam taskParam = context.getRuntimeConfig().getTaskParam();
        parameter.setPath(sourceParam.getString("path"));
        parameter.setFileType(sourceParam.getString("fileType"));
        Boolean isCheckFile = sourceParam.getBoolean("isCheckFile");
        if(true == isCheckFile){
            parameter.setIsCheckFile(true);
        }
        if(StringUtils.isNotBlank(sourceParam.getString("fieldDelimiter"))){
            parameter.setFieldDelimiter(sourceParam.getString("fieldDelimiter"));
        }
        if(StringUtils.isNotBlank(sourceParam.getString("isHeader"))){
            parameter.setSkipHeader(true);
        }
        List<Map<String,Object>> columns= new ArrayList<>();
        if (CollectionUtils.isNotEmpty(context.getRuntimeConfig().getCatalog().getTables().get(0).getColumns())){
            for (int i=0;i<context.getRuntimeConfig().getCatalog().getTables().get(0).getColumns().size();i++){
                HashMap<String, Object> column = new HashMap<>();
                column.put("index",i);
                column.put("type",typeTransform(context.getRuntimeConfig().getCatalog().getTables().get(0).getColumns().get(i).getData_type()));
                columns.add(column);
            }
        }
        parameter.setColumn(columns);
        ActorProvider actorProvider = JSON.parseObject(context.getSourceConfigJson().getString("provider")
                , ActorProvider.class);
        String bucket = actorProvider.getBucket();
        String awsAccessKeyId = actorProvider.getAwsAccessKeyId();
        String awsSecretAccessKey = actorProvider.getAwsSecretAccessKey();
        HashMap<String, Object> hadoopConfig = new HashMap<>();
        String defaultFs = null;
        if ("s3".equalsIgnoreCase(context.getSourceType())){
            defaultFs = String.format("%s%s","s3://",bucket);
            hadoopConfig.put("fs.s3.impl","org.apache.hadoop.fs.s3a.S3AFileSystem");
            hadoopConfig.put("fs.s3a.access.key", awsAccessKeyId);
            hadoopConfig.put("fs.s3a.secret.key", awsSecretAccessKey);
        } else if ("ks3".equalsIgnoreCase(context.getSourceType())) {
            defaultFs = String.format("%s%s","s3://",bucket);
            hadoopConfig.put("fs.ks3.impl","com.ksyun.kmr.hadoop.fs.ks3.Ks3FileSystem");
            hadoopConfig.put("fs.ks3.AccessKey",awsAccessKeyId);
            hadoopConfig.put("fs.ks3.AccessSecret",awsSecretAccessKey);
        } else if ("oss".equalsIgnoreCase(context.getSourceType())){
            defaultFs = String.format("%s%s","oss://",bucket);
            hadoopConfig.put("fs.oss.impl","org.apache.hadoop.fs.aliyun.oss.AliyunOSSFileSystem");
            hadoopConfig.put("fs.oss.accessKeyId",awsAccessKeyId);
            hadoopConfig.put("fs.oss.accessKeySecret",awsSecretAccessKey);
            hadoopConfig.put("fs.oss.endpoint","oss-cn-beijing.aliyuncs.com");
        }
        parameter.setDefaultFS(defaultFs);
        parameter.setHadoopConfig(hadoopConfig);
    }

    public String typeTransform(String type){
        switch (type.toLowerCase()){
            case "tinyint":
            case "smallint":
            case "int":
            case "bigint":
                return "Long";
            case "float":
            case "double":
                return "Double";
            case "boolean":
                return "Boolean";
            default:
                return "String";
        }
    }


    private FileReader.Parameter parameter;

    @Data
    public static class Parameter{
        private String fieldDelimiter;
        private Boolean skipHeader;
        private String defaultFS;
        private String fileType;
        private String path;
        private String encoding;
        private List<Map<String,Object>> column;
        private Map<String,Object> hadoopConfig;
        private Boolean haveKerberos;
        private String kerberosKeytabFilePath;
        private String kerberosPrincipal;
        private Boolean isCheckFile=false;

    }
}
