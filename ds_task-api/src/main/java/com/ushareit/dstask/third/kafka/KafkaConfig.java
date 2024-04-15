package com.ushareit.dstask.third.kafka;

import com.google.common.collect.Lists;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.kafka.clients.consumer.ConsumerConfig.*;

/**
 * @author fengxiao
 * @date 2022/11/8
 */
@Component
public class KafkaConfig {

    private static final String DESERIALIZER_DEFAULT = "org.apache.kafka.common.serialization.StringDeserializer";
    private static final Map<String, Object> config = new HashMap<>();

    static {
        config.put(KEY_DESERIALIZER_CLASS_CONFIG, DESERIALIZER_DEFAULT);
        config.put(VALUE_DESERIALIZER_CLASS_CONFIG, DESERIALIZER_DEFAULT);
        config.put(ENABLE_AUTO_COMMIT_CONFIG, true);
        config.put(HEARTBEAT_INTERVAL_MS_CONFIG, 8000);
        config.put(SESSION_TIMEOUT_MS_CONFIG, 30000);
        List<String> assignmentList = Lists.newArrayList();
        //将这两种全部添加 在滚动升级时现在的线上消费不会停止 经测试用的是顺序靠前的 如果只替换为RoundRobinAssignor会 出现不兼容的分配策略 需要全部停止消费
        assignmentList.add("org.apache.kafka.clients.consumer.RoundRobinAssignor");
        assignmentList.add("org.apache.kafka.clients.consumer.RangeAssignor");
        config.put(PARTITION_ASSIGNMENT_STRATEGY_CONFIG, assignmentList);
        config.put(FETCH_MAX_WAIT_MS_CONFIG, 500);
        config.put(MAX_POLL_RECORDS_CONFIG, "5000");
        config.put(MAX_PARTITION_FETCH_BYTES_CONFIG, 10485760);
        config.put(METADATA_MAX_AGE_CONFIG, 120000);

        // 默认从最新消费
        config.put(AUTO_OFFSET_RESET_CONFIG, "latest");
    }

    public Map<String, Object> getConfig() {
        return config;
    }

}
