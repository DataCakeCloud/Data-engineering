package com.ushareit.dstask.bean;

import lombok.Data;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;

@Data
@Table(name = "user_group_relation")
public class UserGroupRelation {
    /**
     * id是主键，自动生成
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Integer userId;//用户id
    private String userName;
    private Integer userGroupId;//用户组id
    private Integer owner; //0不是  1 是
    private Timestamp createTime;
}
