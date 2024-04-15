package com.ushareit.dstask.constant;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum BaseActionCodeEnum {
    /**
     * 任务类
     */
    CREATE("保存"),
    UPDATE("保存"),
    UPDATEANDSTART("保存并发布"),
    CREATEANDSTART("保存并发布"),

    COPY("复制"),
    BACKFILL("补数"),
    CLEAR("重算"),
    SWITCH("切换"),
    ONLINE("上线"),
    OFFLINE("下线"),
    START("启动"),
    STOP("停止"),
    DELETE("删除"),
    CANCEL("取消");

    private String message;

    public static List<BaseActionCodeEnum> actionCodeList = new ArrayList<>();
    public static Map<String, BaseActionCodeEnum> actionEnumMap = new HashMap<>();

    static {
        actionCodeList.add(BaseActionCodeEnum.UPDATE);
        actionCodeList.add(BaseActionCodeEnum.UPDATEANDSTART);
        actionCodeList.add(BaseActionCodeEnum.CREATEANDSTART);
        actionCodeList.add(BaseActionCodeEnum.CREATE);
        actionCodeList.add(BaseActionCodeEnum.COPY);
        actionCodeList.add(BaseActionCodeEnum.ONLINE);
        actionCodeList.add(BaseActionCodeEnum.OFFLINE);
        actionCodeList.add(BaseActionCodeEnum.DELETE);
        actionCodeList.add(BaseActionCodeEnum.CLEAR);
        actionCodeList.add(BaseActionCodeEnum.STOP);
        for (BaseActionCodeEnum actionCodeEnum : actionCodeList) {
            actionEnumMap.put(actionCodeEnum.name(), actionCodeEnum);
            actionEnumMap.put(actionCodeEnum.getMessage(), actionCodeEnum);
        }
    }


    BaseActionCodeEnum(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return MessageFormat.format("ResponseCode.proto:{0},{1}.", this.name(), this.message);
    }

}
