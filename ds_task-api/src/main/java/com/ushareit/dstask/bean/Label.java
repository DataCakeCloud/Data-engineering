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
import java.util.List;

/**
 * @author wuyan
 * @date 2021/9/22
 */
@Data
@Entity
@Builder
@Table(name = "label")
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@ApiModel("标签类")
public class Label extends DeleteEntity{
    @Column(name = "name")
    private String name;

    @Column(name = "tasks")
    private String tasks;

    private String workflows;//工作流ids

    private Integer publish;//是否公开

    private String publishers;//公开的userid

    @Column(name = "tenancy_code")
    private String tenancyCode;

    @Transient
    private Boolean isMy = false;

    @Transient
    private List<Task> list;
}
