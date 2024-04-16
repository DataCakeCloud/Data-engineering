package com.ushareit.dstask.bean;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.sql.Timestamp;
import java.util.List;

/**
 * @author wuyan
 * @date 2022/1/19
 */
@Data
@Entity
@Builder
@Table(name = "label_collect")
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@ApiModel("标签收藏类")
public class LabelCollect extends BaseEntity {
    @Column(name = "label_ids")
    private String labelIds;

    @Column(name = "create_by")
    private String createBy;

    @Column(name = "update_time")
    private Timestamp updateTime;

    @Transient
    private List<Label> labels;

}
