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
public class DataCakeServiceConfig {

    @Value("${ds-pipeline.host}")
    public String pipelineHost;

    @Value("${console-server-url.host}")
    private String CLUSTER_MANAGER_URL;

}
