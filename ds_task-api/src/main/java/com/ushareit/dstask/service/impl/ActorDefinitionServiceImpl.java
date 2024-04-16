package com.ushareit.dstask.service.impl;

import com.ushareit.dstask.bean.AccessProduct;
import com.ushareit.dstask.bean.Actor;
import com.ushareit.dstask.bean.ActorDefinition;
import com.ushareit.dstask.bean.DeleteEntity;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.constant.SourceTypeEnum;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.mapper.ActorDefinitionMapper;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import com.ushareit.dstask.service.ActorDefinitionService;
import com.ushareit.dstask.service.ActorService;
import com.ushareit.dstask.third.airbyte.AirbyteService;
import com.ushareit.dstask.third.airbyte.config.ConnectorSpecification;
import com.ushareit.dstask.third.airbyte.json.Jsons;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import com.ushareit.dstask.web.utils.DataCakeConfigUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author fengxiao
 * @date 2022/7/25
 */
@Slf4j
@Service
public class ActorDefinitionServiceImpl extends AbstractBaseServiceImpl<ActorDefinition>
        implements ActorDefinitionService {

    @Autowired
    private ActorDefinitionMapper actorDefinitionMapper;

    @Resource
    private ActorService actorService;

    @Autowired
    private AirbyteService airbyteService;

    @Override
    public CrudMapper<ActorDefinition> getBaseMapper() {
        return actorDefinitionMapper;
    }

    public static String SOURCE = "source";

    @Override
    public Object save(ActorDefinition actorDefinition) {
        String imageUrl = String.join(":", actorDefinition.getDockerRepository(), actorDefinition.getDockerImageTag());
        try {
            ConnectorSpecification spec = airbyteService.spec(imageUrl);
            actorDefinition.setSpec(Jsons.serialize(spec));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new ServiceException(BaseResponseCodeEnum.SYS_ERR.name(), "获取SPEC文件信息失败");
        }

        return super.save(actorDefinition);
    }

    @Override
    public void delete(Object id) {
        ActorDefinition toUpdateParam = ActorDefinition.builder().build();
        toUpdateParam.setId((Integer) id);
        toUpdateParam.setDeleteStatus(DeleteEntity.DELETE);

        actorDefinitionMapper.updateByPrimaryKeySelective(toUpdateParam);
    }


    public List<Actor> getActorByTypeAndRegion(String type, String region) {
        if (StringUtils.isEmpty(type)) {
            return new ArrayList<>();
        }
        String dockerRepository = SourceTypeEnum.SourceTypeMap.get(type).getSourceDefinition();
        ActorDefinition actorDefinition = new ActorDefinition();
        actorDefinition.setDockerRepository(dockerRepository);
        actorDefinition.setActorType(SOURCE);
        actorDefinition.setDeleteStatus(0);
        actorDefinition.setForDsTemplate(1);
        List<ActorDefinition> selectList = actorDefinitionMapper.select(actorDefinition);
        if (selectList.isEmpty()) {
            return new ArrayList<>();
        }
        List<Integer> ids = selectList.stream().map(ActorDefinition::getId).collect(Collectors.toList());
        return actorService.selectByActorDefinitionIds(ids, region);
    }

    @Override
    public void initActorDefinitions(String tenantName) {
        InfTraceContextHolder.get().setTenantName(DataCakeConfigUtil.getDataCakeSourceConfig().getSuperTenant());
        Example example = new Example(ActorDefinition.class);
        example.or()
                .andEqualTo("deleteStatus", DeleteEntity.NOT_DELETE);
        List<ActorDefinition> actorDefinitionList = actorDefinitionMapper.selectByExample(example);

        InfTraceContextHolder.get().setTenantName(tenantName);
        actorDefinitionList.stream().peek(item -> {
            item.setCreateBy("admin");
            item.setUpdateBy("admin");
            item.setCreateTime(new Timestamp(System.currentTimeMillis()));
            item.setCreateTime(new Timestamp(System.currentTimeMillis()));
        }).forEach(actorDefinitionMapper::insertSelective);
    }
}
