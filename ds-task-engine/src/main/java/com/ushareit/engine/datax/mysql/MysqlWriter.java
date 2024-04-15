package com.ushareit.engine.datax.mysql;

import com.alibaba.fastjson.JSONObject;
import com.ushareit.engine.Context;
import com.ushareit.engine.datax.Writer;
import com.ushareit.engine.param.Column;
import com.ushareit.engine.param.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Lists;

import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MysqlWriter extends Writer {
    private static String URL_MODE = "jdbc:mysql://{0}:{1}/{2}";

    public MysqlWriter(Context context){
        context.prepare();
        JSONObject sink=context.getSinkConfigJson();
        this.setName("mysqlwriter");
        Parameter parameter=new Parameter();
        this.setParameter(parameter);
        parameter.setPassword(sink.getString("password"));
        parameter.setUsername(sink.getString("username"));
        Parameter.Connection connection=new Parameter.Connection();
        parameter.setConnection(Lists.newArrayList(connection));
        String exportMode = context.getRuntimeConfig().getAdvancedParameters().getExportMode();
        if (StringUtils.isNotEmpty(exportMode)) {
            parameter.setWriteMode(exportMode);
        }
        String jdbcUrl=MessageFormat.format(URL_MODE,sink.getString("host"),sink.getString("port"),sink.getString("database"));
        if (StringUtils.isNoneBlank(sink.getString("jdbc_url_params"))){
            jdbcUrl=jdbcUrl+"?"+sink.getString("jdbc_url_params");
        }
        connection.setJdbcUrl(jdbcUrl);
        if (CollectionUtils.isNotEmpty(context.getRuntimeConfig().getCatalog().getTables())){
            connection.setTable(context.getRuntimeConfig().getCatalog().getTables().stream().map(Table::getTargetTable).collect(Collectors.toList()));
            if (CollectionUtils.isNotEmpty(context.getRuntimeConfig().getCatalog().getTables().get(0).getColumns())){
                parameter.setColumn(context.getRuntimeConfig().getCatalog().getTables().get(0).getColumns().stream().map(data->String.format("`%s`",data.getColumnName())).collect(Collectors.toList()));
            }
        }
        String targetPartition = context.getRuntimeConfig().getCatalog().getTables().get(0).getTargetPartition();
        List<String> preSql = context.getRuntimeConfig().getPreSql();
        List<String> postSql = context.getRuntimeConfig().getPostSql();
        if(preSql!=null && preSql.size()>0){
            parameter.setPreSql(preSql);
        }
        if(postSql!=null && postSql.size()>0){
            parameter.setPostSql(postSql);
        }

        if (StringUtils.isNotBlank(targetPartition)){
            parameter.column.add(targetPartition.split("=",2)[0]);
        }
    }

    private Parameter parameter;
    @Data
    public static class Parameter{
        private String writeMode="insert";
        private String username;
        private String password;
        private List<String> column;
        private List<String> session;
        private List<String> preSql;
        private List<String>  postSql;
        private List<Connection> connection;

        @Data
        public static class Connection{
            private List<String> table;
            private String jdbcUrl;
        }

    }
}
