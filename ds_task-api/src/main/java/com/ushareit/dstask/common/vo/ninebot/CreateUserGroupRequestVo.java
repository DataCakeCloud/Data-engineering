package com.ushareit.dstask.common.vo.ninebot;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateUserGroupRequestVo {
    private String creator;
    private String userGroupName;
    private String defaultDatabase;
    private long createTime;
}
