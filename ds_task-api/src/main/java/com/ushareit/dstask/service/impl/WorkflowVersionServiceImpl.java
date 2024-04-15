package com.ushareit.dstask.service.impl;

import com.alibaba.fastjson.JSON;
import com.ushareit.dstask.bean.DeleteEntity;
import com.ushareit.dstask.bean.Workflow;
import com.ushareit.dstask.bean.WorkflowVersion;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.constant.WorkflowStatus;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.mapper.WorkflowMapper;
import com.ushareit.dstask.mapper.WorkflowTaskMapper;
import com.ushareit.dstask.mapper.WorkflowVersionMapper;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import com.ushareit.dstask.service.WorkflowVersionService;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author fengxiao
 * @date 2022/11/11
 */
@Slf4j
@Service
public class WorkflowVersionServiceImpl extends AbstractBaseServiceImpl<WorkflowVersion> implements WorkflowVersionService {

    @Autowired
    private WorkflowVersionMapper workflowVersionMapper;
    @Autowired
    private WorkflowTaskMapper workflowTaskMapper;
    @Autowired
    private WorkflowMapper workflowMapper;

    @Override
    public CrudMapper<WorkflowVersion> getBaseMapper() {
        return workflowVersionMapper;
    }

    @Override
    public Object save(WorkflowVersion workflowVersion) {
        Validate.notNull(workflowVersion.getWorkflowId(), "工作流ID不能为空");

        Optional<WorkflowVersion> latestWorkflowOptional = getLatestVersionById(workflowVersion.getWorkflowId());
        latestWorkflowOptional.ifPresent(item -> {
            if (StringUtils.isBlank(workflowVersion.getCollaborators())) {
                workflowVersion.setCollaborators(item.getCollaborators());
            }

            if (StringUtils.isBlank(workflowVersion.getOwner())) {
                workflowVersion.setOwner(item.getOwner());
            }

            if (StringUtils.isBlank(workflowVersion.getGranularity())) {
                workflowVersion.setGranularity(item.getGranularity());
            }

            if (StringUtils.isBlank(workflowVersion.getCronConfig())) {
                workflowVersion.setCronConfig(item.getCronConfig());
            }

            if (WorkflowStatus.of(item.getStatus()) == WorkflowStatus.CREATED) {
                workflowVersion.setStatus(item.getStatus());
            } else {
                workflowVersion.setStatus(WorkflowStatus.OFFLINE.getType());
            }

            workflowVersion.setVersion(item.getVersion() + NumberUtils.INTEGER_ONE);
        });

        return workflowVersionMapper.insertSelective(workflowVersion);
    }

    @Override
    public Optional<WorkflowVersion> getLatestVersionById(Integer workflowId) {
        if (workflowId == null) {
            throw new ServiceException(BaseResponseCodeEnum.SYS_ERR.name(), "工作流ID不能为空");
        }

        Example example = new Example(WorkflowVersion.class);
        example.or()
                .andEqualTo("workflowId", workflowId)
                .andEqualTo("deleteStatus", DeleteEntity.NOT_DELETE);
        return workflowVersionMapper.selectByExample(example).stream().max(Comparator.comparing(WorkflowVersion::getVersion));
    }

    @Override
    public Optional<WorkflowVersion> getVersionById(Integer workflowId, Integer workflowVersionId) {
        if (workflowId == null) {
            throw new ServiceException(BaseResponseCodeEnum.SYS_ERR.name(), "工作流ID不能为空");
        }

        Example example = new Example(WorkflowVersion.class);
        example.or()
                .andEqualTo("workflowId", workflowId)
                .andEqualTo("id", workflowVersionId)
                .andEqualTo("deleteStatus", DeleteEntity.NOT_DELETE);
        return workflowVersionMapper.selectByExample(example).stream().findFirst();
    }

    @Override
    public Optional<WorkflowVersion> getCurrentVersion(Integer workflowId) {
        Workflow workflow = workflowMapper.selectByPrimaryKey(workflowId);
        if (workflow == null) {
            throw new ServiceException(BaseResponseCodeEnum.SYS_ERR.name(), String.format("工作流【%s】不存在", workflowId));
        }

        return getByVersion(workflowId, workflow.getCurrentVersion());
    }

