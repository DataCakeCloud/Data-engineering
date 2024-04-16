package com.ushareit.dstask.web.controller;

import com.ushareit.dstask.bean.Actor;
import com.ushareit.dstask.bean.ActorDefinition;
import com.ushareit.dstask.bean.MetaData;
import com.ushareit.dstask.common.param.MetaDataParam;
import com.ushareit.dstask.common.param.StorageSchemaParam;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.service.*;
import com.ushareit.dstask.third.airbyte.AirbyteService;
import com.ushareit.dstask.third.airbyte.common.vo.CheckConnectionRead;
import com.ushareit.dstask.third.airbyte.config.AirbyteCatalog;
import com.ushareit.dstask.web.ddl.metadata.Table;
import com.ushareit.dstask.web.utils.CommonUtil;
import com.ushareit.dstask.web.vo.BaseResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.*;

/**
 * @author: xuebotao
 * @create: 2022-08-03
 */

@Slf4j
@Api(tags = "元数据获取信息")
@RestController
@RequestMapping("/metadata")
public class MetaDataController extends BaseBusinessController<Table> {

    @Autowired
    private MetaDataService metaDataService;

    @Autowired
    private ActorService actorService;
    @Autowired
    private AirbyteService airbyteService;
    @Autowired
    private ActorCatalogService actorCatalogService;
    @Autowired
    private ActorDefinitionService actorDefinitionService;

    @Override
    public BaseService<Table> getBaseService() {
        return metaDataService;
    }

    @Resource
    private CacheManager cacheManager;


    @ApiOperation(value = "元数据查询")
    @ApiResponses({@ApiResponse(code = 200, response = BaseResponse.class, message = "成功")})
    @PostMapping("/search")
    public BaseResponse search(@RequestBody @Valid MetaDataParam metaDataParam) {
        List<Table> search;
        if (StringUtils.isEmpty(metaDataParam.getActorId())) {
            search =  metaDataService.search(metaDataParam);
        } else {
            if (metaDataParam.getClearCache()) {
                metaDataService.clearCache(metaDataParam.getType(), metaDataParam.getActorId(), metaDataParam.getDb(), metaDataParam.getTable(), metaDataParam.getSourceParam());
            }
            search = metaDataService.search(metaDataParam, metaDataParam.getType(), metaDataParam.getActorId(), metaDataParam.getDb(), metaDataParam.getTable(), metaDataParam.getSourceParam());
        }
        if (StringUtils.isNotEmpty(metaDataParam.getQualifiedName()) ||
                StringUtils.isNotEmpty(metaDataParam.getGuid()) || StringUtils.isNotEmpty(metaDataParam.getTable())) {
            return BaseResponse.success(search.stream().findFirst());
        }
        return BaseResponse.success(new MetaData(search, search.size()));
    }
    @ApiOperation(value = "元数据查询")
    @ApiResponses({@ApiResponse(code = 200, response = BaseResponse.class, message = "成功")})
    @PostMapping("/getStorageSchema")
    public BaseResponse getStorageSchema(@RequestBody @Valid StorageSchemaParam storageSchemaParm) {
        Actor actor = actorService.getById(storageSchemaParm.getActorId());
        List<Map<String, String>> storageSchemaDetail = metaDataService.getStorageSchemaDetail(actor,storageSchemaParm.getType(),
                storageSchemaParm.getPath(),
                storageSchemaParm.getFileType(),
                storageSchemaParm.getFieldDelimiter());
        return BaseResponse.success(storageSchemaDetail);
    }

    @ApiOperation(value = "数据预览")
    @ApiResponses({@ApiResponse(code = 200, response = BaseResponse.class, message = "成功")})
    @PostMapping("/preview")
    public BaseResponse preview(@RequestBody @Valid MetaDataParam metaDataParam) {
        Map results = new HashMap<>();
        results.put("results", metaDataService.getTableSample(metaDataParam));
        return BaseResponse.success(results);
    }

    @ApiOperation(value = "校验DB连接性")
    @ApiResponses({@ApiResponse(code = 200, response = BaseResponse.class, message = "成功")})
    @PostMapping("/checkConnection")
    public BaseResponse checkConnection(@RequestBody @Valid MetaDataParam metaDataParam) {
        Boolean result;
        try {
            result = metaDataService.checkConnection(metaDataParam);
        } catch (Exception e) {
            log.error("failed to check connection:" + CommonUtil.printStackTraceToString(e));
            return BaseResponse.success(new CheckConnectionRead(false, "连接失败"));
        }
        if (result) {
            return BaseResponse.success(new CheckConnectionRead(result, null));
        }
        return BaseResponse.success(new CheckConnectionRead(result, "连接失败"));
    }

