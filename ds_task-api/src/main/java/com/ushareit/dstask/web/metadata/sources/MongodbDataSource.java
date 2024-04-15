package com.ushareit.dstask.web.metadata.sources;

import com.alibaba.fastjson.JSONObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.ClientSession;
import com.mongodb.client.ListCollectionsIterable;
import com.mongodb.client.ListDatabasesIterable;
import com.ushareit.dstask.common.param.MetaDataParam;
import com.ushareit.dstask.web.metadata.AbstractDataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;

@Slf4j
public class MongodbDataSource extends AbstractDataSource {

    @Override
    public Boolean checkConnection(MetaDataParam metaDataParam) {
        MongoClient mongoClient = null;
        try {
            JSONObject connectConfig = metaDataParam.getConnectionConfiguration();
            String user = connectConfig.getString("user");
            String password = connectConfig.getString("password");
            String database = connectConfig.getString("database");
            JSONObject instanceType = connectConfig.getJSONObject("instance_type");
            String serverAddresses = instanceType.getString("server_addresses");

            if(connectConfig.containsKey("urlParam") && StringUtils.isNotEmpty(connectConfig.getString("urlParam"))) {
                MongoClientURI mongoClientURI = new MongoClientURI("mongodb://"+user+":"+password+"@"+serverAddresses+"/"+database+"?" + connectConfig.getString("urlParam"));
                mongoClient = new MongoClient(mongoClientURI);
            }else {
                MongoClientURI mongoClientURI = new MongoClientURI("mongodb://"+user+":"+password+"@"+serverAddresses+"/"+database);
                mongoClient = new MongoClient(mongoClientURI);
            }
            ListCollectionsIterable<Document> documents = mongoClient.getDatabase(database)
                    .listCollections();
            int i = 0;
            for (Document document : documents) {
                if(i++ > 0) {
                    break;
                }
//                System.out.println(document.toJson());
            }
            log.error("测试Mongo链接成功：{}",connectConfig);
        }catch (Exception e){
            e.printStackTrace();
            log.error("测试Mongo链接失败：{}",e);
            throw new RuntimeException(e.getMessage());
        }finally {
            if(mongoClient != null) mongoClient.close();
        }
        return true;
    }

}
