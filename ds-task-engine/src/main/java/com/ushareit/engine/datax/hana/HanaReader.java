package com.ushareit.engine.datax.hana;

import com.alibaba.fastjson.JSONObject;
import com.ushareit.engine.Context;
import com.ushareit.engine.datax.Reader;
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

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HanaReader extends Reader {

    public HanaReader(Context context){
        context.prepare();
        JSONObject sourceConfigJson=context.getSourceConfigJson();
        this.setName("rdbmsreader");
        Parameter parameter=new Parameter();
        this.setParameter(parameter);
        parameter.setPassword(sourceConfigJson.getString("password"));
        parameter.setUsername(sourceConfigJson.getString("username"));
        Parameter.Connection connection=new Parameter.Connection();
        parameter.setConnection(Lists.newArrayList(connection));
        parameter.setWhere(context.getRuntimeConfig().getCatalog().getTables().get(0).getFilterStr());
        connection.setJdbcUrl(Arrays.asList(sourceConfigJson.getString("jdbc_url_params")));

        String sourceDb = context.getRuntimeConfig().getCatalog().getSourceDb();

        if (CollectionUtils.isNotEmpty(context.getRuntimeConfig().getCatalog().getTables())){
            connection.setTable(context.getRuntimeConfig().getCatalog().getTables().stream().map(table -> String.format("%s.%s",sourceDb,table.getSourceTable())).collect(Collectors.toList()));
            if (CollectionUtils.isNotEmpty(context.getRuntimeConfig().getCatalog().getTables().get(0).getColumns())){
                parameter.setColumn(context.getRuntimeConfig().getCatalog().getTables().get(0).getColumns().stream().map(data->String.format("\"%s\"", data.getName())).collect(Collectors.toList()));
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
