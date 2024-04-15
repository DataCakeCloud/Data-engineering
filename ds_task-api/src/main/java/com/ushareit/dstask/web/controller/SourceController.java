package com.ushareit.dstask.web.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.pagehelper.PageInfo;
import com.google.api.client.util.Lists;
import com.ushareit.dstask.bean.*;
import com.ushareit.dstask.common.vo.UserGroupVo;
import com.ushareit.dstask.bean.Actor;
import com.ushareit.dstask.bean.ActorDefinition;
import com.ushareit.dstask.bean.ActorShare;
import com.ushareit.dstask.bean.DeleteEntity;
import com.ushareit.dstask.common.vo.ActorUserGroupVo;
import com.ushareit.dstask.constant.ActorTypeEnum;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.service.*;
import com.ushareit.dstask.third.airbyte.AirbyteService;
import com.ushareit.dstask.third.airbyte.common.param.SourceCheckConnection;
import com.ushareit.dstask.third.airbyte.common.param.SourceCreate;
import com.ushareit.dstask.third.airbyte.common.param.SourceSearch;
import com.ushareit.dstask.third.airbyte.common.param.SourceUpdate;
import com.ushareit.dstask.third.airbyte.common.vo.CheckConnectionRead;
import com.ushareit.dstask.third.airbyte.common.vo.SourceDiscoverSchemaRead;
import com.ushareit.dstask.third.airbyte.common.vo.SourceRead;
import com.ushareit.dstask.third.airbyte.config.AirbyteCatalog;
import com.ushareit.dstask.third.airbyte.connector.MySQLConnector;
import com.ushareit.dstask.third.airbyte.json.Jsons;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import com.ushareit.dstask.utils.HttpUtil;
import com.ushareit.dstask.web.factory.flink.param.RuntimeConfig;
import com.ushareit.dstask.web.metadata.lakecat.Lakecatutil;
import com.ushareit.dstask.web.utils.DataCakeTaskConfig;
import com.ushareit.dstask.web.utils.IdUtils;
import com.ushareit.dstask.web.utils.PageUtils;
import com.ushareit.dstask.web.vo.BaseResponse;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.ushareit.dstask.constant.BaseResponseCodeEnum.DATASOURCE_CONFIG_IS_EXIST;

/**
 * @author fengxiao
 * @date 2022/7/22
 */
@Slf4j
@Api(tags = "数据实例定义管理")
@RestController
@RequestMapping("actor/sources")
public class SourceController {

    @Autowired
    private ActorService actorService;
    @Autowired
    private AirbyteService airbyteService;
    @Autowired
    private ActorCatalogService actorCatalogService;
    @Autowired
    private ActorDefinitionService actorDefinitionService;
    @Autowired
    private Lakecatutil lakecatutil;
    @Autowired
    private UserGroupService userGroupService;

    @Autowired
    private OwnerAppService ownerAppService;

    @Resource
    public MySQLConnector mySQLConnector;


