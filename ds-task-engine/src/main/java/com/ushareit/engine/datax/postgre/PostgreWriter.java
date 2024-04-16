package com.ushareit.engine.datax.postgre;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ushareit.engine.Context;
import com.ushareit.engine.datax.Writer;
import com.ushareit.engine.datax.mysql.MysqlWriter;
import com.ushareit.engine.param.Column;
import com.ushareit.engine.param.Table;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Lists;

import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class PostgreWriter extends Writer {
    private static String URL_MODE = "jdbc:postgresql://{0}:{1}/{2}";


    public PostgreWriter(Context context){
        context.prepare();
        JSONObject sink=context.getSinkConfigJson();
        this.setName("postgresqlwriter");
        Parameter parameter=new Parameter();
        this.setParameter(parameter);
        parameter.setPassword(sink.getString("password"));
        parameter.setUsername(sink.getString("username"));
        Parameter.Connection connection=new Parameter.Connection();
        parameter.setConnection(Lists.newArrayList(connection));
        String jdbcUrl=MessageFormat.format(URL_MODE,sink.getString("host"),sink.getString("port"),sink.getString("database"));
        if (StringUtils.isNoneBlank(sink.getString("jdbc_url_params"))){
            jdbcUrl=jdbcUrl+"?"+sink.getString("jdbc_url_params");
        }
        JSONArray schemas = sink.getJSONArray("schemas");
        String schema;
        if(schemas != null && schemas.size()>0){
            schema = schemas.getString(0);
        } else {
            schema = "public";
        }
        connection.setJdbcUrl(jdbcUrl);
        if (CollectionUtils.isNotEmpty(context.getRuntimeConfig().getCatalog().getTables())){
            connection.setTable(context.getRuntimeConfig().getCatalog().getTables().stream().map(data->schema+"."+data.getTargetTable()).collect(Collectors.toList()));
            if (CollectionUtils.isNotEmpty(context.getRuntimeConfig().getCatalog().getTables().get(0).getColumns())){
                parameter.setColumn(context.getRuntimeConfig().getCatalog().getTables().get(0).getColumns().stream().map(Column::getColumnName).collect(Collectors.toList()));
            }
        }
        List<String> preSql = context.getRuntimeConfig().getPreSql();
        List<String> postSql = context.getRuntimeConfig().getPostSql();
        if(preSql!=null && preSql.size()>0){
            parameter.setPreSql(preSql);
        }
        if(postSql!=null && postSql.size()>0){
            parameter.setPostSql(postSql);
        }
    }

    private Parameter parameter;
    @Data
    public static class Parameter{
        private String username;
        private String password;
        private List<String> column;
        private List<String> preSql;
        private List<Connection> connection;
        private List<String>  postSql;

        @Data
        public static class Connection{
            private List<String> table;
            private String jdbcUrl;
        }

    }
}
