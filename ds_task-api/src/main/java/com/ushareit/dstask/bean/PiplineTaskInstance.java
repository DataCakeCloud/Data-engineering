package com.ushareit.dstask.bean;


import com.ushareit.dstask.constant.TemplateEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.Entity;


/**
 * @author: xuebotao
 * @create: 2023-10-13 15:24
 */
@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PiplineTaskInstance   {

    private String task_name;

    private String end_date;

    private String execution_date;

    private String spark_ui;

    private String genie_job_url;

    private String type;

    private String is_kyuubi_job;

    private String version;

    private String duration;

    private String genie_job_id;

    private String name;

    private String state;

    private String start_date;

    private String template_code;

    private String task_type;

    private String templateCode;

    private String granularity;

    private Integer taskId;

    private Integer try_number;

    private Integer workflowId;

    private Boolean isOwnerOrCollaborator;

    private Boolean online;

    private Boolean isScanSqlCmd;

    private Boolean isScanKibana;

    private Boolean isScanSparkUI;

    private String owner;

}
