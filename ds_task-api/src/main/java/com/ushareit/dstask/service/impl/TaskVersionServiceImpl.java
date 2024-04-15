package com.ushareit.dstask.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ushareit.dstask.bean.ArtifactVersion;
import com.ushareit.dstask.bean.*;
import com.ushareit.dstask.constant.*;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.mapper.TaskMapper;
import com.ushareit.dstask.mapper.TaskVersionMapper;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import com.ushareit.dstask.service.ArtifactVersionService;
import com.ushareit.dstask.service.TaskService;
import com.ushareit.dstask.service.TaskVersionService;
import com.ushareit.dstask.third.airbyte.json.Jsons;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import com.ushareit.dstask.web.factory.flink.param.RuntimeConfig;
import com.ushareit.dstask.web.factory.scheduled.param.EventDepend;
import com.ushareit.dstask.web.utils.AuditlogUtil;
import com.ushareit.dstask.web.utils.DataCakeConfigUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * @author: licg
 * @create: 2021-05-12 15:24
 */
@Slf4j
@Service
public class TaskVersionServiceImpl extends AbstractBaseServiceImpl<TaskVersion> implements TaskVersionService {

    @Resource
    private TaskMapper taskMapper;
    @Resource
    private TaskVersionMapper taskVersionMapper;
    @Autowired
    private ArtifactVersionService artifactVersionService;
    @Autowired
    private TaskService taskService;
    @Override
    public CrudMapper<TaskVersion> getBaseMapper() {
        return taskVersionMapper;
    }

    @Override
    public int getMaxVersionById(Integer id) {
        Integer maxVersion = taskVersionMapper.getMaxVersionById(id);
        return maxVersion == null ? 0 : maxVersion;
    }

    @Override
    public void setMaxVersions(List<Task> tasks) {
        List<Integer> ids = tasks.stream().map(Task::getId).collect(Collectors.toList());
        List<TaskVersion> taskVersions = taskVersionMapper.getTaskVersionByIds(ids);
        Map<Integer, List<TaskVersion>> group = taskVersions.stream().collect(Collectors.groupingBy(TaskVersion::getTaskId));

        group.forEach((id, list) -> {
            Task find = findTaskById(tasks, id);
            if (list == null) {
                return;
            }
            TaskVersion max = list.stream().filter(taskVersion -> taskVersion.getVersion() != null).max(Comparator.comparingInt(TaskVersion::getVersion)).get();
            find.setCurrentVersion(max.getVersion() + 1);
        });

        tasks.stream().forEach(task -> {
            if (task.getCurrentVersion() == null) {
                task.setCurrentVersion(1);
            }
        });
    }

    private Task findTaskById(List<Task> tasks, Integer id) {
        Task result = tasks.stream().filter(task -> task.getId().equals(id)).findFirst().get();
        return result;
    }

    @Override
    public TaskVersion selectByIdAndVersion(Integer id, Integer version) {
        if (id == null || version == null) {
            throw new ServiceException(BaseResponseCodeEnum.CLI_PARAM_REQUIRED);
        }
        TaskVersion taskVersionRes;
        TaskVersion taskVersion = new TaskVersion();
        taskVersion.setTaskId(id);
        taskVersion.setVersion(version);
        taskVersionRes = getBaseMapper().selectOne(taskVersion);
        if (taskVersionRes == null) {
            throw new ServiceException(BaseResponseCodeEnum.DATA_NOT_FOUND);
        }
        taskVersionRes.setStatusCode(BaseResponseCodeEnum.CREATED.name());
        isStreaming(taskVersionRes);
        isTaskInCurrent(taskVersionRes);
        setDisplayDependJars(taskVersionRes);
        return taskVersionRes;
    }


    private void isTaskInCurrent(TaskVersion taskVersion) {
        Boolean admin = InfTraceContextHolder.get().getAdmin();
        if (admin == null) {
            admin = false;
        }
        if (admin) {
            taskVersion.setCanEdit(true);
            return;
        }
        String currentUser = InfTraceContextHolder.get().getUserName();
        if (taskVersion.getCreateBy().equalsIgnoreCase(currentUser)) {
            taskVersion.setCanEdit(true);
            return;
        }
        RuntimeConfig runtimeConfig = JSON.parseObject(taskVersion.getRuntimeConfig(), RuntimeConfig.class);
        if (!StringUtils.isEmpty(runtimeConfig.getOwner()) && runtimeConfig.getOwner().equalsIgnoreCase(currentUser)) {
            taskVersion.setCanEdit(true);
            return;
        }

        String collaborators = taskVersion.getCollaborators();
        if (StringUtils.isEmpty(collaborators)) {
            return;
        }
        String[] collaboratorsArr = collaborators.split(",");
        for (String collaborator : collaboratorsArr) {
            if (collaborator.equalsIgnoreCase(currentUser)) {
                taskVersion.setCanEdit(true);
                break;
            }
        }
    }

