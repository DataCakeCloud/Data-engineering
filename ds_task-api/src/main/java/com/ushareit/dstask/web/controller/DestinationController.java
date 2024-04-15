package com.ushareit.dstask.web.controller;

import com.github.pagehelper.PageInfo;
import com.ushareit.dstask.bean.Actor;
import com.ushareit.dstask.bean.ActorDefinition;
import com.ushareit.dstask.bean.DeleteEntity;
import com.ushareit.dstask.constant.ActorTypeEnum;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.service.ActorDefinitionService;
import com.ushareit.dstask.service.ActorService;
import com.ushareit.dstask.third.airbyte.AirbyteService;
import com.ushareit.dstask.third.airbyte.common.param.DestinationCheckConnection;
import com.ushareit.dstask.third.airbyte.common.param.DestinationCreate;
import com.ushareit.dstask.third.airbyte.common.param.DestinationSearch;
import com.ushareit.dstask.third.airbyte.common.param.DestinationUpdate;
import com.ushareit.dstask.third.airbyte.common.vo.CheckConnectionRead;
import com.ushareit.dstask.third.airbyte.common.vo.DestinationRead;
import com.ushareit.dstask.utils.HttpUtil;
import com.ushareit.dstask.web.utils.PageUtils;
import com.ushareit.dstask.web.vo.BaseResponse;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
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

/**
 * @author fengxiao
 * @date 2022/7/22
 */
@Slf4j
@Api(tags = "数据实例定义管理")
@RestController
@RequestMapping("actor/destinations")
public class DestinationController {

    @Autowired
    private ActorService actorService;
    @Autowired
    private AirbyteService airbyteService;
    @Autowired
    private ActorDefinitionService actorDefinitionService;


    @PostMapping("create")
    public BaseResponse<?> create(@RequestBody @Valid DestinationCreate destinationCreate) {
        return BaseResponse.success(actorService.save(destinationCreate.toAddEntity()));
    }

    @PostMapping("update")
    public BaseResponse<?> update(@RequestBody @Valid DestinationUpdate destinationUpdate) {
        actorService.update(destinationUpdate.toUpdateEntity());
        return BaseResponse.success();
    }

    @GetMapping("delete")
    public BaseResponse<?> delete(@RequestParam("destinationId") Integer destinationId) {
        actorService.delete(destinationId);
        return BaseResponse.success();
    }

    @GetMapping("exist")
    public BaseResponse<?> exist(Integer destinationId, @RequestParam("name") String name) {
        return BaseResponse.success(actorService.checkExist(destinationId, name, ActorTypeEnum.destination.name()));
    }

    @GetMapping("get")
    public BaseResponse<DestinationRead> get(@RequestParam("destinationId") Integer destinationId) {
        Actor actor = actorService.getById(destinationId);
        if (actor == null) {
            throw new ServiceException(BaseResponseCodeEnum.SYS_ERR.name(), "数据 destination 信息不存在");
        }

        ActorDefinition actorDefinition = actorDefinitionService.getById(actor.getActorDefinitionId());
        Map<Integer, ActorDefinition> definitionMap = new HashMap<>();
        definitionMap.put(actor.getActorDefinitionId(), actorDefinition);
        return BaseResponse.success(new DestinationRead(actor, definitionMap));
    }


    @GetMapping("page")
    public BaseResponse<PageInfo<DestinationRead>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                                        @RequestParam(defaultValue = "50") Integer pageSize,
                                                        @Valid @ModelAttribute DestinationSearch destinationSearch) {
        List<Integer> definitionIds = new ArrayList<>();
        if (StringUtils.isNotBlank(destinationSearch.getDestinationName())) {
            Example example = new Example(ActorDefinition.class);
            example.or()
                    .andLike("name", "%" + destinationSearch.getDestinationName() + "%")
                    .andEqualTo("deleteStatus", DeleteEntity.NOT_DELETE);

            definitionIds = actorDefinitionService.listByExample(example).stream()
                    .map(ActorDefinition::getId)
                    .collect(Collectors.toList());
        }

        PageInfo<Actor> pageInfo = actorService.listByPage(pageNum, pageSize, destinationSearch.toExample(definitionIds));
        Map<Integer, ActorDefinition> definitionMap = actorDefinitionService.mapByIds(pageInfo.getList().stream()
                .map(Actor::getActorDefinitionId));
        return BaseResponse.success(PageUtils.map(pageInfo, item -> new DestinationRead(item, definitionMap)));
    }

    @PostMapping("check_connection")
    public BaseResponse<CheckConnectionRead> checkConnection(@Valid @RequestBody DestinationCheckConnection checkConnection) {
        ActorDefinition actorDefinition = actorDefinitionService.getById(checkConnection.getDestinationDefinitionId());
        if (actorDefinition == null) {
            throw new ServiceException(BaseResponseCodeEnum.SYS_ERR.name(), "数据源定义信息不存在");
        }

        try {
            boolean result = airbyteService.check(String.join(":", actorDefinition.getDockerRepository(),
                    actorDefinition.getDockerImageTag()), checkConnection.getConnectionConfiguration().toJSONString());
            return BaseResponse.success(new CheckConnectionRead(result, null));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return BaseResponse.success(new CheckConnectionRead(false, e.getMessage()));
        }
    }

    @GetMapping("document/{instance}")
    public BaseResponse<?> doc(@PathVariable("instance") String instance) {
        return HttpUtil.get(String.format("https://demo.airbyte.io/docs/integrations/destinations/%s", instance));
    }

}
