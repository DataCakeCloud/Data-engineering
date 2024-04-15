package com.ushareit.dstask.web.metadata;

import com.ushareit.dstask.bean.ActorDefinition;
import com.ushareit.dstask.common.param.MetaDataParam;
import com.ushareit.dstask.mapper.ActorDefinitionMapper;
import com.ushareit.dstask.web.metadata.sources.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DataSourceManager {

    @Autowired
    private ActorDefinitionMapper actorDefinitionMapper;

    public Boolean checkConnection(MetaDataParam metaDataParam) {
        AbstractDataSource dataSourceManager = getByType(metaDataParam.getSourceDefinitionId());
        if(dataSourceManager == null) return true;
        return dataSourceManager.checkConnection(metaDataParam);
    }

    public AbstractDataSource getByType(Integer sourceDefinitionId){
        ActorDefinition actorDefinition = actorDefinitionMapper.selectByPrimaryKey(sourceDefinitionId);
        String name = actorDefinition.getName().toLowerCase();
        return getByType(name);
    }

    public static AbstractDataSource getByType(String name){
        if (name.contains("mysql")) {
            return new MysqlDataSource();
        } else if (name.contains("clickhouse")) {

        } else if (name.contains("oracle")) {
            return new OracleDataSource();
        } else if (name.contains("doris")) {
            return new DorisDataSource();
        } else if (name.contains("sql server")) {
            return new SqlServerDataSource();
        } else if (name.contains("postgres")) {
            return new PostgreSQLDataSource();
        } else if(name.contains("s3")){
            return new S3DataSource();
        }else if(name.contains("oss")){
            return new OSSDataSource();
        }else if(name.contains("hana")){
            return new HanaDataSource();
        }else if(name.contains("mongodb")){
            return new MongodbDataSource();
        }
        return null;
    }

}
