package com.ushareit.dstask.configuration;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @author xuebotao
 * @date 2023/04/20
 */
@Slf4j
@Data
@Configuration
public class DataCakeConfig {

    @Value("${datacake.external-role:true}")
    public Boolean dcRole;

    @Value("${spark-resources.middleResource}")
    public String middleResource;

    @Value("${spark-resources.largeResource}")
    public String largeResource;

    @Value("${spark-resources.extraLargeResource}")
    public String extraLargeResource;

    @Value("${spark-resources.nodeSelectorLifecycle}")
    public String nodeSelectorLifecycle;

    @Value("${spark.env.namespace}")
    public String namespace;

    @Value("${datacake.flink-sa:flink}")
    public String flinkSa;

    @Value("${datacake.flink-scheduler:default-scheduler}")
    public String flinkScheduler;

    @Value("${gateway-url.host}")
    public String gatewayHost;

    @Value("${gateway-rest-url.host}")
    public String gatewayRestHost;

    @Value("${cloud-resource.current-service:false}")
    public Boolean currentService;

    @Value("${server-url.host}")
    public String serverHost;

    @Value("${spark.mode}")
    public String sparkMode;


}