    public Boolean isStreaming(TaskVersion taskVersion) {
        boolean stream = TemplateEnum.valueOf(taskVersion.getTemplateCode()).isStreamingTemplate();
        if (stream) {
            RuntimeConfig runtimeConfig = JSON.parseObject(taskVersion.getRuntimeConfig(), RuntimeConfig.class);
            Boolean isBatchTask = runtimeConfig.getIsBatchTask();
            if (isBatchTask != null && isBatchTask) {
                taskVersion.setIsStreamingTemplateCode(false);
            } else {
                taskVersion.setIsStreamingTemplateCode(true);
            }
        } else {
            taskVersion.setIsStreamingTemplateCode(false);
        }
        return taskVersion.getIsStreamingTemplateCode();
    }


    public void setDisplayDependJars(TaskVersion taskVersion) {
        String runtimeConfigJson = taskVersion.getRuntimeConfig();
        JSONObject runtimeConfigObject = JSON.parseObject(runtimeConfigJson);
        if (StringUtils.isNotEmpty(taskVersion.getDependArtifacts())) {
            CloudResouce.DataResource defaultRegionConfig = DataCakeConfigUtil.getCloudResourcesService().getDefaultRegionConfig();
            String region = defaultRegionConfig.getRegionAlias();
            if (StringUtils.isNotEmpty(runtimeConfigObject.getString("sourceRegion"))) {
                region = runtimeConfigObject.getString("sourceRegion");
            }
            if (isStreaming(taskVersion) && StringUtils.isNotEmpty(runtimeConfigObject.getString("region"))) {
                region = runtimeConfigObject.getString("region");
            }
            List<ArtifactVersion> list = artifactVersionService.getDisplayArtifact(taskVersion.getDependArtifacts(), region);
            taskVersion.setDisplayDependJars(list);
        }
    }

    @Override
    public TaskVersion getById(Object id) {
        TaskVersion taskVersion = super.getById(id);

        if (StringUtils.isNotEmpty(taskVersion.getDependArtifacts())) {
            List<ArtifactVersion> list = artifactVersionService.getDisplayArtifact(taskVersion.getDependArtifacts(), null);
            taskVersion.setDisplayDependJars(list);
        }
        return taskVersion;
    }

    @Override
    public List<TaskVersion> listByExample(TaskVersion taskVersion) {
        List<TaskVersion> list = taskVersionMapper.select(taskVersion);
        Task task = taskService.getById(taskVersion.getTaskId());
        padInfos(list);
        list.add(0, TaskVersion.CreateCurrent(task));

        return list;
    }

    @Override
    public List<TaskVersion> list(TaskVersion taskVersion) {
        return taskVersionMapper.select(taskVersion);
    }


    private void padInfos(List<TaskVersion> list) {
        list.stream().forEach(taskVersion -> {
            taskVersion.setDisplayVersion("V" + taskVersion.getVersion());
            padDisplayArtifact(taskVersion);

        });
    }

    private void padDisplayArtifact(TaskVersion taskVersion) {
        if (StringUtils.isEmpty(taskVersion.getDependArtifacts())) {
            return;
        }

        taskVersion.setDisplayDependJars(getDisplayArtifact(taskVersion.getDependArtifacts()));
    }

    private List<ArtifactVersion> getDisplayArtifact(String dependJars) {
        // 6:-1 or 6:7 or 6:-1,6:7
        String[] depJarIds = dependJars.split(",");
        ArrayList<ArtifactVersion> list = new ArrayList<>(16);
        for (String depJarId : depJarIds) {
            String[] dep = depJarId.split(":");
            ArtifactVersion example = new ArtifactVersion().setArtifactId(Integer.parseInt(dep[0]));
            example.setId(Integer.parseInt(dep[1]));
            ArtifactVersion artifactVersion = artifactVersionService.selectOne(example);
            if (artifactVersion == null) {
                continue;
            }

            if ("-1".equals(depJarId.split(":")[1])) {
                artifactVersion.setId(-1);
                artifactVersion.setVersion(-1);
                artifactVersion.setDisplayVersion("Current");
            }

            list.add(artifactVersion);
        }
        return list;
    }


