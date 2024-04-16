package com.ushareit.dstask.web.factory.scheduled.param;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.support.spring.PropertyPreFilters;
import com.ushareit.dstask.web.factory.scheduled.ScheduledJob;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
// TODO:参数越写越长.... 待优化
public class ScheduledJobParam {

    private String name;
    private String emails;
    private String owner;
    private int retries;

    @JSONField(name = "max_active_runs", alternateNames = "maxActiveRuns")
    private int maxActiveRuns;

    @JSONField(name = "email_on_success", alternateNames = "emailOnSuccess")
    private boolean emailOnSuccess;

    @JSONField(name = "email_on_failure", alternateNames = "emailOnFailure")
    private boolean emailOnFailure;

    @JSONField(name = "email_on_start", alternateNames = "emailOnStart")
    private boolean emailOnStart;

    @JSONField(name = "input_datasets", alternateNames = "datasets")
    private List<Dataset> datasets;

    @JSONField(name = "output_datasets", alternateNames = "outputDatasets")
    private List<Dataset> outputDatasets;

    @JSONField(name = "start_date", alternateNames = "startDate")
    private String startDate;

    @JSONField(name = "end_date", alternateNames = "endDate")
    private String endDate;

    @JSONField(name = "execution_timeout", alternateNames = "executionTimeout")
    private Integer executionTimeout;

    @JSONField(name = "extra_params", alternateNames = "extraParams")
    private Map<String, String> extraParams;

    @JSONField(name = "task_items", alternateNames = "taskItems")
    private List<Map<String, Object>> taskItems;

    @JSONField(name = "event_depends", alternateNames = "eventDepends")
    private List<EventDepend> eventDepends;

    @JSONField(name = "trigger_param", alternateNames = "triggerParam")
    private TriggerParam triggerParam;

    @JSONField(name = "depend_types", alternateNames = "dependTypes")
    private String dependTypes;

    @JSONField(name = "version")
    private Integer version;

    public ScheduledJobParam(ScheduledJob scheduledJob, List<Map<String, Object>> taskItems) {
        this.name = scheduledJob.getName();
        this.emails = scheduledJob.getEmails();
        this.owner = scheduledJob.getNotifiedOwner();
        this.retries = scheduledJob.getRetries();
        this.maxActiveRuns = scheduledJob.getMaxActiveRuns();
        this.emailOnSuccess = scheduledJob.isEmailOnSuccess();
        this.emailOnFailure = scheduledJob.isEmailOnFailure();
        this.emailOnStart = scheduledJob.isEmailOnStart();
        this.datasets = scheduledJob.getInputDatasets();
        this.outputDatasets = scheduledJob.getOutputDatasets();
        this.startDate = scheduledJob.getStartDate();
        this.endDate = scheduledJob.getEndDate();
        this.executionTimeout = scheduledJob.getExecutionTimeout();
        this.extraParams = scheduledJob.getExtraParam();
        this.eventDepends = scheduledJob.getEventDepends();
        this.triggerParam = scheduledJob.getTriggerParam();
        this.dependTypes = scheduledJob.getDependTypes();
        this.version = scheduledJob.getVersion();

        this.taskItems = taskItems;
    }

    /**
     * 过滤掉不需要的字段
     */
    public PropertyPreFilters.MySimplePropertyPreFilter getFilterField() {
        PropertyPreFilters filters = new PropertyPreFilters();
        PropertyPreFilters.MySimplePropertyPreFilter excludefilter = filters.addFilter();
        if (executionTimeout == 0) {
            excludefilter.addExcludes("execution_timeout");
        }
        return excludefilter;
    }
}
