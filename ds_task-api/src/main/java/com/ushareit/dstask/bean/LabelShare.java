package com.ushareit.dstask.bean;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 公开表
 */
/*@Data
@Entity
@Builder
@Table(name = "label_share")
@AllArgsConstructor
@NoArgsConstructor*/
public class LabelShare extends BaseEntity{

    private Integer lableId;

    private String userId;//公开的人员

    private String tasks;//任务的ids

    private String workflows;//工作流的id
}
