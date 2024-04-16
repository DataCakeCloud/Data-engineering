package com.ushareit.dstask.web.factory.scheduled.param;

import com.ushareit.dstask.bean.UserBase;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class RegularAlert {
    private int offset;
    private String graularity;
    private String checkTime;
    private String triggerCondition;
    private List<String> alertType;
    private boolean notifyCollaborator;
    private List<UserBase> emailReceivers;
    private List<UserBase> wechatReceivers;
    private String wechatRobotKey;
}
