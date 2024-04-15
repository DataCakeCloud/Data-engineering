package com.ushareit.engine.datax.hdfs;

import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ushareit.engine.Context;
import com.ushareit.engine.datax.Writer;
import com.ushareit.engine.datax.doris.DorisWriter;
import com.ushareit.engine.datax.mysql.MysqlWriter;
import com.ushareit.engine.param.Catalog;
import com.ushareit.engine.param.Column;
import com.ushareit.engine.param.RuntimeConfig;
import com.ushareit.engine.param.Table;
import com.ushareit.engine.seatunnel.adapter.bean.IcebergConfig;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Lists;

import java.util.*;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Data
@Slf4j
@AllArgsConstructor
@NoArgsConstructor
public class HdfsWriter extends Writer {

    public HdfsWriter(Context context){
        context.prepare();
        this.setName("hdfswriter");
        Parameter parameter=new Parameter();
        this.setParameter(parameter);
        parameter.setCompress("NONE");
        Table table = context.getRuntimeConfig().getCatalog().getTables().get(0);
        if(context.getRuntimeConfig().getCatalog().getTables().get(0).getAutoCreateTable()){
            parameter.setFieldDelimiter("\2");
            parameter.setFileType("orc");
            parameter.setCompress("SNAPPY");
            parameter.setDefaultFS("hdfs://hdfs-ha");
            parameter.setPath(context.getRuntimeConfig().getTaskParam().getLocation());
        }else{
            parameter.setFieldDelimiter(table.getDelimiter());
            String location = table.getLocation();
            parameter.setCompress(table.getCompress());
            String patternStr = "(hdfs://\\S+?)(/.+)";
            Pattern pattern = Pattern.compile(patternStr);
            Matcher matcher = pattern.matcher(location);
            if (matcher.find()) {
                parameter.setDefaultFS(matcher.group(1));
                parameter.setPath(matcher.group(2));
            }
            String fileFormat = table.getFileFormat();
            switch (fileFormat.split("\\.")[fileFormat.split("\\.").length -1]){
                case "OrcInputFormat":
                    parameter.setFileType("orc");
                    break;
                case "TextInputFormat":
                    parameter.setFileType("text");
                    break;
                default:
                    parameter.setFileType("text");
            }
            String partitions = context.getRuntimeConfig().getCatalog().getTables().get(0).getPartitions();
            if (StringUtils.isNoneBlank(partitions)){
                ArrayList<String> path = new ArrayList<>();
                path.add(parameter.getPath());
                Arrays.stream(partitions.split(","))
                        .forEach(partition->{
                            path.add(partition);
                        });
                parameter.setPath(String.join("/",path));
            }
        }
        IcebergConfig catalogConfig = context.getSinkConfigJson().getObject("catalog_config", IcebergConfig.class);
        String defaultFS = catalogConfig.getDefaultFS();
        if (StringUtils.isNotBlank(defaultFS)){
            Map<String, Object> hadoopConf = catalogConfig.getHadoopConf();
            parameter.setDefaultFS(defaultFS);
            parameter.setHadoopConfig(hadoopConf);
        }else{
            String userGroupName = context.getRuntimeConfig().getAdvancedParameters().getUserGroupName();
            HashMap<String, Object> hadoopConfig = new HashMap<>();
            hadoopConfig.put("dfs.nameservices","hdfs-ha");
            hadoopConfig.put("dfs.ha.namenodes.hdfs-ha","nn1,nn2");
            hadoopConfig.put("dfs.namenode.rpc-address.hdfs-ha.nn1","10.64.3.217:8020");
            hadoopConfig.put("dfs.namenode.rpc-address.hdfs-ha.nn2","10.64.3.92:8020");
            hadoopConfig.put("dfs.client.failover.proxy.provider.hdfs-ha","org.apache.hadoop.hdfs.server.namenode.ha.ConfiguredFailoverProxyProvider");
            parameter.setHadoopConfig(hadoopConfig);
            parameter.setKerberosKeytabFilePath(MessageFormat.format(KERBEROS_FILE_PATH,userGroupName,userGroupName));
            parameter.setHaveKerberos(true);
            parameter.setKerberosPrincipal(MessageFormat.format(KERBEROS_PRINCIPAL,userGroupName));
        }
        parameter.setFileName(context.getRuntimeConfig().getCatalog().getTargetDb()+"."+context.getRuntimeConfig().getCatalog().getTables().get(0).getTargetTable());
        if (CollectionUtils.isNotEmpty(context.getRuntimeConfig().getCatalog().getTables().get(0).getColumns())){
            parameter.setColumn(Lists.newArrayList());
            for (Column column:context.getRuntimeConfig().getCatalog().getTables().get(0).getColumns()){
                Parameter.Column c=new Parameter.Column();
                c.setName(column.getColumnName());
                c.setType(column.getColumnType());
                parameter.getColumn().add(c);
            }
        }
        RuntimeConfig.TaskParam taskParam = context.getRuntimeConfig().getTaskParam();
        if(StringUtils.isNotBlank(taskParam.getWriteMode())){
            parameter.setWriteMode(taskParam.getWriteMode());
        }
    }

    private Parameter parameter;
    @Data
    public static class Parameter{
        private String fieldDelimiter;
        private String compress;
        private String writeMode="truncate";
        private String defaultFS;
        private String fileType;
        private String path;
        private String fileName;
        private List<Column> column;
        private Map<String,Object> hadoopConfig;
        private Boolean haveKerberos;
        private String kerberosKeytabFilePath;
        private String kerberosPrincipal;
        @Data
        public static class Column{
            private String name;
            private String type;
        }

    }
}
