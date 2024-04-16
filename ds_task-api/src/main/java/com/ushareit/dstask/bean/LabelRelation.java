package com.ushareit.dstask.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 标签和任务 工作流的关联表
 */
/*@Data
@Entity
@Builder
@Table(name = "label_relation")
@AllArgsConstructor
@NoArgsConstructor*/
public class LabelRelation extends BaseEntity{

    private Integer  lableId;

    private Integer releationId;//job或者 工作流

    private int type;//1task  2 workflow


}
