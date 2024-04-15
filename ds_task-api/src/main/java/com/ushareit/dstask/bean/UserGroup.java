package com.ushareit.dstask.bean;

import com.google.api.client.util.Lists;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.List;

/**
 * 用户组
 * 初始化角色 privilege_single_group_role_uuid
 * privilege_single_group_user_uuid
 */
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Data
@Table(name = "user_group")
public class UserGroup extends DeleteEntity{
    private String name;//名称
    private String uuid;//后续隐藏角色用
    private Integer parentId;//用户组在哪个组织架构下 关联access_group表
    private String defaultHiveDbName;//默认的hive库。
    private String token;
    private String description;

    private transient List<String> org;
    private transient List<String> actorPrivileges= Lists.newArrayList();
    private transient List<UserGroupRelation> userGroupRelationList= Lists.newArrayList();

}
