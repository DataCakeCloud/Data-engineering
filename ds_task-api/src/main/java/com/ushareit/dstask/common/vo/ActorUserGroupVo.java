package com.ushareit.dstask.common.vo;

import lombok.Data;

import java.util.List;

@Data
public class ActorUserGroupVo {
    private Integer id;//数据源id
    private List<UserGroupVo> userGroupVoList;
}
