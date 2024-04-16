package com.ushareit.dstask.common.format;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ushareit.dstask.bean.Task;
import com.ushareit.dstask.constant.DsTaskConstant;
import com.ushareit.dstask.constant.SchedulerCycleEnum;
import com.ushareit.dstask.constant.SymbolEnum;
import com.ushareit.dstask.constant.TemplateEnum;
import com.ushareit.dstask.web.factory.flink.param.RuntimeConfig;
import com.ushareit.dstask.web.factory.scheduled.param.Dataset;
import com.ushareit.dstask.web.factory.scheduled.param.EventDepend;
import com.ushareit.dstask.web.factory.scheduled.param.TriggerParam;
import com.ushareit.dstask.web.utils.DataCakeTaskConfig;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author fengxiao
 * @date 2023/2/16
 */
public class TaskFormatter {

    /**
     * 格式化从 web 传入的 task
     *
     * @param task task 原数据
     */
    public static void formatFromWeb(Task task) {
        produceDependJars(task);

        produceDependDataSets(task);

        produceRoles(task);

        // 生成datasetId
        produceDatasetsId(task);

        // 兼容没有时间调度前的旧任务
        produceOutputGra(task);
    }

    private static void produceDependJars(Task taskFromWeb) {
        if (taskFromWeb.getDisplayDependJars() == null) {
            return;
        }

        String dependJars = taskFromWeb.getDisplayDependJars().stream()
                .filter(artifactVersion -> artifactVersion.getArtifactId() != null && artifactVersion.getId() != null)
                .map(artifactVersion -> artifactVersion.getArtifactId() + SymbolEnum.COLON.getSymbol() + artifactVersion.getId())
                .collect(Collectors.joining(SymbolEnum.COMMA.getSymbol()));
        taskFromWeb.setDependArtifacts(dependJars);
    }

    private static void produceDependDataSets(Task task) {
        if (StringUtils.isNotEmpty(task.getInputDataset())) {
            task.setInputGuids(getQualifiedName(task.getInputDataset()));
        }

        if (StringUtils.isNotBlank(task.getEventDepends())) {
            List<EventDepend> eventDepends = JSON.parseArray(task.getEventDepends(), EventDepend.class);
            if (CollectionUtils.isNotEmpty(eventDepends)) {
                String eventDependGuids = eventDepends.stream()
                        .filter(item -> !item.getIsDelete())
                        .map(EventDepend::getMetadataId)
                        .filter(StringUtils::isNotBlank)
                        .collect(Collectors.joining(SymbolEnum.COMMA.getSymbol()));

                task.setInputGuids(StringUtils.isBlank(task.getInputGuids()) ? eventDependGuids :
                        StringUtils.isBlank(eventDependGuids) ? task.getInputGuids() :
                                String.join(SymbolEnum.COMMA.getSymbol(), task.getInputGuids(), eventDependGuids));
            }
        }

        if (StringUtils.isNotEmpty(task.getOutputDataset())) {
            // 兼容前端传递outputDataset = [{"offset": -1, "id": "", "placeholder": true}]
            String outputGuids = "";
            List<Dataset> datasets = JSON.parseArray(task.getOutputDataset(), Dataset.class);
//            if ((datasets.size() != 0) && ((!datasets.get(0).getId().equals("")) || (datasets.get(0).getMetadata() != null))) {
//                outputGuids = getQualifiedName(task.getOutputDataset());
//            }
            if (datasets.size() != 0) {
                if (datasets.get(0).getId() != null || datasets.get(0).getMetadata() != null) {
                    outputGuids = getQualifiedName(task.getOutputDataset());
                } else if (datasets.get(0).getId() == null && datasets.get(0).getMetadata() == null) {
                    outputGuids = task.getTemplateCode() + "_" + task.getName();
                    datasets.get(0).setId(outputGuids);
                    datasets.get(0).setOffset(-1);
                    datasets.get(0).setPlaceholder(true);
                    task.setOutputDataset(JSONObject.toJSONString(datasets));
                }
            } else {
                HashMap<String, Object> temp = new HashMap<>();
                ArrayList<Map<String, Object>> output = new ArrayList<Map<String, Object>>();
                String id = task.getTemplateCode() + "_" + task.getName();
                temp.put("id", id);
                temp.put("placeholder", true);
                temp.put("offset", -1);
                output.add(temp);
                task.setOutputDataset(JSONObject.toJSONString(output));
                outputGuids = id;
            }
            task.setOutputGuids(outputGuids);
        }
    }

    private static void produceRoles(Task task) {
        String runtimeConfigJson = task.getRuntimeConfig();

        String owner = DataCakeTaskConfig.getStringConfigValue(runtimeConfigJson, "owner");
        task.setCreateBy(owner);

        String collaborators = DataCakeTaskConfig.getStringConfigValue(runtimeConfigJson, "collaborators");
        if (StringUtils.isEmpty(collaborators)) {
            return;
        }

        List<String> collaboratorArr = JSONArray.parseArray(collaborators, String.class);
        task.setCollaborators(CollectionUtils.isEmpty(collaboratorArr) ? StringUtils.EMPTY :
                String.join(SymbolEnum.COMMA.getSymbol(), collaboratorArr));
    }