    @ApiOperation(value = "创建DB表")
    @ApiResponses({@ApiResponse(code = 200, response = BaseResponse.class, message = "成功")})
    @PostMapping("/createTable")
    public BaseResponse createTable(@RequestBody @Valid MetaDataParam metaDataParam) {
        Map result = new HashMap();
        result.put("results", metaDataService.createTable(metaDataParam));
        return BaseResponse.success();
    }

    @ApiOperation(value = "元数据DDL获取")
    @PostMapping("/getddl")
    public BaseResponse getDdl(@RequestBody @Valid MetaDataParam metaDataParam) {
        String ddl = "";
        try {
            ddl = metaDataService.getDdl(metaDataParam);
        } catch (ServiceException e) {
            return BaseResponse.error(e.getCodeStr(), e.getMessage());
        } catch (Exception e) {
            return BaseResponse.error(BaseResponseCodeEnum.SYS_ERR, CommonUtil.printStackTraceToString(e));
        }
        return BaseResponse.success(BaseResponseCodeEnum.SUCCESS, ddl);
    }

    @ApiOperation(value = "获取DDL")
    @PostMapping("/getdispalyddl")
    public BaseResponse getDispalyDdl(@RequestBody @Valid MetaDataParam metaDataParam) {
        Map<String, Object> dispalyddl;
        try {
            dispalyddl = metaDataService.getDisplayDdl(metaDataParam);
        } catch (ServiceException e) {
            return BaseResponse.error(e.getCodeStr(), e.getMessage());
        } catch (Exception e) {
            return BaseResponse.error(BaseResponseCodeEnum.SYS_ERR, CommonUtil.printStackTraceToString(e));
        }
        return BaseResponse.success(BaseResponseCodeEnum.SUCCESS, dispalyddl);
    }

    @ApiOperation("清理缓存-用于hive的元数据适配--+--与Airbyte元数据信息发现")
    @GetMapping(value = "/clearCache")
    public BaseResponse<?> clearCache(@RequestParam("actorId") Integer actorId,
                                      @RequestParam("type") String type) {
        if (StringUtils.isNotEmpty(type) && !type.equals("hive")) {
            return discoverSchema(actorId);
        }
        String name = getCurrentUser().getUserName();

        CaffeineCache caffeineCache = (CaffeineCache) cacheManager.getCache("metadata");
        com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache = caffeineCache.getNativeCache();
        Set<Object> cacheMetadataKey = nativeCache.asMap().keySet();
        for (Object metadataKey : cacheMetadataKey) {
            String metadataKeyString = metadataKey.toString();
            if (metadataKeyString.contains(name)) {
                nativeCache.invalidate(metadataKey);
            }
        }
        return BaseResponse.success(BaseResponseCodeEnum.SUCCESS);
    }

    @ApiOperation("Airbyte元数据信息发现")
    @GetMapping("/discover")
    public BaseResponse discoverSchema(@RequestParam("actorId") Integer actorId) {
        Actor actor = actorService.getById(actorId);
        if (actor == null) {
            throw new ServiceException(BaseResponseCodeEnum.SYS_ERR.name(), "数据源信息不存在");
        }

        CaffeineCache caffeineCache = (CaffeineCache) cacheManager.getCache("metadata");
        com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache = caffeineCache.getNativeCache();
        Set<Object> cacheMetadataKey = nativeCache.asMap().keySet();
        for (Object metadataKey : cacheMetadataKey) {
            String metadataKeyString = metadataKey.toString();
            if (metadataKeyString.contains("actor_id-" + actorId)) {
                nativeCache.invalidate(metadataKey);
            }
        }

        ActorDefinition actorDefinition = actorDefinitionService.getById(actor.getActorDefinitionId());
        if (actorDefinition == null) {
            throw new ServiceException(BaseResponseCodeEnum.SYS_ERR.name(), "数据源定义信息不存在");
        }

        try {
            AirbyteCatalog airbyteCatalog = airbyteService.discover(String.join(":", actorDefinition.getDockerRepository(),
                    actorDefinition.getDockerImageTag()), actor.getConfiguration());
            actorCatalogService.saveOrUpdate(actorId, airbyteCatalog);

            return BaseResponse.success(BaseResponseCodeEnum.SUCCESS);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }


}