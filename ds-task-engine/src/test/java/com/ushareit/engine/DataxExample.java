package com.ushareit.engine;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ushareit.engine.datax.Job;
import com.ushareit.engine.datax.adapter.DataxAdapter;

import java.util.Map;

public class DataxExample {

    public static Context initContext(){
        Context context = new Context();
        context.setRuntimeConfigStr("{\"sourceRegion\":\"ue1\",\"executeMode\":\"local\",\"sourceId\":\"4e84c531-7d9d-4865-83d6-4e96be38af84\",\"destinationId\":\"dfe60cd6-2690-4ab7-8f27-16c70e1b16ee\",\"catalog\":{\"table_type\":\"single\",\"sourceDb\":\"score\",\"targetDb\":\"default\",\"tables\":[{\"sourceTable\":\"engine\",\"partitions\":\"\",\"targetTable\":\"engine_t\",\"targetTablePart\":\"\",\"primaryKey\":\"\",\"addColumns\":[],\"columns\":[{\"columnType\":\"STRING\",\"name\":\"engine_key\",\"data_type\":\"varchar\",\"columnComment\":\"这个是\\\"name\\\"\",\"comment\":\"这个是\\\"name\\\"\",\"columnName\":\"name\"},{\"columnType\":\"INT\",\"name\":\"id\",\"data_type\":\"int\",\"columnComment\":\"\",\"comment\":\"\",\"columnName\":\"id\"}],\"existTargetTable\":false}],\"sync_mode\":\"full_refresh\"},\"advancedParameters\":{\"maxActiveRuns\":1,\"acrossCloud\":\"common\",\"resourceLevel\":\"standard\",\"startDate\":\"\",\"endDate\":\"\",\"dsGroups\":[],\"lifecycle\":\"Ec2spot\",\"executionTimeout\":0,\"retries\":1,\"owner\":\"\",\"collaborators\":[]},\"alertModel\":{\"success\":{\"notifyCollaborator\":false,\"alertType\":[]},\"failure\":{\"notifyCollaborator\":false,\"alertType\":[\"dingTalk\"],\"isChecked\":true},\"start\":{\"notifyCollaborator\":false,\"alertType\":[]},\"retry\":{\"notifyCollaborator\":false,\"alertType\":[]}},\"cost\":[{\"value\":100,\"key\":54}]}");
        context.setSourceType("Mysql");
        context.setSourceConfigStr("{\"jdbc_url_params\":\"useUnicode=true&characterEncoding=UTF-8&useSSL=false\",\"password\":\"123456\",\"database\":\"query_editor\",\"port\":3306,\"replication_method\":\"STANDARD\",\"host\":\"127.0.0.1\",\"ssl\":false,\"tunnel_method\":{\"tunnel_method\":\"NO_TUNNEL\"},\"username\":\"query_editor\"}");
        context.setSinkType("Mysql");
        context.setSinkConfigStr("{\"jdbc_url_params\":\"useUnicode=true&characterEncoding=UTF-8&useSSL=false\",\"password\":\"123456\",\"database\":\"query_editor\",\"port\":3306,\"replication_method\":\"STANDARD\",\"host\":\"127.0.0.1\",\"ssl\":false,\"tunnel_method\":{\"tunnel_method\":\"NO_TUNNEL\"},\"username\":\"query_editor\"}");
        return context;
    }

    public static void main(String[] args) {
        String json = "{\"job\":{\"content\":[{\"reader\":{\"parameter\":{\"fieldDelimiter\":\"\\t\",\"defaultFS\":\"hdfs://127.0.0.1:8020\",\"fileType\":\"orc\",\"path\":\"/lakehouse/warehouse/company\",\"encoding\":null,\"column\":[{\"index\":0,\"type\":\"Long\"},{\"index\":1,\"type\":\"String\"},{\"index\":2,\"type\":\"Long\"}],\"hadoopConfig\":{\"dfs.data.transfer.protection\":\"authentication\",\"dfs.client.use.datanode.hostname\":true},\"haveKerberos\":true,\"kerberosKeytabFilePath\":\"/etc/security/keytab/hive.keytab\",\"kerberosPrincipal\":\"hive/127.0.0.1@EXAMPLE.COM\"},\"name\":\"hdfsreader\"},\"writer\":{\"parameter\":{\"loadUrl\":[\"10.32.13.109:8030\"],\"username\":\"root\",\"password\":\"123456\",\"column\":[\"id\",\"name\",\"age\"],\"session\":null,\"preSql\":[\"select 2\"],\"postSql\":null,\"flushInterval\":0,\"loadProps\":null,\"connection\":[{\"table\":[\"company2_pt\"],\"selectedDatabase\":\"demo\",\"jdbcUrl\":\"jdbc:mysql://10.32.13.109:9030/demo\"}]},\"name\":\"doriswriter\"},\"transformer\":[]}],\"setting\":{\"jvm\":\"-Xms2G -Xmx2G\",\"speed\":{\"channel\":2}}}}";
        JSONObject jobJson = JSON.parseObject(json);
        Job job = JSON.parseObject(jobJson.getString("job"), Job.class);
        Map<String, Object> setting = job.getSetting();
        String jvm = (String) setting.getOrDefault("jvm", "-Xms1G -Xmx1G");
        System.out.println(jvm);
//        System.out.println(setting);
//        Context context=initContext();
//        String jobInfo=DataxAdapter.getJobInfo(context);
//        System.out.println(jobInfo);
    }
}
