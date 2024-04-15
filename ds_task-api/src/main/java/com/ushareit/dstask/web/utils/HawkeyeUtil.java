package com.ushareit.dstask.web.utils;


import com.alibaba.fastjson.JSON;
import com.ushareit.dstask.constant.CommonConstant;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import com.ushareit.dstask.utils.HttpUtil;
import com.ushareit.dstask.bean.HawKeyeResult;
import com.ushareit.dstask.web.vo.BaseResponse;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class HawkeyeUtil {

    public static String URL = "http://prod.landing-app-select.sgt.sg2.api:8481/select/0/prometheus/api/v1/query";

    public static String RANGE_URL = "http://prod.landing-app-select.sgt.sg2.api:8481/select/0/prometheus/api/v1/query_range";

    public static String QUERY = "query";

    public static HawKeyeResult requestHawkeye(String url, Map<String, String> param) {
        if (!param.isEmpty() && param.containsKey(QUERY)) {
            BaseResponse response = HttpUtil.get(url, param, null);
            return JSON.parseObject(response.getData().toString(), HawKeyeResult.class);
        }
        return null;
    }

    public static HawKeyeResult requestHawkeye(Map<String, String> param) {
        return requestHawkeye(URL, param);
    }


//    public static void main(String[] args) {
//
//        //测试hawkeye get请求
//        Map<String, String> param = new HashMap<>();
////        param.put("query", "sum(kafka_consumergroup_lag{consumergroup=\"sprs_funu_event-log-group01\",topic=\"sprs_funu_event-log\"}) by (consumergroup,topic)");
////        param.put("query","avg(flink_taskmanager_job_task_operator_recordDeserializeTime{app=\"newstream-likeitlite-userinfo\",cluster=\"bdp-flink-sg2-prod\",quantile=\"0.99\"}) by (operator_name)\n"); //,instance="10.21.57.9:9308"
////        param.put("query", "sum(delta(kafka_consumergroup_current_offset{project='mq-queue3-sg2-kafka',topic='sprs_likeitlite_userinfolog', consumergroup='buzznews_likeitlite_userinfo_newbucket'}[5m]) / 5) by (topic, consumer_group)");
////        param.put("query", "(flink_taskmanager_job_task_operator_KafkaConsumer_topic_partition_committedOffsets{app=\"newstream-likeitlite-userinfo\",job_id=\"00000000000000000000000000000000\"},topoic)"); ,instance=~'10.21.17.215:[0-9]*'
////        param.put("query","(kafka_brokers{project=\"mq-queue3-sg2-kafka\"}, instance)");
////        param.put("query","sum(delta(kafka_consumergroup_current_offset{project='metis-common-sg2-kafka',topic='metis_web_jollymax_sensor', consumergroup='flink-11334',instance=~'10.21.45.175:9308|10.21.4.254:9308'}[5m]) / 5) by (topic, consumer_group)");
////        param.put("query","sum(delta(kafka_consumergroup_current_offset{project='metis-common-sg2-kafka',topic='metis_web_jollymax_sensor', consumergroup='flink-11334'}[5m]) / 5) by (topic, consumer_group)");
////        param.put("query", "sum(rate(kafka_topic_partition_current_offset{project='mq-queue3-sg2-kafka', topic='rcs_rms_ch_decision_record, instance=~'10.21.62.89:9308|10.21.29.105:9308|10.21.40.190:9308|10.21.2.156:9308|10.21.56.55:9308|10.21.57.9:9308|10.21.3.52:9308|10.21.4.245:9308' }[1h])) / min(1000000 / (avg(flink_taskmanager_job_task_operator_recordDeserializeTime {app='dev-decision-record-clickhouse',cluster='test',quantile='0.99'} + flink_taskmanager_job_task_operator_recordProcessTime{app='dev-decision-record-clickhouse',cluster='test',quantile='0.99'}) by (operator_name)))\n");
//        // param.put("query","count(sum(kafka_log_log_size{project='metis-common-sg2-kafka',topic='metis_web_jollymax_sensor'}) by (partition))");
//        //param.put("query","sum(rate(kafka_topic_partition_current_offset{project='mq-mw-sg2-test-kafka', topic='flink-test_fx-data3', instance=~'10.20.14.238:9308' }[1h])) / min(1000000 / (avg(flink_taskmanager_job_task_operator_recordDeserializeTime {app='flink-scale-metrics-test',cluster='shareit-cce-test',quantile='0.99'} + flink_taskmanager_job_task_operator_recordProcessTime{app='flink-scale-metrics-test',cluster='shareit-cce-test',quantile='0.99'}) by (operator_name)))");
////        param.put("query","count(flink_taskmanager_job_task_operator_assigned_partitions{app=\"flink-scale-metrics-test\",cluster=\"shareit-cce-test\"})");
////        param.put("query", "count(sum(kafka_log_log_size{project='mq-mw-sg2-test-kafka',topic='flink-test_fx-data3'}) by (partition))");
//        //param.put("query","sum(rate(kafka_topic_partition_current_offset{project='mq-mw-sg2-test-kafka', topic='flink-test_fx-data3', instance=~'10.20.14.238:9308' }[1h])) / min(1000000 / (avg(flink_taskmanager_job_task_operator_recordDeserializeTime {app='flink-scale-metrics-test',cluster='shareit-cce-test',quantile='0.99'} + flink_taskmanager_job_task_operator_recordProcessTime{app='flink-scale-metrics-test',cluster='shareit-cce-test',quantile='0.99'}) by (operator_name)))");
//        param.put("query", "sum((kafka_consumergroup_lag{project='mq-mw-sg2-test-kafka', topic='flink-test_fx-data3',consumergroup='test', instance=~'10.20.14.238:9308'})) by (topic, consumergroup)");
////        param.put("query","sum((kafka_consumergroup_lag{project='mq-mw-sg2-test-kafka', topic='flink_test_fx_data3', consumergroup='test', instance=~'10.20.14.238:9308'})) by (topic, consumergroup)");
//        String url = "http://prod.landing-app-select.sgt.sg2.api:8481/select/0/prometheus/api/v1/query";
//        long time = new Date().getTime();
//        Integer nowTimestamp = Integer.valueOf(String.valueOf(time / 1000));
//        Integer earliestTimestamp = nowTimestamp - 1800;
//        long last = nowTimestamp + 300;
//        System.out.println(String.valueOf(earliestTimestamp));
//        System.out.println(last);
//        param.put("start", "1642588396");
//        param.put("end", "1642590196");
//        System.out.println(url);
//        Map<String, String> headers = new HashMap<>(1);
//        headers.put(CommonConstant.AUTHENTICATION_HEADER, InfTraceContextHolder.get().getAuthentication());
//        BaseResponse response = HttpUtil.get(HawkeyeUtil.RANGE_URL, param, null);
//        System.out.println(response);
//        HawKeyeResult hawKeyeResult = JSON.parseObject(response.getData().toString(), HawKeyeResult.class);
//        System.out.println(response.getData().toString());
//        System.out.println(hawKeyeResult.toString());
//        System.out.println(hawKeyeResult.getData().getResult().get(0).getValue()[1]);
//        System.out.println(hawKeyeResult.getData().getResult().get(0).getMetric().getInstance());
////        long time = new Date().getTime();
//        System.out.println(time);
//    }
}