    private static void produceDatasetsId(Task task) {
        // 拼接datasetId
        List<Dataset> inputDataset = JSON.parseArray(task.getInputDataset(), Dataset.class);
        List<Dataset> outputDataset = JSON.parseArray(task.getOutputDataset(), Dataset.class);
        if (inputDataset.size() > 0) {
            inputDataset.forEach(dataset -> {
                Dataset.Metadata metadata = dataset.getMetadata();
                if (metadata == null || metadata.getTable() == null || metadata.getTable().isEmpty()) {
                    dataset.setId(dataset.getId());
                } else {
                    if (metadata.getType() == null) {
                        // 之前存在一部分dataset没有type传过来,这部分都是hive任务,这里给一个默认值
                        metadata.setType("hive");
                    }
                    dataset.setId(metadata.toString());
                }
            });
            task.setInputDataset(JSON.toJSONString(inputDataset));
        }

        if (outputDataset.size() > 0) {
            outputDataset.forEach(dataset -> {
                Dataset.Metadata metadata = dataset.getMetadata();
                if (metadata == null || metadata.getTable() == null || metadata.getTable().isEmpty()) {
                    dataset.setId(dataset.getId());
                } else {
                    if (metadata.getType() == null) {
                        // 之前存在一部分dataset没有type传过来,这部分都是hive任务,这里给一个默认值
                        metadata.setType("hive");
                    }
                    dataset.setId(metadata.toString());
                }
            });
            task.setOutputDataset(JSON.toJSONString(outputDataset));
        }
    }

    private static void produceOutputGra(Task task) {
        if (isStreaming(task)) {
            return;
        }
        // 兼容旧的任务生成接口
        if (task.getTriggerParam() == null || task.getTriggerParam().isEmpty()) {
            Stack<SchedulerCycleEnum> inputMinCycleStack = new Stack<>();
            inputMinCycleStack.push(SchedulerCycleEnum.YEARLY);

            TriggerParam triggerParam = new TriggerParam();
            String inputDataset = task.getInputDataset();
            List<Dataset> inputDatasets = JSON.parseArray(inputDataset, Dataset.class);
            if (inputDatasets != null && !inputDatasets.equals("[]")) {
                inputDatasets.forEach(x -> {
                    SchedulerCycleEnum inputCycle = SchedulerCycleEnum.valueOf(x.getGranularity().toUpperCase());
                    SchedulerCycleEnum inputMinCycle = inputMinCycleStack.pop();
                    if (!inputCycle.compare(inputMinCycle)) {
                        inputMinCycleStack.push(inputCycle);
                    } else {
                        inputMinCycleStack.push(inputMinCycle);
                    }
                });
            }

            String eventDepends = task.getEventDepends();
            List<EventDepend> eventDependsList = JSON.parseArray(eventDepends, EventDepend.class);
            if (eventDependsList != null && !eventDependsList.equals("[]") && !eventDependsList.isEmpty()) {
                eventDependsList.forEach(x -> {
                    SchedulerCycleEnum inputCycle = SchedulerCycleEnum.valueOf(x.getGranularity().toUpperCase());
                    SchedulerCycleEnum inputMinCycle = inputMinCycleStack.pop();
                    if (!inputCycle.compare(inputMinCycle)) {
                        inputMinCycleStack.push(inputCycle);
                    } else {
                        inputMinCycleStack.push(inputMinCycle);
                    }
                });
            }
            triggerParam.setType(DsTaskConstant.DATA_TRIGGER);
            triggerParam.setOutputGranularity(inputMinCycleStack.pop().getType());
            task.setTriggerParam(JSON.toJSONString(triggerParam).replace("output_granularity", "outputGranularity"));
            task.setDependTypes("[\"dataset\",\"event\"]");
        }
    }

    private static Boolean isStreaming(Task task) {
        boolean stream = TemplateEnum.valueOf(task.getTemplateCode()).isStreamingTemplate();
        if (stream) {
            com.ushareit.engine.param.RuntimeConfig runtimeConfig = DataCakeTaskConfig.paseRuntimeConfig(task.getRuntimeConfig());
            Boolean isBatchTask = runtimeConfig.getAdvancedParameters().getIsBatchTask();
            task.setIsStreamingTemplateCode(isBatchTask == null || !isBatchTask);
        } else {
            task.setIsStreamingTemplateCode(false);
        }
        return task.getIsStreamingTemplateCode();
    }

    private static String getQualifiedName(String datasetStr) {
        List<Dataset> datasets = JSON.parseArray(datasetStr, Dataset.class);
        if (datasets.size() == 0) {
            return StringUtils.EMPTY;
        }
        return datasets.stream()
                .map(data -> StringUtils.isNotEmpty(data.getId()) ? data.getId() : data.getMetadata().toString())
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining(SymbolEnum.COMMA.getSymbol()));
    }

}