    @Override
    public void verionSwitch(TaskVersion taskVersion) {
        List<TaskVersion> taskVersionList = taskVersionMapper.select(TaskVersion.builder().version(taskVersion.getVersion())
                .taskId(taskVersion.getTaskId()).build());
        if (taskVersionList == null || taskVersionList.isEmpty()) {
            throw new ServiceException(BaseResponseCodeEnum.TASK_VERSION_FOUND.name(), String.format("%s: %d", BaseResponseCodeEnum.TASK_VERSION_FOUND.getMessage(), taskVersion.getVersion()));
        }
        Task taskSource = taskService.getById(taskVersion.getTaskId());
        TaskVersion selectTaskVersion = taskVersionList.stream().findFirst().orElse(null);
        Task task = Task.cloneByTaskVersion(selectTaskVersion);
        task.setCurrentVersion(taskVersion.getVersion());
        task.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        task.setUpdateBy(InfTraceContextHolder.get().getUserName());
        task.setOnline(taskSource.getOnline());
        taskService.updateByid(task);
        AuditlogUtil.auditlog(DsTaskConstant.TASK, task.getId(), BaseActionCodeEnum.SWITCH, "任务被切换到版本:" + taskVersion.getVersion());
        if (taskSource.getOnline() != null && taskSource.getOnline().equals(1) && !taskService.isStreaming(task)) {
            try {
                taskService.start(task.getId(), null, null);
            } catch (Exception e) {
                throw new ServiceException(BaseResponseCodeEnum.APP_START_FAIL, e);
            }
        }
    }

    @Override
    public Map<Integer, TaskVersion> getLatestVersion(Collection<Integer> taskIds) {
        if (CollectionUtils.isEmpty(taskIds)) {
            return Collections.emptyMap();
        }

        Example example = new Example(TaskVersion.class);
        example.or().andIn("taskId", taskIds);

        return taskVersionMapper.selectByExample(example).stream()
                .collect(Collectors.groupingBy(TaskVersion::getTaskId)).entrySet().stream()
                .collect(HashMap::new, (m, entry) -> {
                    Optional<TaskVersion> taskVersionOptional = entry.getValue().stream()
                            .max(Comparator.comparing(TaskVersion::getVersion));
                    taskVersionOptional.ifPresent(taskVersion -> m.put(entry.getKey(), taskVersionOptional.get()));
                }, HashMap::putAll);
    }

    @Override
    public void swapTaskKeys(List<Pair<Optional<String>, Integer>> taskKeyIdPairs,
                             BiFunction<String, Map<String, Integer>, List<EventDepend>> eventDependsParser) {
        Map<String, Integer> keyIdMap = taskKeyIdPairs.stream()
                .filter(item -> item.getKey().isPresent())
                .collect(HashMap::new, (m, pair) -> m.put(pair.getKey().get(), pair.getValue()), HashMap::putAll);

        if (MapUtils.isEmpty(keyIdMap)) {
            return;
        }

        getLatestVersion(taskKeyIdPairs.stream().map(Pair::getValue).collect(Collectors.toList())).values().stream()
                .filter(item -> StringUtils.isNotBlank(item.getEventDepends()))
                .forEach(item -> {
                    List<EventDepend> eventDependList = eventDependsParser.apply(item.getEventDepends(), keyIdMap);
                    TaskVersion toUpdateParam = new TaskVersion();
                    toUpdateParam.setId(item.getId());
                    toUpdateParam.setEventDepends(Jsons.serialize(eventDependList));
                    taskVersionMapper.updateByPrimaryKeySelective(toUpdateParam);
                });
    }

    @Override
    public void batchTurnTaskVersions(Collection<Pair<Integer, Integer>> idVersionPairs, TurnType turnType) {
        getTaskVersionList(idVersionPairs).forEach(item -> {
            Task toUpdateTaskParam = new Task();
            toUpdateTaskParam.setId(item.getTaskId());
            toUpdateTaskParam.setOnline(turnType.getType());
            taskMapper.updateByPrimaryKeySelective(toUpdateTaskParam);

            TaskVersion toUpdateTaskVersionParam = new TaskVersion();
            toUpdateTaskVersionParam.setId(item.getId());
            toUpdateTaskVersionParam.setOnline(turnType.getType());
            taskVersionMapper.updateByPrimaryKeySelective(toUpdateTaskVersionParam);
        });
    }

    private List<TaskVersion> getTaskVersionList(Collection<Pair<Integer, Integer>> idVersionPairs) {
        if (CollectionUtils.isEmpty(idVersionPairs)) {
            return Collections.emptyList();
        }

        Example example = new Example(TaskVersion.class);
        CollectionUtils.emptyIfNull(idVersionPairs).forEach(item -> example.or().andEqualTo("taskId", item.getKey())
                .andEqualTo("version", item.getValue()));

        return taskVersionMapper.selectByExample(example);
    }
}
