package com.ushareit.dstask.grpc;

import io.grpc.Status;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tag;
import io.prometheus.client.Histogram;

import java.util.Arrays;

/**
 * @author fengxiao
 * @date 2023/2/28
 */
public class GrpcMetricUtils {

    private static final Histogram GRPC_REQUEST_COST_HISTOGRAM =
            registryHistogramWithBuckets("method_cost_latency", new String[]{"method_name"},
                    1, 5, 10, 100, 300, 600, 1000, 3000, 5000, 10000, 30000, 60000, 120000, 300000);

    public static void count(String method, Status status) {
        Metrics.counter("data-cake.grpc.access", Arrays.asList(Tag.of("method", method),
                Tag.of("status", status.getCode().name()))).increment();
    }

    /**
     * thread task latency
     *
     * @param method  grpc 方法名
     * @param latency 线程任务执行时间 单位 ms
     */
    public static void grpcCostHistogram(String method, double latency) {
        histogramCount(GRPC_REQUEST_COST_HISTOGRAM, latency, method);
    }

    /**
     * histogram统计
     *
     * @param histogram   histogram
     * @param diff        diff
     * @param labelValues labelValues
     */
    public static void histogramCount(Histogram histogram, double diff, String... labelValues) {
        histogram.labels(labelValues).observe(diff);
    }

    /**
     * 指定桶的histogram
     *
     * @param name       histogram name
     * @param labelNames 标签
     * @param buckets    指定的桶
     * @return Histogram
     */
    public static Histogram registryHistogramWithBuckets(String name, String[] labelNames, double... buckets) {
        return Histogram.build().name(name).buckets(buckets).labelNames(labelNames).help(name).register();
    }

}
