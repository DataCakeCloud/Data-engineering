package com.ushareit.dstask.web.metadata.airbyte;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ushareit.dstask.bean.Actor;
import com.ushareit.dstask.bean.UserGroup;
import com.ushareit.dstask.common.param.MetaDataParam;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.constant.DsTaskConstant;
import com.ushareit.dstask.constant.SourceTypeEnum;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.service.ActorDefinitionService;
import com.ushareit.dstask.service.ActorService;
import com.ushareit.dstask.service.impl.TaskServiceImpl;
import com.ushareit.dstask.service.impl.UserGroupServiceImpl;
import com.ushareit.dstask.web.ddl.metadata.Column;
import com.ushareit.dstask.web.ddl.metadata.Table;
import com.ushareit.dstask.web.utils.EncryptUtil;
import com.ushareit.engine.param.ActorProvider;
import com.ushareit.engine.param.Catalog;
import com.ushareit.engine.param.RuntimeConfig;
import com.ushareit.engine.seatunnel.util.DataSourceUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.ListTopicsOptions;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.KafkaFuture;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class KafkaMetaData extends AirByteMetaData {

    private static KafkaMetaData seatunnelMetaData;

    @Resource
    public ActorService actorService;
    @Resource
    public TaskServiceImpl taskService;
    @Resource
    public UserGroupServiceImpl userGroupService;

    @Resource
    public ActorDefinitionService actorDefinitionService;

    @PostConstruct
    public void init() {
        seatunnelMetaData = this;
        seatunnelMetaData.actorDefinitionService = this.actorDefinitionService;
        seatunnelMetaData.actorService = this.actorService;
        seatunnelMetaData.userGroupService = this.userGroupService;
        seatunnelMetaData.taskService = this.taskService;
    }

    public KafkaMetaData(MetaDataParam metaDataParam) {
        super(metaDataParam);
    }


    @Override
    public List<Table> search(MetaDataParam metaDataParam) {
        List<Actor> actorByType;
        if (StringUtils.isEmpty(metaDataParam.getActorId())) {
            actorByType = seatunnelMetaData.actorDefinitionService.getActorByTypeAndRegion(metaDataParam.getType(), metaDataParam.getRegion());
            if (actorByType.isEmpty()) {
                return new ArrayList<>();
            }
        } else {
            //拿这actor 和 table 查表
            Actor byId = seatunnelMetaData.actorService.getById(metaDataParam.getActorId());
            if (StringUtils.isEmpty(metaDataParam.getDb()) && StringUtils.isEmpty(metaDataParam.getTable()) && StringUtils.isEmpty(metaDataParam.getSourceParam())) {
                //返回所有的topic 把topic当库
                return getDataBase(byId);
            }
            if (StringUtils.isNotEmpty(metaDataParam.getDb()) || StringUtils.isNotEmpty(metaDataParam.getSourceParam())) {
                //查topic的scheam
                return getTableSchema(metaDataParam, byId);
            }
            return new ArrayList<>();
        }
        return getSourceList(metaDataParam, actorByType);
    }


    public Boolean checkConnection(MetaDataParam metaDataParam) {
        Properties config = new Properties();
        JSONObject connectionConfiguration = metaDataParam.getConnectionConfiguration();
        String bootstrapServers = connectionConfiguration.getString("bootstrap_servers");
        JSONObject protocol = connectionConfiguration.getJSONObject("protocol");
        if (protocol != null) {
            String security_protocol = protocol.getString("security_protocol");
            if (StringUtils.isNotEmpty(security_protocol) && security_protocol.equals("SASL_PLAINTEXT")) {
                config.put("security.protocol", security_protocol);
                config.put("sasl.mechanism", "SCRAM-SHA-512");
                String sasl_jaas_config = protocol.getString("sasl_jaas_config");
                if (StringUtils.isNotEmpty(sasl_jaas_config)) {
                    config.put("sasl.jaas.config", sasl_jaas_config);
                }
            }
        }
        config.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        log.info("connection bootstrapServers is :" + bootstrapServers);
        log.info("properties is :" + config);
        AdminClient admin = null;
        try {
            admin = AdminClient.create(config);
            ListTopicsOptions options = new ListTopicsOptions();
            options.listInternal(false);
            ListTopicsResult topics = admin.listTopics(options);
            KafkaFuture<Set<String>> names = topics.names();
            Set<String> topicNames = names.get();
            List<String> list = new ArrayList<>(topicNames);
            return true;
        } catch (Exception e) {
            log.info("connection fail reson is :" + e.getMessage());
            return false;
        } finally {
            if (admin != null) {
                admin.close();
            }
        }
    }


    /**
     * 获取表信息
     */
    public List<Table> getTableSchema(MetaDataParam metaDataParam, Actor actor) {
        String configuration = actor.getConfiguration();
        JSONObject con = JSON.parseObject(configuration);
        String topicName = metaDataParam.getDb();
//        String protocol = con.getString("protocol");
//        if(StringUtils.isEmpty(protocol) || !protocol.equals("CANAL_JSON")){
//           return new ArrayList<>();
//        }
        log.info("connection bootstrapServers is :" + con.getString("bootstrap_servers"));
        log.info("connection topicName is :" + metaDataParam.getDb());
        Properties props = new Properties();
        props.put("bootstrap.servers", con.getString("bootstrap_servers"));
        props.put("group.id", "datacake-seatunnel-group1");
        JSONObject protocol = con.getJSONObject("protocol");
        if (protocol != null) {
            String security_protocol = protocol.getString("security_protocol");
            if (StringUtils.isNotEmpty(security_protocol) && security_protocol.equals("SASL_PLAINTEXT")) {
                props.put("security.protocol", security_protocol);
                props.put("sasl.mechanism", "SCRAM-SHA-512");
                String sasl_jaas_config = protocol.getString("sasl_jaas_config");
                if (StringUtils.isNotEmpty(sasl_jaas_config)) {
                    props.put("sasl.jaas.config", sasl_jaas_config);
                }
            }
        }
        props.put("auto.offset.reset", "earliest");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        log.info("properties is :" + props.toString());
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(topicName));
        int i = 0;
        int j = 0;
        List<Table> resultList = new ArrayList<>();
        Table resTable = new Table();
        resultList.add(resTable);
        List<Column> columns = new ArrayList<>();
        resTable.setColumns(columns);
        try {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                j++;
                if (j > 100) {
                    return resultList;
                }
                for (ConsumerRecord<String, String> record : records) {
                    if (i > 0) {
                        return resultList;
                    }
                    String value = record.value();
                    JSONObject jsonObject = JSON.parseObject(value);
                    if (!jsonObject.containsKey("mysqlType")) {
                        return new ArrayList<>();
                    }
                    String sqlType = jsonObject.getString("mysqlType");
                    Map mapTypes = JSON.parseObject(sqlType);
                    for (Object obj : mapTypes.keySet()) {
                        Column column = new Column();
                        column.setName(obj.toString());
                        column.setType(mapTypes.get(obj).toString().toUpperCase());
                        columns.add(column);
                    }
                    i++;
                }
            }
        } catch (Exception e) {
            log.info("get fail reson is :" + e.getMessage());
            return new ArrayList<>();
        } finally {
            consumer.close();
        }
    }


    /**
     * 获取数据库集合
     */
    public List<Table> getDataBase(Actor actor) {
        Properties config = new Properties();
        String configuration = actor.getConfiguration();
        JSONObject connectionConfiguration = JSON.parseObject(configuration);
        String bootstrapServers = connectionConfiguration.getString("bootstrap_servers");
        JSONObject protocol = connectionConfiguration.getJSONObject("protocol");
        if (protocol != null) {
            String security_protocol = protocol.getString("security_protocol");
            if (StringUtils.isNotEmpty(security_protocol) && security_protocol.equals("SASL_PLAINTEXT")) {
                config.put("security.protocol", security_protocol);
                config.put("sasl.mechanism", "SCRAM-SHA-512");
                String sasl_jaas_config = protocol.getString("sasl_jaas_config");
                if (StringUtils.isNotEmpty(sasl_jaas_config)) {
                    config.put("sasl.jaas.config", sasl_jaas_config);
                }
            }
        }
        log.info("connection bootstrapServers is :" + bootstrapServers);
        log.info("properties is :" + config);
        config.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        // 创建AdminClient实例
        AdminClient admin = null;
        try {
            admin = AdminClient.create(config);
            ListTopicsOptions options = new ListTopicsOptions();
            options.listInternal(false);
            ListTopicsResult topics = admin.listTopics(options);
            KafkaFuture<Set<String>> names = topics.names();
            Set<String> topicNames = names.get();
            List<Table> collect = topicNames.stream().map(data -> {
                Table table = new Table();
                table.setName(data);
                return table;
            }).collect(Collectors.toList());
            return collect;
        } catch (Exception e) {
            log.info("connection fail reson is :" + e.getMessage());
            return new ArrayList<>();
        } finally {
            if (admin != null) {
                admin.close();
            }
        }
    }

    /**
     * 获取source
     */
    public List<Table> getSourceList(MetaDataParam metaDataParam, List<Actor> actorByType) {
        return actorByType.stream().filter(data -> {
            UserGroup userGroup = seatunnelMetaData.userGroupService.selectUserGroupByUuid(data.getCreateUserGroupUuid());
            if (userGroup == null) {
                return false;
            }
            if (userGroup.getDeleteStatus() == 0) {
                return true;
            } else {
                return false;
            }
        }).map(data -> {
            Table table = new Table();
            table.setName(data.getName());
            table.setActorId(data.getId().toString());
            UserGroup userGroup = seatunnelMetaData.userGroupService.selectUserGroupById(Integer.parseInt(data.getGroups()));
            table.setUserGroupName(userGroup.getName());
            return table;
        }).collect(Collectors.toList());
    }


}
