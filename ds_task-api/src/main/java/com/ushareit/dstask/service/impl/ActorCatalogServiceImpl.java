package com.ushareit.dstask.service.impl;

import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.ushareit.dstask.bean.ActorCatalog;
import com.ushareit.dstask.bean.DeleteEntity;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.mapper.ActorCatalogMapper;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import com.ushareit.dstask.service.ActorCatalogService;
import com.ushareit.dstask.third.airbyte.config.AirbyteCatalog;
import com.ushareit.dstask.third.airbyte.json.Jsons;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

/**
 * @author fengxiao
 * @date 2022/7/27
 */
@Service
public class ActorCatalogServiceImpl extends AbstractBaseServiceImpl<ActorCatalog> implements ActorCatalogService {

    @Autowired
    private ActorCatalogMapper actorCatalogMapper;

    private final HashFunction hashFunction = Hashing.murmur3_32();

    @Override
    public CrudMapper<ActorCatalog> getBaseMapper() {
        return actorCatalogMapper;
    }

    @Override
    public void saveOrUpdate(Integer actorId, AirbyteCatalog airbyteCatalog) {
        Example example = new Example(ActorCatalog.class);
        example.or()
                .andEqualTo("actorId", actorId)
                .andEqualTo("deleteStatus", DeleteEntity.NOT_DELETE);
        List<ActorCatalog> actorCatalogList = getBaseMapper().selectByExample(example);

        if (actorCatalogList.size() > 1) {
            throw new ServiceException(BaseResponseCodeEnum.SYS_ERR, "数据错误");
        }

        String catalog = Jsons.serialize(airbyteCatalog);
        String catalogHash = hashFunction.hashBytes(catalog.getBytes(Charsets.UTF_8)).toString();

        if (CollectionUtils.isEmpty(actorCatalogList)) {
            ActorCatalog actorCatalog = ActorCatalog.builder()
                    .actorId(actorId)
                    .catalog(catalog)
                    .catalogHash(catalogHash)
                    .build();

            actorCatalog.setCreateBy(InfTraceContextHolder.get().getUserName());
            actorCatalog.setUpdateBy(InfTraceContextHolder.get().getUserName());
            save(actorCatalog);
        } else {
            ActorCatalog toUpdateParam = ActorCatalog.builder()
                    .catalog(catalog)
                    .catalogHash(catalogHash)
                    .build();
            toUpdateParam.setId(actorCatalogList.get(0).getId());
            toUpdateParam.setUpdateBy(InfTraceContextHolder.get().getUserName());
            getBaseMapper().updateByPrimaryKey(toUpdateParam);
        }
    }

    @Override
    public List<ActorCatalog> selectByActorId(Integer actorId) {
        ActorCatalog actorCatalog = new ActorCatalog();
        actorCatalog.setActorId(actorId);
        actorCatalog.setDeleteStatus(0);
        return getBaseMapper().select(actorCatalog);
    }
}
