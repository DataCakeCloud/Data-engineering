package com.ushareit.dstask.web.utils;

import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.exception.ServiceException;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.orc.OrcFile;
import org.apache.orc.Reader;
import org.apache.orc.TypeDescription;
import org.apache.parquet.ParquetReadOptions;
import org.apache.parquet.hadoop.ParquetFileReader;
import org.apache.parquet.hadoop.metadata.ParquetMetadata;
import org.apache.parquet.hadoop.util.HadoopInputFile;
import org.apache.parquet.schema.MessageType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

public class FileSystemUtil {

    public static Configuration getFileSystemConf(String accessKey,String accessSecret,String implClass,String defaultFS,String type) throws IOException {
        Configuration conf = new Configuration();
        switch (type){
            case "s3":
                conf.set("fs.s3a.access.key", accessKey);
                conf.set("fs.s3a.secret.key", accessSecret);
                conf.set("fs.s3.impl", implClass);
                conf.set("fs.defaultFS", defaultFS);
                break;
            case "ks3":
                conf.set("fs.ks3.AccessKey",accessKey);
                conf.set("fs.ks3.AccessSecret",accessSecret);
                conf.set("fs.ks3.impl",implClass);
                conf.set("fs.defaultFS",defaultFS);
                break;
            case "hdfs":
                System.setProperty("java.security.krb5.conf", "/Users/api/src/main/resources/kerberos-conf/krb5.conf");
                conf.set("hadoop.security.authentication","kerberos");
                conf.set("dfs.client.use.datanode.hostname","true");
                conf.set("dfs.data.transfer.protection","authentication");
                conf.set("fs.defaultFS",defaultFS);
                UserGroupInformation.setConfiguration(conf);
                UserGroupInformation.loginUserFromKeytab("hive/127.0.0.1@EXAMPLE.COM",
                        "/Users/shareit/ninebot/hive.keytab");
                break;
            default:
                throw new ServiceException(BaseResponseCodeEnum.NOT_SUPPORT_STORAGE_TYPE.name(),
                        String.format(BaseResponseCodeEnum.NOT_SUPPORT_STORAGE_TYPE.getMessage()));

        }
        return conf;
    }


    public static List<Map<String,String>> getParquetSchema(Configuration conf,Path path) throws IOException {
        HadoopInputFile hadoopInputFile = HadoopInputFile.fromPath(path, conf);
        ParquetFileReader parquetFileReader = ParquetFileReader.open(hadoopInputFile, ParquetReadOptions.builder().build());
        ParquetMetadata metaData = parquetFileReader.getFooter();
        MessageType schema = metaData.getFileMetaData().getSchema();
        parquetFileReader.close();

        List<Map<String, String>> columnDetail = schema.getColumns().stream().map(column -> {
            Map<String, String> map = new HashMap<>();
            map.put("name", column.getPrimitiveType().getName());
            map.put("type", parquetColumnTypeTransform(column.getPrimitiveType().getPrimitiveTypeName().name()));
            return map;
        }).collect(Collectors.toList());
        return columnDetail;
    }

    public static List<Map<String,String>> getTextFileSchema(Configuration conf,Path path,String delimiter) throws IOException {
        FileSystem fs = FileSystem.get(conf);
        BufferedReader reader = new BufferedReader(new InputStreamReader(fs.open(path)));
        String line = reader.readLine();
        ArrayList<Map<String, String>> columnDetail = new ArrayList<>();
        if(StringUtils.isNotBlank(line)){
            String[] columnName = line.split(delimiter);
            for(int i=0;i<columnName.length;i++){
                HashMap<String, String> map = new HashMap<>();
                map.put("name",columnName[i]);
                map.put("type","String");
                columnDetail.add(map);
            }
        }
        return columnDetail;
    }


    public static List<Map<String,String>> getOrcSchema(Configuration conf,Path path) throws IOException {
        Reader reader = OrcFile.createReader(path,OrcFile.readerOptions(conf));
        TypeDescription schema = reader.getSchema();
        ArrayList<Map<String, String>> columnDetail = new ArrayList<>();
        for (int i=0;i<schema.getMaximumId();i++){
            Map<String, String> map = new HashMap<>();
            map.put("name", schema.getFieldNames().get(i));
            map.put("type", orcColumnTypeTransform(schema.getChildren().get(i).toString()));
            columnDetail.add(map);
        }
        return columnDetail;
    }

    public static Path getPath(Configuration conf,Path path) throws IOException {
        FileSystem fs = FileSystem.get(conf);
        if(fs.getFileStatus(path).isFile()){
            return path;
        }
        RemoteIterator<LocatedFileStatus> files = fs.listFiles(path,true);
        while (files.hasNext()){
            LocatedFileStatus fileStatus = files.next();
            if(fileStatus.isFile()){
                return new Path(fileStatus.getPath().toUri().getPath());
            }
        }
        return null;
    }

    public static String parquetColumnTypeTransform(String type){
        switch (type.toLowerCase()){
            case "int32":
            case "int64":
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

    public static String orcColumnTypeTransform(String type){
        switch (type.toLowerCase()){
            case "byte":
            case "short":
            case "int":
            case "long":
                return "Long";
            case "float":
            case "double":
            case "decimal":
                return "Double";
            case "boolean":
                return "Boolean";
            default:
                return "String";
        }
    }
}
