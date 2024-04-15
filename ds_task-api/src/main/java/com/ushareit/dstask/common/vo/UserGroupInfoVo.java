package com.ushareit.dstask.common.vo;

import lombok.Data;

import java.util.List;

@Data
public class UserGroupInfoVo {
    private Integer id;//用户组的id
    private String name;//名称
    private Integer owner;
    private String uuid;//后续隐藏角色用
    private String defaultHiveDbName;//默认的hive库。
    private boolean dbc;
    private List<String> ownerList;
}