    @PostMapping("create")
    public BaseResponse<?> create(@RequestBody @Valid SourceCreate sourceCreate) {
        ActorDefinition actorDefinition = actorDefinitionService.getById(sourceCreate.getSourceDefinitionId());
        JSONObject conConfig = sourceCreate.getConnectionConfiguration();
        List<Actor> repeatedSource = new ArrayList<>();
        String where = String.format(" actor_definition_id = %d and `create_user_group_uuid` = '%s' and",sourceCreate.getSourceDefinitionId(),InfTraceContextHolder.get().getUuid()) ;
        switch (actorDefinition.getName().toLowerCase()) {
            case "mysql":
            case "microsoft sql server (mssql)":
                where += String.format(" JSON_EXTRACT(configuration , '$.host') = '%s' " +
                                "and JSON_EXTRACT(configuration , '$.port')  = %d " +
                                "and JSON_EXTRACT(configuration , '$.database') = '%s'",
                        conConfig.getString("host"),
                        conConfig.getInteger("port"),
                        conConfig.getString("database"));
                repeatedSource = actorService.selectByConfigInfo(where);
                break;
            case "postgres":
                where += String.format(" JSON_EXTRACT(configuration , '$.host') = '%s' " +
                                "and JSON_EXTRACT(configuration , '$.port')  = %d " +
                                "and JSON_EXTRACT(configuration , '$.database') = '%s'" +
                                "and JSON_EXTRACT(configuration , '$.schemas[0]') = '%s'",
                        conConfig.getString("host"),
                        conConfig.getInteger("port"),
                        conConfig.getString("database"),
                        conConfig.getJSONArray("schemas").get(0));
                repeatedSource = actorService.selectByConfigInfo(where);
                break;
            case "mongodb":
                where += String.format(" JSON_EXTRACT(configuration , '$.instance_type.server_addresses') = '%s' " +
                                "and JSON_EXTRACT(configuration , '$.database') = '%s'",
                        conConfig.getJSONObject("instance_type").getString("server_addresses"),
                        conConfig.getString("database"));
                repeatedSource = actorService.selectByConfigInfo(where);
                break;
            case "doris":
                where += String.format(" JSON_EXTRACT(configuration , '$.host') = '%s' " +
                                "and JSON_EXTRACT(configuration , '$.queryport')  = %d " +
                                "and JSON_EXTRACT(configuration , '$.database') = '%s'",
                        conConfig.getString("host"),
                        conConfig.getInteger("queryport"),
                        conConfig.getString("database"));
                repeatedSource = actorService.selectByConfigInfo(where);
                break;
            case "oracle db":
                where += String.format(" JSON_EXTRACT(configuration , '$.host') = '%s' " +
                                "and JSON_EXTRACT(configuration , '$.port')  = %d " +
                                "and JSON_EXTRACT(configuration , '$.schemas[0]') = '%s'",
                        conConfig.getString("host"),
                        conConfig.getInteger("port"),
                        conConfig.getJSONArray("schemas").get(0));
                repeatedSource = actorService.selectByConfigInfo(where);
                break;
            default:
                break;

        }
        if (repeatedSource.size() > 0) {
            Actor actor = repeatedSource.get(0);
            return BaseResponse.error(DATASOURCE_CONFIG_IS_EXIST.name(), String.format("该数据源已被%s用户注册，如需使用请联系负责人！"
                    , actor.getCreateBy()));
        } else {
            actorService.save(sourceCreate.toAddEntity());
        }
        return BaseResponse.success();

    }

    @PostMapping("update")
    public BaseResponse<?> update(@RequestBody @Valid SourceUpdate sourceUpdate) {
        actorService.update(sourceUpdate.toUpdateEntity());
        return BaseResponse.success();
    }

    @GetMapping("delete")
    public BaseResponse<?> delete(@RequestParam("sourceId") Integer sourceId) {
        actorService.delete(sourceId);
        return BaseResponse.success();
    }

    @GetMapping("exist")
    public BaseResponse<?> exist(Integer sourceId, @RequestParam("name") String name) {
        return BaseResponse.success(actorService.checkExist(sourceId, name, ActorTypeEnum.source.name()));
    }

    @GetMapping("get")
    public BaseResponse<SourceRead> get(@RequestParam("sourceId") Integer sourceId) {
        Actor actor = actorService.getById(sourceId);
        if (actor == null) {
            throw new ServiceException(BaseResponseCodeEnum.SYS_ERR.name(), "数据源信息不存在");
        }
        ActorDefinition actorDefinition = actorDefinitionService.getById(actor.getActorDefinitionId());
        Map<Integer, ActorDefinition> definitionMap = new HashMap<>();
        definitionMap.put(actor.getActorDefinitionId(), actorDefinition);
        OwnerApp ownerApp = ownerAppService.getById(actor.getOwnerAppId());
        return BaseResponse.success(new SourceRead(actor, definitionMap, ownerApp));
    }

