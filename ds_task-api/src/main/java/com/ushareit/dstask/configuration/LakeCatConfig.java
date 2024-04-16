package com.ushareit.dstask.configuration;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * @author fengxiao
 * @date 2023/2/14
 */
@Slf4j
@Data
@Configuration
public class LakeCatConfig {

    @Value("${lakecat-url.host}")
    private String lakeCatHost;
    @Value("${lakeCat.token}")
    private String token;

    @PostConstruct
    public void init() {
        log.info("lakeCat config info is {}", this);
    }

}
