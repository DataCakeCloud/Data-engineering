package com.ushareit.dstask.web.factory.scheduled.param;

import com.alibaba.fastjson.annotation.JSONField;
import com.ushareit.dstask.bean.CrontabParam;
import com.ushareit.dstask.entity.ScheduleJobOuterClass;
import lombok.Data;

@Data
public class TriggerParam {
    private String type; // cron,data
    @JSONField(name = "output_granularity", alternateNames = "outputGranularity")
    private String outputGranularity;
    @JSONField(name = "isIrregularSheduler")
    private Integer isIrregularSheduler = 1; // 1正常调度,2不定时调度
    private String crontab;
    @JSONField(name = "crontab_param", alternateNames = "crontabParam")
    private CrontabParam crontabParam;

    /**
     * 转换成于调度约定的数据格式
     */
    public ScheduleJobOuterClass.TriggerParam toPbTriggerParam() {
        ScheduleJobOuterClass.TriggerParam.Builder builder = ScheduleJobOuterClass.TriggerParam.newBuilder()
                .setType(this.getType())
                .setOuputGranularity(this.getOutputGranularity())
                .setIsIrregularSheduler(this.getIsIrregularSheduler());

        if (this.getCrontab() == null) {
            builder.setCrontab("00 00 * * *");
        } else {
            builder.setCrontab(this.getCrontab());
        }

        return builder.build();
    }
}
