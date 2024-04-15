package com.ushareit.dstask.bean;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author wuyan
 * @date 2022/6/13
 */
@Data
@Entity
@Builder
@Table(name = "access_group")
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@ApiModel("组类")
public class AccessGroup extends DeleteEntity {
    @NotBlank(message = "名字不能为空")
    @ApiModelProperty(value = "名字")
    private String name;

    private String eName;

    private Integer tenantId;

    private Integer parentId;

    private Integer type = 0;

    private Integer userId;

    private Integer hierarchy;

    // 0:是 ，1：不是
    private Integer isLeader;

    private String director;//负责人

    @Transient
    private String userName;

    @Transient
    private Integer userNum;

    //    @Transient
    private List<AccessGroup> children = new ArrayList<>();

    @Transient
    private List<AccessUser> users;

    @Transient
    private Integer userSize;

    @Transient
    private Boolean hasChildren = true;

    @Transient
    private Integer groupId;

    @Transient
    private String isLeaderFlag;

    @Transient
    private List<Integer> ids;

    @Transient
    private Set<String> userSet;

    @Transient
    private Boolean isHasChildrenDir = false;



}
