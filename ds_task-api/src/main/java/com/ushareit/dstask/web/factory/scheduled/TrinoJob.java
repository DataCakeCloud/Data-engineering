package com.ushareit.dstask.web.factory.scheduled;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ushareit.dstask.bean.CloudResouce;
import com.ushareit.dstask.bean.Task;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.constant.DsTaskConstant;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.service.impl.TaskServiceImpl;
import com.ushareit.dstask.web.factory.scheduled.param.Column;
import com.ushareit.dstask.web.factory.scheduled.param.DBEtlConfig;
import com.ushareit.dstask.web.utils.DataCakeConfigUtil;
import com.ushareit.dstask.web.utils.EncryptUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class TrinoJob extends ScheduledJob{
    private String sql;
    private boolean existTargetTable;
    List<Column> columns;

    private DBEtlConfig dbEtlConfig = new DBEtlConfig();

    public TrinoJob(Task task, TaskServiceImpl taskServiceImpl) {
        super(task, taskServiceImpl);
        String runtimeConfigJson = task.getRuntimeConfig();
        JSONObject runtimeConfigObject = JSON.parseObject(runtimeConfigJson);
        existTargetTable = runtimeConfigObject.getBoolean("existTargetTable")!= null ? runtimeConfigObject.getBoolean("existTargetTable") : true;
        dbEtlConfig.setConnectionUrl(runtimeConfigObject.getString("connectionUrl"));
        dbEtlConfig.setDbUser(runtimeConfigObject.getString("dbUser"));
        dbEtlConfig.setDbPassword(runtimeConfigObject.getString("dbPassword"));
        String srcQualifyName = runtimeConfigObject.getOrDefault("sourceDb", "") + "." + runtimeConfigObject.getOrDefault("sourceTable", "");
        if (!srcQualifyName.equals("\\.")) {
            dbEtlConfig.setSourceTable(srcQualifyName);
        }
        dbEtlConfig.setTargetTable(runtimeConfigObject.getString("targetDB") + "." + runtimeConfigObject.getString("targetTable"));
        String partitions;
        try {
            JSONArray defaut = JSON.parseObject("[{\"name\":\"datepart\",\"value\":\"{{ yesterday_ds }}\"}]", JSONArray.class);
            JSONArray partition = (JSONArray)runtimeConfigObject.getOrDefault("partitions",defaut);
            partitions = partition.stream().map(data -> {
                JSONObject json = JSON.parseObject(String.valueOf(data));
                if("Hive2Clickhouse".equals(templateCode)){
                    return String.format("%s=%s",json.getString("name"),json.getString("value"));
                }else{
                    return String.format("%s='%s'",json.getString("name"),json.getString("value"));
                }
            }).collect(Collectors.joining(","));
        }catch (Exception e){
            log.info("分区数据格式异常，采用旧逻辑兼容");
            partitions = (String) runtimeConfigObject.getOrDefault("partitions", "datepart='{{ yesterday_ds }}'");
        }
        dbEtlConfig.setPartitions(partitions);
        String schema = runtimeConfigObject.getString("columns");
        columns = JSON.parseArray(schema, Column.class);

        String sqlStr = task.getContent();
        if (sqlStr != null && !sqlStr.isEmpty()) {
            String reEncode;
            try {
                String whereClause = partitions.replaceAll(","," AND ");
                String deleteStr = String.format("delete from %s where %s;",dbEtlConfig.getTargetTable(),whereClause);
                String decodeSql = URLDecoder.decode(new String(Base64.getDecoder().decode(sqlStr.getBytes())), "UTF-8");
                String reSql = deleteStr +"\n" + String.format("insert into %s %s","bi_mysql."+dbEtlConfig.getTargetTable(),decodeSql);
                String urlEncode = URLEncoder.encode(reSql,"UTF-8").replace("+", "%20").replace("%2B", "+");
                Base64.Encoder encoder = Base64.getEncoder();
                reEncode = new String(encoder.encodeToString(urlEncode.getBytes()));


            }catch (Exception e){
                throw new ServiceException(BaseResponseCodeEnum.APP_START_FAIL,"sql解析失败");
            }
            sql = encodeFlag(reEncode);
            dbEtlConfig.setSql(sql);
        }
    }

    @Override
    protected List<Map<String, Object>> buildTaskItems() {
        HashMap<String, Object> taskItem = new HashMap <>();
        taskItems.add(taskItem);
        String runtimeConfig = task.getRuntimeConfig();
        JSONObject runtimeConfigObject = JSON.parseObject(runtimeConfig);
        String clusterTags;
        CloudResouce.DataResource cloudResource = DataCakeConfigUtil.getCloudResourcesService().getCloudResource(region);
        if ("common".equals(acrossCloud)) {
            clusterTags = String.format("type:k8s,region:%s,sla:%s,provider:%s", cloudResource.getRegion(), clusterSLA, cloudResource.getProvider());
        } else {
            clusterTags = String.format("type:%s,region:%s,sla:%s,provider:%s", acrossCloud, cloudResource.getRegion(), clusterSLA, cloudResource.getProvider());
        }
        taskItem.put("cluster_tags", clusterTags);
        if (this.clusterTags != null && this.clusterTags.length() > 0) {
            taskItem.put("cluster_tags", this.clusterTags);
        }
        if (dependencies != null && dependencies.length() > 0) {
            taskItem.put("dependencies", dependencies);
        }
        taskItem.put("task_type", "KyuubiTrinoOperator");
        taskItem.put("command", buildCommand());
        log.info("buildTaskItems taskItems=" + taskItems.toString());
        return taskItems;
    }

    @Override
    protected String buildCommand() {
        return sql;
    }

    @Override
    public void beforeExec() throws Exception{
        Connection cn= null;
        Statement smt = null;
        String sql = getCreateTableSql();
        try{
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            cn=DriverManager.getConnection(dbEtlConfig.getConnectionUrl(),dbEtlConfig.getDbUser(),EncryptUtil.decrypt(dbEtlConfig.getDbPassword(),DsTaskConstant.METADATA_PASSWDKEY));
            smt=cn.createStatement();
            log.info(String.format("start execute ddl:[%s]",sql));
            smt.executeUpdate(sql);
            System.out.println(sql);
            log.info(String.format("execute ddl:[%s] success",sql));
            cn.close();
        }catch(Exception e){
            System.out.println(e.getMessage());
            cn.close();
            throw new ServiceException(BaseResponseCodeEnum.HIVE_TABLE_FAIL,BaseResponseCodeEnum.HIVE_TABLE_FAIL.getMessage(),e.getMessage());
        }
    }





    public String getCreateTableSql(){
        String partitions = dbEtlConfig.getPartitions();
        String[] partition = new String[2];
        List<Column> addColumns = dbEtlConfig.getAddColumns();

        if(!partitions.isEmpty() && partitions != null){
            String[] partitionKV = partitions.split(",")[0].split("=");
            String partitionValue = partitionKV[1];
            if (partitionValue.startsWith("'") || partitionValue.startsWith("\"")) {
                partitionValue = partitionValue.substring(1);
            }
            if (partitionValue.endsWith("'") || partitionValue.endsWith("\"")) {
                partitionValue = partitionValue.substring(0, partitionValue.length() - 1);
            }
            partition[0] = partitionKV[0];
            partition[1] = partitionValue;
            if(addColumns != null){
                addColumns.add(new Column(partition[0],"VARCHAR(255)","","",null,"",""));
            }else{
                addColumns = Arrays.asList(new Column(partition[0],"VARCHAR(255)","","",null,"",""));
            }

        }

        // 生成查询sql语句
        String targetTable = dbEtlConfig.getTargetTable();

        if(addColumns!=null){
            columns.addAll(addColumns);
        }
        String schema = getTableSchema(columns);
        String sql = String.format("CREATE TABLE IF NOT EXISTS %s (%s) ",targetTable,schema);
        log.info("建标语句："+sql);
        return sql;
    }

    public String getTableSchema(List<Column> cloneColumns){
        Map<String,String> typeMap = new HashMap<>();
        typeMap.put("string","varchar(255)");
        typeMap.put("int","int");
        typeMap.put("double","double");
        typeMap.put("tinyint","int");
        typeMap.put("smallint","int");
        typeMap.put("bigint","bigint");
        typeMap.put("float","float");
        typeMap.put("real","float");
        typeMap.put("varchar","varchar(255)");
        typeMap.put("boolean","boolean");
        typeMap.put("","varchar(255)");
        typeMap.put("integer","int");
        String schema = cloneColumns.stream().distinct().map(column -> {
            String columnType;
            if (typeMap.containsKey(column.getColumnType())){
                columnType = typeMap.get(column.getColumnType());
            }else{
                columnType = "varchar(255)";
            }
            return String.format("%s %s COMMENT '%s'", column.getColumnName(), columnType, column.getComment());
        }).collect(Collectors.joining(","));
        return schema;
    }

    @Override
    public void afterExec() {

    }
}