    @Override
    public Optional<WorkflowVersion> getByVersion(Integer workflowId, Integer version) {
        if (workflowId == null) {
            throw new ServiceException(BaseResponseCodeEnum.SYS_ERR.name(), "工作流ID不能为空");
        }

        Example example = new Example(WorkflowVersion.class);
        example.or()
                .andEqualTo("workflowId", workflowId)
                .andEqualTo("version", version)
                .andEqualTo("deleteStatus", DeleteEntity.NOT_DELETE);
        return workflowVersionMapper.selectByExample(example).stream().findFirst();
    }

    @Override
    public void offline(Integer workflowId, Integer workflowVersionId) {
        WorkflowVersion toUpdateEntity = new WorkflowVersion();
        toUpdateEntity.setId(workflowVersionId);
        toUpdateEntity.setStatus(WorkflowStatus.OFFLINE.getType());
        toUpdateEntity.setUpdateBy(InfTraceContextHolder.get().getUserName());
        workflowVersionMapper.updateByPrimaryKeySelective(toUpdateEntity);

        Workflow toUpdateWorkflowParam = new Workflow();
        toUpdateWorkflowParam.setId(workflowId);
        toUpdateWorkflowParam.setStatus(WorkflowStatus.OFFLINE.getType());
        toUpdateWorkflowParam.setUpdateBy(InfTraceContextHolder.get().getUserName());
        workflowMapper.updateByPrimaryKeySelective(toUpdateWorkflowParam);
    }

    @Override
    public void online(Integer workflowId, Integer workflowVersionId) {
        WorkflowVersion workflowVersion = workflowVersionMapper.selectByPrimaryKey(workflowVersionId);
        if (workflowVersion == null) {
            throw new ServiceException(BaseResponseCodeEnum.SYS_ERR.name(), "指定的工作流版本不存在");
        }

        WorkflowVersion toUpdateEntity = new WorkflowVersion();
        toUpdateEntity.setStatus(WorkflowStatus.ONLINE.getType());
        toUpdateEntity.setUpdateBy(InfTraceContextHolder.get().getUserName());
        toUpdateEntity.setId(workflowVersion.getId());
        update(toUpdateEntity);

        // 设置工作流的当前版本
        Workflow toUpdateParam = new Workflow();
        BeanUtils.copyProperties(workflowVersion, toUpdateParam, "id", "status", "createBy", "updateTime");
        toUpdateParam.setId(workflowId);
        toUpdateParam.setStatus(WorkflowStatus.ONLINE.getType());
        toUpdateParam.setCurrentVersion(workflowVersion.getVersion());
        toUpdateEntity.setUpdateBy(InfTraceContextHolder.get().getUserName());

        log.info("to update workflow param is {}", JSON.toJSONString(toUpdateParam));
        workflowMapper.updateByPrimaryKeySelective(toUpdateParam);
    }

    @Override
    public Map<Integer, Optional<WorkflowVersion>> getWorkflowVersionList(Collection<Pair<Integer, Integer>> idVersionPairs) {
        if (CollectionUtils.isEmpty(idVersionPairs)) {
            return Collections.emptyMap();
        }

        Example example = new Example(WorkflowVersion.class);
        idVersionPairs.forEach(item -> example.or().andEqualTo("workflowId", item.getKey())
                .andEqualTo("version", item.getValue()));
        example.and().andEqualTo("deleteStatus", DeleteEntity.NOT_DELETE);

        Map<Integer, Integer> idVersionMap = idVersionPairs.stream()
                .collect(HashMap::new, (m, pair) -> m.put(pair.getKey(), pair.getValue()), HashMap::putAll);

        return workflowVersionMapper.selectByExample(example).stream()
                .collect(Collectors.groupingBy(WorkflowVersion::getWorkflowId))
                .entrySet().stream()
                .collect(HashMap::new, (m, entry) -> m.put(entry.getKey(), entry.getValue().stream()
                                .filter(one -> one.getVersion().intValue() == idVersionMap.get(one.getWorkflowId())).findFirst()),
                        HashMap::putAll);
    }

}
