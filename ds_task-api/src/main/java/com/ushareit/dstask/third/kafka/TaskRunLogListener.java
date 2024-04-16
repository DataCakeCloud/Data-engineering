package com.ushareit.dstask.third.kafka;

import com.alibaba.fastjson.JSONObject;
import com.ushareit.dstask.common.message.*;
import com.ushareit.dstask.configuration.DataCakeConfig;
import com.ushareit.dstask.web.socket.DebugTaskHandler;
import com.ushareit.dstask.web.utils.DataCakeConfigUtil;
import com.ushareit.dstask.web.utils.ValidateUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import static org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG;

/**
 * @author fengxiao
 * @date 2022/11/8
 */
@Slf4j
//@Component
public class TaskRunLogListener {

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    @Autowired
    private KafkaConfig kafkaConfig;
    @Resource
    private DataCakeConfig dataCakeConfig;
    @Autowired
    private DebugTaskHandler debugTaskHandler;
    @Value("${kafka.task-run.bootstrap-servers}")
    private String broker;
    @Value("${kafka.task-run.topic-name}")
    private String topic;
    private KafkaConsumer<String, String> kafkaConsumer;
    private boolean shutdown = false;

    @PostConstruct
    public void init() {
        if(dataCakeConfig.getDcRole()){
            broker="localhost:9092";
        }
        String hostName = System.getenv("HOSTNAME");
        String consumerGroup = String.format("ds-task_task-run_%s", StringUtils.defaultIfBlank(hostName,
                RandomStringUtils.randomAlphabetic(10)));
        log.info("consumer group for broker {} and topic {} is {}", broker, topic, consumerGroup);

        Map<String, Object> consumerConfig = new HashMap<>(kafkaConfig.getConfig());
        consumerConfig.put(BOOTSTRAP_SERVERS_CONFIG, broker);
        consumerConfig.put(GROUP_ID_CONFIG, consumerGroup);
        kafkaConsumer = new KafkaConsumer<>(consumerConfig);

        kafkaConsumer.subscribe(Pattern.compile(topic));
        executorService.execute(this::consume);
    }

    public void consume() {
        while (!shutdown) {
            ConsumerRecords<String, String> records = kafkaConsumer.poll(Duration.ofMillis(500L));
            records.iterator().forEachRemaining(record -> {
                try {
                    String message = record.value();
                    MessageCard messageCard = JSONObject.parseObject(message, MessageCard.class);
                    if (messageCard == null || CardEnum.of(messageCard.getType()) == null) {
                        log.error("not a valid message from schedule, message is {}", record.value());
                        return;
                    }

                    CardEnum cardType = CardEnum.of(messageCard.getType());
                    if (cardType == null) {
                        log.error("not supported message type for run log {}, message is {}", messageCard.getType(),
                                record.value());
                        return;
                    }

                    switch (cardType) {
                        case RUN_INFO:
                            TaskRunInfoMessage runInfoMessage = messageCard.parseToObject(TaskRunInfoMessage.class);
                            ValidateUtils.validate(runInfoMessage);
                            debugTaskHandler.sendMessage(runInfoMessage.getChatId(), ResponseMessage
                                    .success(runInfoMessage.toCard()));
                            break;
                        case STOP_INFO:
                            TaskStopMessage stopTaskMessage = messageCard.parseToObject(TaskStopMessage.class);
                            ValidateUtils.validate(stopTaskMessage);
                            debugTaskHandler.sendMessage(stopTaskMessage.getChatId(), ResponseMessage
                                    .success(stopTaskMessage.toCard()));
                            break;
                        case STATUS_INFO:
                            TaskStatusInfoMessage statusInfoMessage = messageCard.parseToObject(TaskStatusInfoMessage.class);
                            ValidateUtils.validate(statusInfoMessage);
                            debugTaskHandler.sendMessage(statusInfoMessage.getChatId(), ResponseMessage
                                    .success(statusInfoMessage.toCard()));
                            break;
                        default:
                            throw new RuntimeException(String.format("not supported card type, message is %s",
                                    record.value()));
                    }
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            });
        }
    }

    @PreDestroy
    public void shutdown() {
        this.shutdown = true;
        log.info("shutdown to stop consume kafka message");
    }
}