    @GetMapping("page")
    public BaseResponse<PageInfo<SourceRead>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                                   @RequestParam(defaultValue = "50") Integer pageSize,
                                                   @Valid @ModelAttribute SourceSearch sourceSearch) {

        List<Integer> definitionIds = new ArrayList<>();
        if (StringUtils.isNotBlank(sourceSearch.getSourceName())) {
            Example example = new Example(ActorDefinition.class);
            example.or()
                    .andLike("name", "%" + sourceSearch.getSourceName() + "%")
                    .andEqualTo("deleteStatus", DeleteEntity.NOT_DELETE);

            definitionIds = actorDefinitionService.listByExample(example).stream()
                    .map(ActorDefinition::getId)
                    .collect(Collectors.toList());
        }
        if (!InfTraceContextHolder.get().getUserInfo().isAdmin()){
            sourceSearch.setCurrentUserGroupUuid(InfTraceContextHolder.get().getUuid());
        }
        Example example = sourceSearch.toExample(definitionIds);
        PageInfo<Actor> pageInfo = actorService.listByPage(pageNum, pageSize, example);
        long count = pageInfo.getList().stream().map(Actor::getOwnerAppId).filter(e -> null != e).count();
        Map<Integer, OwnerApp> ownerAppMap;
        if(count > 0){
            ownerAppMap = ownerAppService.mapByIds(pageInfo.getList().stream().map(Actor::getOwnerAppId).filter(e -> null != e));
        } else {
            ownerAppMap = new HashMap<>();
        }
        Map<Integer, ActorDefinition> definitionMap = actorDefinitionService.mapByIds(pageInfo.getList().stream()
                .map(Actor::getActorDefinitionId));
        return BaseResponse.success(PageUtils.map(pageInfo, item -> new SourceRead(item, definitionMap,ownerAppMap.get(item.getOwnerAppId()))));
    }


    @GetMapping("all")
    public BaseResponse<List<SourceRead>> allActor(@RequestParam(name = "name",required = false) String name) {
        Example example = new Example(Actor.class);
        Example.Criteria criteria= example.and();
        criteria.andEqualTo("deleteStatus", DeleteEntity.NOT_DELETE);
        if (StringUtils.isNoneBlank(name)){
            criteria.andEqualTo("name", name);
        }
        PageInfo<Actor> pageInfo = actorService.listByPage(1, 1000, example);
        if (pageInfo!=null&&CollectionUtils.isNotEmpty(pageInfo.getList())){
            Map<Integer, ActorDefinition> definitionMap = actorDefinitionService.mapByIds(pageInfo.getList().stream()
                    .map(Actor::getActorDefinitionId));
            return BaseResponse.success(PageUtils.map(pageInfo, item -> new SourceRead(item, definitionMap)).getList());
            //return BaseResponse.success(pageInfo.getList());
        }
        return BaseResponse.success(Lists.newArrayList());
    }

    @PostMapping("check_connection")
    public BaseResponse<CheckConnectionRead> checkConnection(@Valid @RequestBody SourceCheckConnection checkConnection) {
        ActorDefinition actorDefinition = actorDefinitionService.getById(checkConnection.getSourceDefinitionId());
        JsonNode jsonNode = Jsons.deserialize(checkConnection.getConnectionConfiguration().toJSONString());
        String type = "";
        if (actorDefinition.getName().toLowerCase().contains("mysql")) {
            type = "mysql";
        }
        if (actorDefinition.getName().toLowerCase().contains("clickhouse")) {
            type = "clickhouse";
        }
        if (actorDefinition.getName().toLowerCase().contains("oracle")) {
            type = "oracle";
        }
        if (actorDefinition.getName().toLowerCase().contains("doris")) {
            type = "doris";
        }
        if (actorDefinition.getName().toLowerCase().contains("sql server")) {
            type = "sqlserver";
        }

        if (StringUtils.isNotEmpty(type)) {
            try {
                mySQLConnector.getTableList(jsonNode, type);
                return BaseResponse.success(new CheckConnectionRead(true, null));
            } catch (Exception e) {
                log.error("数据源连接失败" + e.getMessage());
                return BaseResponse.success(new CheckConnectionRead(false, "连接失败"));
            }
        } else {
            return BaseResponse.success(new CheckConnectionRead(true, null));
        }
//
//        if (actorDefinition == null) {
//            throw new ServiceException(BaseResponseCodeEnum.SYS_ERR.name(), "数据源定义信息不存在");
//        }
//
//        try {
//            boolean result = airbyteService.check(String.join(":", actorDefinition.getDockerRepository(),
//                    actorDefinition.getDockerImageTag()), checkConnection.getConnectionConfiguration().toJSONString());
//            return BaseResponse.success(new CheckConnectionRead(result, null));
//        } catch (Exception e) {
//            log.error(e.getMessage(), e);
//            return BaseResponse.success(new CheckConnectionRead(false, e.getMessage()));
//        }
    }

    private void checkConnectionRepeat(SourceCreate sourceCreate, List<Actor> actors) {

        ActorDefinition actorDefinition = actorDefinitionService.getById(sourceCreate.getSourceDefinitionId());
        if (actors == null || actors.isEmpty()) {
            return;
        }
        JsonNode jsonNode = Jsons.deserialize(sourceCreate.getConnectionConfiguration().toJSONString());
        String groups = sourceCreate.getGroups();
        log.info(" ConnectionConfiguration is :" + sourceCreate.getConnectionConfiguration());
        StringBuilder groupInnerKey = new StringBuilder();
        StringBuilder tenantInnerKey = new StringBuilder();
        if (!actorDefinition.getName().toLowerCase().contains("mysql") &&
                !actorDefinition.getName().toLowerCase().contains("clickhouse") &&
                !actorDefinition.getName().toLowerCase().contains("oracle") &&
                !actorDefinition.getName().toLowerCase().contains("doris") &&
                !actorDefinition.getName().toLowerCase().contains("sql server")) {
            return;
        }
        groupInnerKey.append(jsonNode.get("host").asText()).append("@").append(groups);
        if (actorDefinition.getName().toLowerCase().contains("oracle")) {
            groupInnerKey.append("@").append(jsonNode.get("sid").asText());
        } else {
            groupInnerKey.append("@").append(jsonNode.get("database").asText());
        }

        tenantInnerKey.append(jsonNode.get("host").asText());
        if (actorDefinition.getName().toLowerCase().contains("oracle")) {
            tenantInnerKey.append("@").append(jsonNode.get("sid").asText())
                    .append("@").append(jsonNode.get("port").asText());
        }
        if (actorDefinition.getName().toLowerCase().contains("doris")) {
            tenantInnerKey.append("@").append(jsonNode.get("database").asText())
                    .append("@").append(jsonNode.get("queryport").asText())
                    .append("@").append(jsonNode.get("httpport").asText());
        } else {
            tenantInnerKey.append("@").append(jsonNode.get("database").asText())
                    .append("@").append(jsonNode.get("port").asText());
        }

        log.info(" groupInnerKey is :" + groupInnerKey);
        log.info(" tenantInnerKey is :" + tenantInnerKey);
        List<Actor> collect = actors.stream().map(data -> {
            String configuration = data.getConfiguration();
            JSONObject configurationObject = JSON.parseObject(configuration);
            String host = configurationObject.getString("host");
            String group = data.getGroups();
            String database ;
            String port;
            if (actorDefinition.getName().toLowerCase().contains("oracle")) {
                groupInnerKey.append("@").append(jsonNode.get("sid").asText());
                database = configurationObject.getString("sid");
            } else {
                database = configurationObject.getString("database");
            }
            if (actorDefinition.getName().toLowerCase().contains("doris")) {
                port = configurationObject.getString("queryport") + "@" + configurationObject.getString("httpport");
            } else {
                port = configurationObject.getString("host");
            }
            StringBuilder dbGroupOnlyKey = new StringBuilder();
            dbGroupOnlyKey.append(host).append("@").append(group).append("@").append(database);
            log.info(" dbGroupOnlyKey is :" + dbGroupOnlyKey);
            if (groupInnerKey.toString().equals(dbGroupOnlyKey.toString())) {
                throw new ServiceException(DATASOURCE_CONFIG_IS_EXIST, String.format("%s已注册该数据源:%s，如需使用请联系负责人%s"
                        , data.getCreateBy(), data.getName(), data.getCreateBy()));
            }

            StringBuilder dbTenantOnlyKey = new StringBuilder();
            dbTenantOnlyKey.append(host).append("@").append(database).append("@").append(port);
            log.info(" dbTenantOnlyKey is :" + dbTenantOnlyKey);
            if (tenantInnerKey.toString().equals(dbTenantOnlyKey.toString())) {
                throw new ServiceException(DATASOURCE_CONFIG_IS_EXIST, String.format("%s已注册该数据源:%s，如需使用请联系负责人%s"
                        , data.getCreateBy(), data.getName(), data.getCreateBy()));
            }
            return data;
        }).collect(Collectors.toList());
    }

    @GetMapping("discover_schema")
    public BaseResponse<SourceDiscoverSchemaRead> discoverSchema(@RequestParam("sourceId") Integer sourceId) {
        Actor actor = actorService.getById(sourceId);
        if (actor == null) {
            throw new ServiceException(BaseResponseCodeEnum.SYS_ERR.name(), "数据源信息不存在");
        }

        ActorDefinition actorDefinition = actorDefinitionService.getById(actor.getActorDefinitionId());
        if (actorDefinition == null) {
            throw new ServiceException(BaseResponseCodeEnum.SYS_ERR.name(), "数据源定义信息不存在");
        }

        try {
            AirbyteCatalog airbyteCatalog = airbyteService.discover(String.join(":", actorDefinition.getDockerRepository(),
                    actorDefinition.getDockerImageTag()), actor.getConfiguration());
            actorCatalogService.saveOrUpdate(sourceId, airbyteCatalog);

            return BaseResponse.success(new SourceDiscoverSchemaRead(sourceId, airbyteCatalog));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @GetMapping("document/{instance}")
    public BaseResponse<?> doc(@PathVariable("instance") String instance) {
        BaseResponse response = HttpUtil.get(String.format("https://demo.airbyte.io/docs/integrations/sources/%s", instance));
        if (response.getData().toString().startsWith("<!DOCTYPE html>")) {
            response = HttpUtil.get(String.format("https://demo.airbyte.io/docs/integrations/destinations/%s", instance));
        }
        return response;
    }


    @RequestMapping("/addShare")
    public BaseResponse addActorShare(@RequestBody ActorShare actorShare){
        actorService.addActorShare(actorShare);
        return BaseResponse.success();
    }

    @RequestMapping("/doAuthEdit")
    public BaseResponse doAuthEdit(@RequestParam(name = "id") String id){
        return BaseResponse.success(actorService.doAuthEdit(id));
    }

    @RequestMapping("/deleteShare")
    public BaseResponse deleteShare(Integer id){
        actorService.deleteActorShare(id);
        return BaseResponse.success();
    }

    @RequestMapping("/listShare")
    public BaseResponse listShare(Integer actorId){
        return BaseResponse.success(actorService.listActorShare(actorId));
    }

    @RequestMapping("/listUserGroup")
    public BaseResponse listUserGroupPrivilege(Integer id){
        return BaseResponse.success(actorService.listUserGroupPrivilege(id));
    }

    @RequestMapping("/saveUserGroup")
    public BaseResponse saveUserGroup(@RequestBody ActorUserGroupVo actorUserGroupVo){
        actorService.saveActorUserGroupPrivileges(actorUserGroupVo);
        return BaseResponse.success();
    }

    public static void main(String[] args) {
        System.out.println(IdUtils.getLenthId(8));
    }

}
