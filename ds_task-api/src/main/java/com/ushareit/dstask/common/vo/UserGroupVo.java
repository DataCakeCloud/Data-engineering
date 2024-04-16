package com.ushareit.dstask.common.vo;

import com.google.api.client.util.Lists;
import com.ushareit.dstask.bean.UserGroupRelation;
import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

@Data
public class UserGroupVo {
    private Integer id;//用户组的id
    private String name;//名称
    private String uuid;//后续隐藏角色用
    private Integer parentId;//用户组在哪个组织架构下 关联access_group表
    private String defaultHiveDbName;//默认的hive库。
    private String token;
    private String description;
    private String createBy;
    private Timestamp createTime;

    private List<String> org;
    private List<Integer> orgId;
    private List<String> actorPrivileges= Lists.newArrayList();
    private List<UserGroupRelation> userGroupRelationList= Lists.newArrayList();
}
