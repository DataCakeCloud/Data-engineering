package com.ushareit.dstask.configuration;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @author fengxiao
 * @date 2021/2/3
 */
@Data
@Configuration
public class DingDingConfig {

    @Value("${dingDing.tokenUrl}")
    private String dingDingTokenUrl;
    @Value("${dingDing.url}")
    private String dingDingUrl;
    @Value("${dingDing.phoneUrl}")
    private String phoneUrl;
    @Value("${dingDing.username}")
    private String username;
    @Value("${dingDing.password}")
    private String password;

}
