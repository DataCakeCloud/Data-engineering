package com.ushareit.engine.datax.hdfs;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ushareit.engine.Context;
import com.ushareit.engine.datax.Reader;
import com.ushareit.engine.param.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.io.IOUtils;
import cn.hutool.core.io.FileUtil;

import javax.persistence.Tuple;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@Slf4j
@AllArgsConstructor
@NoArgsConstructor
public class HdfsReader extends Reader {
    public HdfsReader(Context context){
        context.prepare();
        this.setName("hdfsreader");
        Parameter parameter=new Parameter();
        this.setParameter(parameter);
        parameter.setFieldDelimiter(context.getRuntimeConfig().getCatalog().getTables().get(0).getDelimiter());
        String location = context.getRuntimeConfig().getCatalog().getTables().get(0).getLocation();
        String patternStr = "(hdfs://\\S+?)(/.+)";
        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(location);
        if (matcher.find()) {
            parameter.setDefaultFS(matcher.group(1));
            parameter.setPath(matcher.group(2));
        }
        String partitions = context.getRuntimeConfig().getCatalog().getTables().get(0).getPartitions();
        if (StringUtils.isNoneBlank(partitions)) {
            ArrayList<String> path = new ArrayList<>();
            path.add(parameter.getPath());
            Arrays.stream(partitions.split(","))
                    .forEach(partition -> {
                        path.add(partition);
                    });
            parameter.setPath(String.join("/", path));
        }

        String fileFormat = context.getRuntimeConfig().getCatalog().getTables().get(0).getFileFormat();
        switch (fileFormat.split("\\.")[fileFormat.split("\\.").length - 1]){
            case "OrcInputFormat":
                parameter.setFileType("orc");
                break;
            case "TextInputFormat":
                parameter.setFileType("text");
                break;
            case "RCFileInputFormat":
                parameter.setFileType("rc");
                break;
            case "SequenceFileInputFormat":
                parameter.setFileType("seq");
                break;
            case "MapredParquetInputFormat":
                parameter.setFileType("parquet");
                break;
            default:
                parameter.setFileType("text");
        }
        HashMap<String, Object> hadoopConfig = new HashMap<>();
        hadoopConfig.put("dfs.nameservices","hdfs-ha");
        hadoopConfig.put("dfs.ha.namenodes.hdfs-ha","nn1,nn2");
        hadoopConfig.put("dfs.namenode.rpc-address.hdfs-ha.nn1","10.64.3.217:8020");
        hadoopConfig.put("dfs.namenode.rpc-address.hdfs-ha.nn2","10.64.3.92:8020");
        hadoopConfig.put("dfs.client.failover.proxy.provider.hdfs-ha","org.apache.hadoop.hdfs.server.namenode.ha.ConfiguredFailoverProxyProvider");
        parameter.setHadoopConfig(hadoopConfig);
        String userGroupName = context.getRuntimeConfig().getAdvancedParameters().getUserGroupName();
        parameter.setKerberosKeytabFilePath(MessageFormat.format(KERBEROS_FILE_PATH,userGroupName,userGroupName));
        parameter.setHaveKerberos(true);
        parameter.setKerberosPrincipal(MessageFormat.format(KERBEROS_PRINCIPAL,userGroupName));
        List<Map<String,Object>> columns= new ArrayList<>();
        List<Column> sourceTableColumn = context.getRuntimeConfig().getCatalog().getTables().get(0).getSourceTableColumn();
        HashMap<String, Column> cloumnMap = new HashMap<>();
        sourceTableColumn.stream().forEach(data->{
            cloumnMap.put(data.getName(), data);
        });

        if (CollectionUtils.isNotEmpty(context.getRuntimeConfig().getCatalog().getTables().get(0).getColumns())){
            for (int i=0;i<context.getRuntimeConfig().getCatalog().getTables().get(0).getColumns().size();i++){
                HashMap<String, Object> column = new HashMap<>();
                Column origin = context.getRuntimeConfig().getCatalog().getTables().get(0).getColumns().get(i);
                if (cloumnMap.containsKey(origin.getName())) {
                    column.put("index", cloumnMap.get(origin.getName()).getIndex());
                    column.put("type",typeTransform(cloumnMap.get(origin.getName()).getColumnType()));
                    columns.add(column);
                }
            }
        }
        parameter.setColumn(columns);
        String targetPartition = context.getRuntimeConfig().getCatalog().getTables().get(0).getTargetPartition();
        if (StringUtils.isNotBlank(targetPartition)){
            HashMap<String, Object> column = new HashMap<>();
            column.put("type","String");
            column.put("value",targetPartition.split("=",2)[1]);
            parameter.column.add(column);
        }
        Boolean nullPointExit = context.getRuntimeConfig().getTaskParam().getNullPointExit();
        parameter.setNullPointExit(nullPointExit);

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

    private Parameter parameter;

    @Data
    public static class Parameter{
        private String fieldDelimiter;
        private String defaultFS;
        private String fileType;
        private String path;
        private String encoding;
        private List<Map<String,Object>> column;
        private Map<String,Object> hadoopConfig;
        private Boolean haveKerberos;
        private String kerberosKeytabFilePath;
        private String kerberosPrincipal;
        private Boolean nullPointExit;

    }
}
