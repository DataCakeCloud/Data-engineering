package com.ushareit.engine.datax.doris;

import com.alibaba.fastjson.JSONObject;
import com.ushareit.engine.Context;
import com.ushareit.engine.datax.Writer;
import com.ushareit.engine.datax.oracle.OracleWriter;
import com.ushareit.engine.param.Column;
import com.ushareit.engine.param.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Lists;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.ushareit.engine.constant.SourceEnum.Doris;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DorisWriter extends Writer {
    private static String URL_MODE = "jdbc:mysql://{0}:{1}/{2}";
    private static String LOAD_URL = "{0}:{1}";

    public DorisWriter(Context context){
        context.prepare();
        JSONObject sink=context.getSinkConfigJson();
        if (context.getSinkType().equalsIgnoreCase(Doris.name())){
            this.setName("doriswriter");
        }else{
            this.setName("starrockswriter");
        }
        Parameter parameter=new Parameter();
        this.setParameter(parameter);
        String port=sink.getString("port");
        if (StringUtils.isBlank(port)){
            port=sink.getString("queryport");
        }
        String httpport = sink.getString("httpport");
        parameter.setLoadUrl(Lists.newArrayList(MessageFormat.format(LOAD_URL,sink.getString("host"),httpport)));
        parameter.setPassword(sink.getString("password"));
        parameter.setUsername(sink.getString("username"));
        Parameter.Connection connection=new Parameter.Connection();
        parameter.setConnection(Lists.newArrayList(connection));

        String jdbcUrl=MessageFormat.format(URL_MODE,sink.getString("host"),port,sink.getString("database"));
        if (StringUtils.isNoneBlank(sink.getString("jdbc_url_params"))){
            jdbcUrl=jdbcUrl+"?"+sink.getString("jdbc_url_params");
        }
        connection.setJdbcUrl(jdbcUrl);
        connection.setSelectedDatabase(sink.getString("database"));
        if (CollectionUtils.isNotEmpty(context.getRuntimeConfig().getCatalog().getTables())){
            connection.setTable(context.getRuntimeConfig().getCatalog().getTables().stream().map(Table::getTargetTable).collect(Collectors.toList()));
            if (CollectionUtils.isNotEmpty(context.getRuntimeConfig().getCatalog().getTables().get(0).getColumns())){
                parameter.setColumn(context.getRuntimeConfig().getCatalog().getTables().get(0).getColumns().stream().map(Column::getColumnName).collect(Collectors.toList()));
            }
        }
        String targetPartition = context.getRuntimeConfig().getCatalog().getTables().get(0).getTargetPartition();
        Parameter.LoadProps loadProps = new Parameter.LoadProps();
        loadProps.setFormat("json");
        loadProps.setStrip_outer_array(true);
        if("full".equalsIgnoreCase(context.getRuntimeConfig().getTaskParam().getSyncmode())){
            String targetTable = parameter.getConnection().get(0).getTable().get(0);
            List<String> preSql = Arrays.asList(String.format("ALTER TABLE %s DROP TEMPORARY PARTITION IF EXISTS tmp", targetTable),
                    String.format("ALTER TABLE %s ADD TEMPORARY PARTITION IF NOT EXISTS tmp VALUES [('0000-01-01'), ('9999-12-31'))",targetTable));
            parameter.setPreSql(preSql);
            List<String> postSql = Arrays.asList(String.format("ALTER TABLE %s REPLACE PARTITION (active) WITH TEMPORARY PARTITION (tmp)", targetTable));
            parameter.setPostSql(postSql);
            loadProps.setTemporary_partitions("tmp");
        }else{
            List<String> preSql = context.getRuntimeConfig().getPreSql();
            List<String> postSql = context.getRuntimeConfig().getPostSql();
            if(preSql!=null && preSql.size()>0){
                parameter.setPreSql(preSql);
            }
            if(postSql!=null && postSql.size()>0){
                parameter.setPostSql(postSql);
            }
        }
        parameter.setLoadProps(loadProps);
        if (StringUtils.isNotBlank(targetPartition)){
            parameter.column.add(targetPartition.split("=",2)[0]);
        }
        int flushInterval = context.getRuntimeConfig().getTaskParam().getFlushInterval();
        int maxBatchRows = context.getRuntimeConfig().getTaskParam().getMaxBatchRows();
        int batchSize = context.getRuntimeConfig().getTaskParam().getBatchSize();
        if(flushInterval>0){
            parameter.setFlushInterval(flushInterval);
        }
        if(maxBatchRows>0){
            parameter.setMaxBatchRows(maxBatchRows);
        }
        if(batchSize>0){
            parameter.setBatchSize(batchSize);
        }

    }


    private Parameter parameter;
    @Data
    public static class Parameter{
        private List<String> loadUrl;
        private String username;
        private String password;
        private List<String> column;
        private List<String> session;
        private List<String> preSql;
        private List<String>  postSql;
        private int flushInterval=30000;
        private int maxBatchRows=500000;
        private int batchSize=104857600;
        private LoadProps loadProps;
        private List<Connection> connection;

        @Data
        public static class Connection{
            private List<String> table;
            private String selectedDatabase;
            private String jdbcUrl;//jdbc:mysql://172.16.0.13:9030/demo
        }

        /**
         * "loadProps": {
         *                             "format": "json",
         *                             "strip_outer_array": true
         *                         }
         */
        @Data
        public static class LoadProps{
            private String format="json";
            private boolean strip_outer_array=true;
            private String temporary_partitions;
        }

    }
}
