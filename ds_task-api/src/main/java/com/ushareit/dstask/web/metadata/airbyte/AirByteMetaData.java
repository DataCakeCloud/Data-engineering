package com.ushareit.dstask.web.metadata.airbyte;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.pagehelper.PageInfo;
import com.nimbusds.oauth2.sdk.util.CollectionUtils;
import com.ushareit.dstask.bean.Actor;
import com.ushareit.dstask.bean.ActorDefinition;
import com.ushareit.dstask.bean.DeleteEntity;
import com.ushareit.dstask.common.param.MetaDataParam;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.constant.SourceTypeEnum;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.service.ActorDefinitionService;
import com.ushareit.dstask.service.ActorService;
import com.ushareit.dstask.third.airbyte.common.param.SourceSearch;
import com.ushareit.dstask.third.airbyte.json.Jsons;
import com.ushareit.dstask.third.cloudresource.CloudResourcesService;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import com.ushareit.dstask.web.ddl.metadata.Table;
import com.ushareit.dstask.web.metadata.AbstractMetaData;
import com.ushareit.dstask.web.metadata.metis.MetisMetaData;
import com.ushareit.dstask.web.metadata.olap.HiveMetaData;
import com.ushareit.dstask.web.metadata.olap.HiveMetaDataV2;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class AirByteMetaData extends AbstractMetaData {

    @Resource
    private HiveMetaData hiveMetaData;

    @Resource
    private HiveMetaDataV2 hiveMetaDataV2;
    @Resource
    private MetisMetaData metisMetaData;
    @Resource
    private ActorService actorService;
    @Resource
    private CloudResourcesService cloudResourcesService;

    @Autowired
    private ActorDefinitionService actorDefinitionService;

    private static AirByteMetaData airByteMetaData;

    @PostConstruct
    public void init() {
        airByteMetaData = this;
    }

    public AirByteMetaData(MetaDataParam metaDataParam) {
        super(metaDataParam);
    }

    @Override
    public List<Table> search(MetaDataParam metaDataParam) {
        if (StringUtils.isEmpty(metaDataParam.getRegion()) || StringUtils.isEmpty(metaDataParam.getType())) {
            throw new ServiceException(BaseResponseCodeEnum.CLI_PARAM_ILLEGAL);
        }
        AirByteMetaData metaDataType = getMetaDataType(metaDataParam.getType(), metaDataParam.getRegion());
        List<Table> search = metaDataType.search(metaDataParam);

//        //如果没有选中库表 先进行权限过滤
//        if (StringUtils.isEmpty(metaDataParam.getActorId()) &&
//                !metaDataParam.getType().equals("hive") &&
//                !metaDataParam.getType().equals("kafka") &&
//                !metaDataParam.getType().equals("metis")) {
//            List<String> uuids = airByteMetaData.actorService.selectActorIdByShareId();
//            List<Integer> definitionIds = new ArrayList<>();
//            SourceSearch sourceSearch = new SourceSearch();
//            if (StringUtils.isBlank(sourceSearch.getCreateBy())) {
//                sourceSearch.setCreateBy(InfTraceContextHolder.get().getUserName());
//            }
//            Example example = sourceSearch.toExample(definitionIds);
//            if (CollectionUtils.isNotEmpty(uuids)) {
//                Example.Criteria userOrGroupCriteria = example.or();
//                userOrGroupCriteria.andEqualTo("deleteStatus", DeleteEntity.NOT_DELETE);
//                userOrGroupCriteria.andIn("uuid", uuids);
//            }
//            PageInfo<Actor> pageInfo = airByteMetaData.actorService.listByPage(1, 10000, example);
//            List<Actor> list = pageInfo.getList();
//            if (list.isEmpty()) {
//                return new ArrayList<>();
//            }
//            List<String> collect = list.stream().map(data -> data.getId().toString()).collect(Collectors.toList());
//            return search.stream().filter(data -> collect.contains(data.getActorId())).collect(Collectors.toList());
//        }
        return search;
    }

    @Override
    public List<Map<String, String>> getTableSample(MetaDataParam metaDataParam) {
        if (StringUtils.isEmpty(metaDataParam.getRegion()) || StringUtils.isEmpty(metaDataParam.getType())) {
            throw new ServiceException(BaseResponseCodeEnum.CLI_PARAM_ILLEGAL);
        }
        AirByteMetaData metaDataType = getMetaDataType(metaDataParam.getType(), metaDataParam.getRegion());
        return metaDataType.getTableSample(metaDataParam);
    }

    private String getDBType(Integer sourceDefinitionId) {
        ActorDefinition actorDefinition = airByteMetaData.actorDefinitionService.getById(sourceDefinitionId);
        String type = "";
        String name = actorDefinition.getName().toLowerCase();
        if (name.contains("mysql")) {
            type = "mysql";
        } else if (name.contains("clickhouse")) {
            type = "clickhouse";
        } else if (name.contains("oracle")) {
            type = "oracle";
        } else if (name.contains("doris")) {
            type = "doris";
        } else if (name.contains("sql server")) {
            type = "sqlserver";
        } else if (name.contains("postgres")) {
            type = "postgres";
        } else if(name.contains("s3")){
            type = "s3";
        } else if (name.contains("kafka")) {
            type = "kafka";
        }
        return type;
    }

    @Override
    public Boolean checkConnection(MetaDataParam metadataParam) {
        AirByteMetaData metaDataType = null;
        Integer sourceDefinitionId = metadataParam.getSourceDefinitionId();
        if (sourceDefinitionId != null) {
            String type = getDBType(sourceDefinitionId);
            if (type.equals("")) {
                return true;
            }
            metadataParam.setType(type);
            metaDataType = getMetaDataType(type, null);
        } else {
            if (StringUtils.isEmpty(metaDataParam.getRegion()) || StringUtils.isEmpty(metaDataParam.getType())) {
                throw new ServiceException(BaseResponseCodeEnum.CLI_PARAM_ILLEGAL);
            }
            metaDataType = getMetaDataType(metaDataParam.getType(), metaDataParam.getRegion());
        }

        return metaDataType.checkConnection(metadataParam);
    }

    @Override
    public Boolean createTable(MetaDataParam metaDataParam) {
        if (StringUtils.isEmpty(metaDataParam.getRegion()) || StringUtils.isEmpty(metaDataParam.getType())
                || StringUtils.isEmpty(metaDataParam.getCreateTableSql())) {
            throw new ServiceException(BaseResponseCodeEnum.CLI_PARAM_ILLEGAL);
        }
        AirByteMetaData metaDataType = getMetaDataType(metaDataParam.getType(), metaDataParam.getRegion());
        return metaDataType.createTable(metaDataParam);
    }

    @Override
    public Table getDdl(MetaDataParam metaDataParam) {
        AirByteMetaData metaDataType = getMetaDataType(metaDataParam.getType(),metaDataParam.getRegion());
        return metaDataType.getDdl(metaDataParam);
    }


    public AirByteMetaData getMetaDataType(String type, String region) {
        switch (SourceTypeEnum.SourceTypeMap.get(type)) {
            case hive_table:
                return airByteMetaData.hiveMetaDataV2;
            case rdbms_table:
            case clickhouse:
            case sql_server:
            case oracle:
            case doris:
            case starrocks:
            case postgres:
            case hologres:
            case s3:
            case oss:
            case ks3:
            case hdfs:
            case mongodb:
            case hana:
                return new SeatunnelMetaData(metaDataParam);
            case kafka_topic:
//                return new JdbcMetaData(metaDataParam);
                return new KafkaMetaData(metaDataParam);
            case metis:
                return airByteMetaData.metisMetaData;
            default:
                throw new ServiceException(BaseResponseCodeEnum.CLI_PARAM_ILLEGAL, "Can not support this type:" + metaDataParam.getType());
        }
    }

}
