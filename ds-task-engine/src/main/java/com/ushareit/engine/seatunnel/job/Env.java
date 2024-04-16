package com.ushareit.engine.seatunnel.job;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

@Data
public class Env {
    @JSONField(name = "execution.parallelism")
    private int executionParallelism = 1;
    @JSONField(name = "job.mode")
    private String jobMode = "BATCH";
    @JSONField(name = "checkpoint.interval")
    private int checkpointInterval = 10000;
}
