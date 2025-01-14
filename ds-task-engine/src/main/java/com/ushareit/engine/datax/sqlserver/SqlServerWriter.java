package com.ushareit.engine.datax.sqlserver;

import com.alibaba.fastjson.JSONObject;
import com.ushareit.engine.Context;
import com.ushareit.engine.datax.Writer;
import com.ushareit.engine.datax.oracle.OracleWriter;
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
public class SqlServerWriter extends Writer {
    private static String URL_MODE = "jdbc:sqlserver://{0}:{1};DatabaseName={2}";

    public SqlServerWriter(Context context){
        context.prepare();
        JSONObject sink=context.getSinkConfigJson();
        this.setName("sqlserverwriter");
        Parameter parameter=new Parameter();
        this.setParameter(parameter);
        parameter.setPassword(sink.getString("password"));
        parameter.setUsername(sink.getString("username"));
        Parameter.Connection connection=new Parameter.Connection();
        parameter.setConnection(Lists.newArrayList(connection));
        String jdbcUrl=MessageFormat.format(URL_MODE,sink.getString("host"),sink.getString("port"),sink.getString("database"));
        if (StringUtils.isNoneBlank(sink.getString("jdbc_url_params"))){
            jdbcUrl=jdbcUrl+";"+sink.getString("jdbc_url_params").replaceAll("&",";");
        }
        connection.setJdbcUrl(jdbcUrl);
        if (CollectionUtils.isNotEmpty(context.getRuntimeConfig().getCatalog().getTables())){
            connection.setTable(context.getRuntimeConfig().getCatalog().getTables().stream().map(Table::getTargetTable).collect(Collectors.toList()));
            if (CollectionUtils.isNotEmpty(context.getRuntimeConfig().getCatalog().getTables().get(0).getColumns())){
                parameter.setColumn(context.getRuntimeConfig().getCatalog().getTables().get(0).getColumns().stream().map(Column::getName).collect(Collectors.toList()));
            }
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
        private List<Connection> connection;

        @Data
        public static class Connection{
            private List<String> table;
            private String jdbcUrl;//jdbc:oracle:thin:@[HOST_NAME]:PORT:[DATABASE_NAME]
        }

    }
}
