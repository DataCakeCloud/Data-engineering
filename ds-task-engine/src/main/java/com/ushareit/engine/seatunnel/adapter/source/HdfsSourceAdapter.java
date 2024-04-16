package com.ushareit.engine.seatunnel.adapter.source;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ushareit.engine.param.RuntimeConfig;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;


public class HdfsSourceAdapter extends AbstractSourceAdapter{
    private static final String SOURCE_TYPE = "HdfsFile";
    private String sourceType;

    public HdfsSourceAdapter(RuntimeConfig runtimeConfig, JSONObject sourceJson,String sourceType) {
        super(runtimeConfig, sourceJson);
        this.sourceType = sourceType;
    }

    @Override
    public Map<String, Object> adapter(RuntimeConfig runtimeConfig, JSONObject sourceJson) {
        Map<String, Object> result = new HashMap();
        JSONObject provider = sourceJson.getJSONObject("provider");
        if("hdfs".equalsIgnoreCase(sourceType)){
            System.setProperty("java.security.krb5.realm",provider.getString("realm"));
            System.setProperty("java.security.krb5.kdc", provider.getString("kdc"));
            String hadoopConf = provider.getString("hadoopConf");
            Map<String,String> config = JSON.parseObject(hadoopConf, Map.class);
            result.put("hadoopConf",config);
            result.put("fs.defaultFS",provider.getString("defaultFS"));
            result.put("kerberos_principal",provider.getString("principal"));
            result.put("kerberos_keytab_path",provider.getString("keytabPath"));
        }else if("s3".equalsIgnoreCase(sourceType)){
            HashMap<String, String> config = new HashMap<>();
            config.put("fs.s3a.access.key",provider.getString("aws_access_key_id"));
            config.put("fs.s3a.secret.key",provider.getString("aws_secret_access_key"));
            config.put("fs.s3.impl","org.apache.hadoop.fs.s3a.S3AFileSystem");
            result.put("hadoopConf",config);
            result.put("fs.defaultFS",String.format("s3://%s", provider.getString("bucket")));
        }else if("oss".equalsIgnoreCase(sourceType)){
            HashMap<String, String> config = new HashMap<>();
            config.put("fs.oss.accessKeyId",provider.getString("aws_access_key_id"));
            config.put("fs.oss.accessKeySecret",provider.getString("aws_secret_access_key"));
            config.put("fs.oss.impl","org.apache.hadoop.fs.aliyun.oss.AliyunOSSFileSystem");
            config.put("fs.oss.endpoint","oss-cn-beijing.aliyuncs.com");
            result.put("hadoopConf",config);
            result.put("fs.defaultFS",String.format("oss://%s", provider.getString("bucket")));
        }else if("ks3".equalsIgnoreCase(sourceType)){
            HashMap<String, String> config = new HashMap<>();
            config.put("fs.ks3.AccessKey",provider.getString("aws_access_key_id"));
            config.put("fs.ks3.AccessSecret",provider.getString("aws_secret_access_key"));
            config.put("alluxio.user.client.base.filesystem.class","alluxio.client.file.Ks3HdfsMetaFileSystem");
            config.put("fs.ks3.endpoint","ks3-cn-beijing.ksyuncs.com");
            result.put("hadoopConf",config);
            result.put("fs.defaultFS", String.format("ks3://%s", provider.getString("bucket")));
        }
        if(runtimeConfig.getSourceParam()!=null){
            JSONObject sourceParam = JSON.parseObject(runtimeConfig.getSourceParam()).getJSONObject("provider");
            if("csv".equalsIgnoreCase(sourceParam.getString("fileType"))) {
                result.put("path",sourceParam.getString("path"));
                result.put("delimiter", ",");
                result.put("file_format_type", "text");
            }else{
                if(StringUtils.isNotBlank(sourceParam.getString("fieldDelimiter"))){
                    result.put("delimiter",sourceParam.getString("fieldDelimiter"));
                }
                result.put("path",sourceParam.getString("path"));
                result.put("file_format_type",sourceParam.getString("fileType"));
            }

        }else{
            result.put("path",result.get("fs.defaultFS"));
        }
//        result.put("path","ks3://bigdata-datacake/parquet/users.parquet");
//        result.put("file_format_type","parquet");
        Map<String, Object> config = new HashMap();
        config.put(SOURCE_TYPE, result);
        return config;
    }
}
