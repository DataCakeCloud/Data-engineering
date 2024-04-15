package com.ushareit.dstask.web.factory.scheduled.param;

import com.ushareit.dstask.bean.UserBase;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
@Data
@AllArgsConstructor
public class AlertModel {
    private List<String> alertType;
    private boolean notifyCollaborator;
    private List<UserBase> emailReceivers;
    private List<UserBase> wechatReceivers;
    private String wechatRobotKey;


    @Override
    public String toString(){
        return String.format("alertType: %s,notifyCollaborator: %s",alertType,notifyCollaborator);
    }
}
