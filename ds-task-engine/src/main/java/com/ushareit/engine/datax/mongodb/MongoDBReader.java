package com.ushareit.engine.datax.mongodb;

import com.alibaba.fastjson.JSONObject;
import com.ushareit.engine.Context;
import com.ushareit.engine.datax.Reader;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MongoDBReader extends Reader {
    private MongoDBReader.Parameter parameter;
    public MongoDBReader(Context context){
        context.prepare();
        this.setName("mongodbreader");
        MongoDBReader.Parameter parameter=new MongoDBReader.Parameter();
        this.setParameter(parameter);
        JSONObject sourceConfigJson = context.getSourceConfigJson();
        JSONObject instanceType = sourceConfigJson.getJSONObject("instance_type");
        String urlParam = instanceType.getString("urlParam");
        if(StringUtils.isNotBlank("urlParam")){
            parameter.setUrlParam(urlParam);
        }
        parameter.setAddress(Arrays.stream(instanceType.getString("server_addresses").split(",")).collect(Collectors.toList()));
        String username = sourceConfigJson.getString("user");
        parameter.setUserName(username);
        String password = sourceConfigJson.getString("password");
        String sourceDb = context.getRuntimeConfig().getCatalog().getSourceDb();
        parameter.setDbName(sourceDb);
        parameter.setUserPassword(password);
        parameter.setCollectionName(context.getRuntimeConfig().getCatalog().getTables().get(0).getSourceTable());
        List<Map<String,Object>> columns= new ArrayList<>();
        if (CollectionUtils.isNotEmpty(context.getRuntimeConfig().getCatalog().getTables().get(0).getColumns())){
            for (int i=0;i<context.getRuntimeConfig().getCatalog().getTables().get(0).getColumns().size();i++){
                HashMap<String, Object> column = new HashMap<>();
                column.put("index",i);
                column.put("type",context.getRuntimeConfig().getCatalog().getTables().get(0).getColumns().get(i).getData_type());
                columns.add(column);
            }
        }
        parameter.setColumn(columns);
        String partition = context.getRuntimeConfig().getCatalog().getTables().get(0).getPartitions();
        if (StringUtils.isNotBlank(partition)){
            HashMap<String, Object> column = new HashMap<>();
            column.put("type","String");
            column.put("value",partition.split("=",2)[1]);
            parameter.column.add(column);
        }
        String filterStr = context.getRuntimeConfig().getCatalog().getTables().get(0).getFilterStr();
        if (StringUtils.isNotBlank(filterStr)){
            parameter.setQuery(filterStr);
        }
    }

    @Data
    public static class Parameter{
        private List<String> address;
        private String userName;
        private String userPassword;
        private String dbName;
        private String collectionName;
        private String query;
        private List<Map<String,Object>> column;
        private String urlParam;
    }

}