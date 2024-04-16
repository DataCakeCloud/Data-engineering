package com.ushareit.engine;


import com.alibaba.fastjson.JSONObject;
import com.ushareit.engine.param.Catalog;
import com.ushareit.engine.param.Column;
import com.ushareit.engine.param.RuntimeConfig;
import com.ushareit.engine.param.Table;
import com.ushareit.engine.seatunnel.util.DataSourceUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 测试数据源链接
 */
public class DataSourceExample {

    public static void main(String[] args) {
        Catalog catalog = new Catalog();
        com.ushareit.engine.param.Table ta = new com.ushareit.engine.param.Table();
        ta.setSourceTable("empty");

        List<Table> tables = new ArrayList<>();
        tables.add(ta);
        catalog.setTables(tables);
//        List<Column> columns = new ArrayList<>();
//        Column column = new Column();
//        column.setName("engine_key").setColumnType("String");
//        columns.add(column);
//        ta.setColumns(columns);

        RuntimeConfig runtimeConfig = new RuntimeConfig();
        runtimeConfig.setExecuteMode("local")
                .setCatalog(catalog);

        List<Map<String, Object>> mysql = DataSourceUtil.getTables("test-datasource", JSONObject.toJSONString(runtimeConfig),
                "mysql", "{\"jdbc_url_params\":\"useUnicode=true&characterEncoding=UTF-8&useSSL=false\",\"password\":\"DRQ9wk8VJ&J4eYgb\",\"database\":\"query_editor\",\"port\":3306,\"replication_method\":\"STANDARD\",\"host\":\"test.datacake-cloud.bdp.sg2.mysql\",\"ssl\":false,\"tunnel_method\":{\"tunnel_method\":\"NO_TUNNEL\"},\"username\":\"query_editor\"}");

        ta.setSourceTable("engine");
        List<Map<String, String>> schema = DataSourceUtil.getSchema("test-datasource1", JSONObject.toJSONString(runtimeConfig),
                "mysql", "{\"jdbc_url_params\":\"useUnicode=true&characterEncoding=UTF-8&useSSL=false\",\"password\":\"DRQ9wk8VJ&J4eYgb\",\"database\":\"query_editor\",\"port\":3306,\"replication_method\":\"STANDARD\",\"host\":\"test.datacake-cloud.bdp.sg2.mysql\",\"ssl\":false,\"tunnel_method\":{\"tunnel_method\":\"NO_TUNNEL\"},\"username\":\"query_editor\"}");


        System.out.println(mysql);
        System.out.println(schema);


    }
}
