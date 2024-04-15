package com.ushareit.dstask.bean;

import com.ushareit.dstask.constant.TemplateEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Transient;
import java.sql.Timestamp;
import java.util.List;

/**
 * @author: licg
 * @create: 2021-05-12 15:24
 */
@Data
public class TaskBase extends DataEntity implements Cloneable {
    private static final long serialVersionUID = -1822338314549481430L;

    @Transient
    protected String displayTemplateCode;
    @Transient
    protected Boolean isStreamingTemplateCode;
    @ApiModelProperty(value = "artifact/online")
    @Column(name = "type_code")
    protected String typeCode;
    @ApiModelProperty(value = "工件地址")
    @Column(name = "jar_url")
    protected String jarUrl;
    @ApiModelProperty(value = "用户组名称")
    @Column(name = "user_group")
    protected String userGroup;
    @ApiModelProperty("应用是jar时，jar的主类名")
    @Column(name = "main_class")
    protected String mainClass;
    @ApiModelProperty("作业启动参数")
    @Column(name = "main_class_args")
    protected String mainClassArgs;
    @ApiModelProperty("'任务运行配置 json格式存储")
    @Column(name = "runtime_config")
    protected String runtimeConfig;
    @ApiModelProperty("输入源数据集")
    @Column(name = "input_dataset")
    protected String inputDataset;
    @ApiModelProperty("输出源数据集")
    @Column(name = "output_dataset")
    protected String outputDataset;
    @ApiModelProperty("依赖工件")
    @Column(name = "depend_artifacts")
    protected String dependArtifacts;
    @Transient
    protected List<ArtifactVersion> displayDependJars;
    @ApiModelProperty("唯一编码")
    @Column(name = "template_code")//任务类型
    private String templateCode;
    @ApiModelProperty(value = "应用名字")
    private String name;
    @ApiModelProperty("内容")
    @Column(name = "content")
    private String content;
    @ApiModelProperty("任务集群ID")
    @Column(name = "flink_cluster_id")
    private Integer flinkClusterId;
    @ApiModelProperty("应用所属租户组")
    @Column(name = "tenancy_code")
    private String tenancyCode;
    @Column(name = "input_guids")
    private String inputGuids;
    @Column(name = "output_guids")
    private String outputGuids;
    @Column(name = "online")
    private Integer online;
    @Column(name = "collaborators")
    private String collaborators;
    @Column(name = "release_time")
    private Timestamp releaseTime;
    @Column(name = "trigger_param")
    private String triggerParam;
    @Column(name = "event_depends")
    private String eventDepends;
    @Column(name = "depend_types")
    private String dependTypes;
    private Integer workflowId;
    private Integer source;

    @Transient
    private String commit;

    public void copy(TaskBase taskBase) {
        this.templateCode = taskBase.getTemplateCode();
        this.name = taskBase.getName();
        this.content = taskBase.getContent();
        this.mainClass = taskBase.getMainClass();
        this.mainClassArgs = taskBase.getMainClassArgs();
        this.runtimeConfig = taskBase.getRuntimeConfig();
        this.inputDataset = taskBase.getInputDataset();
        this.outputDataset = taskBase.getOutputDataset();
        this.dependArtifacts = taskBase.getDependArtifacts();
        this.flinkClusterId = taskBase.getFlinkClusterId();
        this.tenancyCode = taskBase.getTenancyCode();
        this.collaborators = taskBase.getCollaborators();
        this.inputGuids = taskBase.getInputGuids();
        this.outputGuids = taskBase.getOutputGuids();
        this.online = taskBase.getOnline();
        this.releaseTime = taskBase.getReleaseTime();
        this.setDescription(taskBase.getDescription());
        this.setCreateBy(taskBase.getCreateBy());
        this.setUpdateBy(taskBase.getUpdateBy());
        this.dependTypes = taskBase.getDependTypes();
        this.eventDepends = taskBase.getEventDepends();
        this.workflowId = taskBase.getWorkflowId();
        this.source = taskBase.getSource();
        this.userGroup = taskBase.getUserGroup();

        this.triggerParam = taskBase.getTriggerParam();
        this.displayTemplateCode = taskBase.getDisplayTemplateCode();
        this.isStreamingTemplateCode = TemplateEnum.valueOf(taskBase.getTemplateCode()).isStreamingTemplate();
        this.typeCode = taskBase.getTypeCode();
        this.jarUrl = taskBase.getJarUrl();
        this.content = taskBase.getContent();
        this.setCreateTime(taskBase.getCreateTime());
        this.setUpdateTime(taskBase.getUpdateTime());
    }
}
