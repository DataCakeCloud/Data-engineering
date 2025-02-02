package com.ushareit.engine.datax.mysql;

import com.alibaba.fastjson.JSONObject;
import com.ushareit.engine.Context;
import com.ushareit.engine.datax.Reader;
import com.ushareit.engine.datax.doris.DorisWriter;
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
public class MysqlReader extends Reader {
    private static String URL_MODE = "jdbc:mysql://{0}:{1}/{2}";

    public MysqlReader(Context context){
        context.prepare();
        JSONObject sourceConfigJson=context.getSourceConfigJson();
        this.setName("mysqlreader");
        Parameter parameter=new Parameter();
        this.setParameter(parameter);
        parameter.setPassword(sourceConfigJson.getString("password"));
        parameter.setUsername(sourceConfigJson.getString("username"));
        Parameter.Connection connection=new Parameter.Connection();
        parameter.setConnection(Lists.newArrayList(connection));
        parameter.setWhere(context.getRuntimeConfig().getCatalog().getTables().get(0).getFilterStr());
        String jdbcUrl=MessageFormat.format(URL_MODE,sourceConfigJson.getString("host"),sourceConfigJson.getString("port"),sourceConfigJson.getString("database"));
        if (StringUtils.isNoneBlank(sourceConfigJson.getString("jdbc_url_params"))){
           jdbcUrl=jdbcUrl+"?"+sourceConfigJson.getString("jdbc_url_params");
        }
        connection.setJdbcUrl(Lists.newArrayList(jdbcUrl));

        if (CollectionUtils.isNotEmpty(context.getRuntimeConfig().getCatalog().getTables())){
            connection.setTable(context.getRuntimeConfig().getCatalog().getTables().stream().map(Table::getSourceTable).map(s -> "`" + s + "`").collect(Collectors.toList()));
            if (CollectionUtils.isNotEmpty(context.getRuntimeConfig().getCatalog().getTables().get(0).getColumns())){
                parameter.setColumn(context.getRuntimeConfig().getCatalog().getTables().get(0).getColumns().stream().map(data->String.format("`%s`",data.getName())).collect(Collectors.toList()));
            }
        }
        String splitPk = context.getRuntimeConfig().getCatalog().getTables().get(0).getSplitPk();
        if(StringUtils.isNotBlank(splitPk)) {
            parameter.setSplitPk(splitPk);
        }
    }

    private Parameter parameter;

    @Data
    public static class Parameter{
        private String where;
        private String username;
        private String password;
        private String splitPk;
        private List<String> column;
        private List<Connection> connection;

        @Data
        public static class Connection{
            private List<String> table;
            private List<String> jdbcUrl;
        }

    }
}
