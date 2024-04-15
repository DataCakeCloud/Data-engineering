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
public class AwsConfig {

    @Value("${aws.accessKey}")
    public String accessKey;
    @Value("${aws.secretKey}")
    public String secretKey;

    @Value("${aws.endpoint}")
    public String endpoint;

    @Value("${aws.bucket}")
    public String bucket;


}
