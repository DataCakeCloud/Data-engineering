package com.ushareit.dstask.constant;

import org.apache.commons.lang3.StringUtils;

/**
 * @author xuebotao
 * @date 2022-04-21
 */
public enum FeedbackLevelEnum {

    GENERAL("1", "一般"),
    SENIOR("2", "紧急");

    private String level;

    private String levelName;

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getLevelName() {
        return levelName;
    }

    public void setLevelName(String levelName) {
        this.levelName = levelName;
    }

    FeedbackLevelEnum(String level, String levelName) {
        this.level = level;
        this.levelName = levelName;
    }

    public static FeedbackLevelEnum of(String name) {
        for (FeedbackLevelEnum feedbackLevelEnum : FeedbackLevelEnum.values()) {
            if (StringUtils.equalsIgnoreCase(feedbackLevelEnum.name(), name)) {
                return feedbackLevelEnum;
            }
        }
        return null;
    }

}
