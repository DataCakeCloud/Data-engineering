package com.ushareit.dstask.configuration;


import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author fengxiao
 * @date 2022/7/28
 */
@Data
@Configuration
public class K8sConfig {


    private String env;

    private String kubeContext;

    private String host;

    private String caCrt;

    private String token;
    @Value("${datacake.namespace:datacake}")
    private String namespace;

    @Bean
    public KubernetesClient initClient() {

        ConfigBuilder configBuilder = new ConfigBuilder();
        DefaultKubernetesClient defaultKubernetesClient = new DefaultKubernetesClient(Config.autoConfigure("currentContext"));
        this.setKubeContext("currentContext");
//        DefaultKubernetesClient defaultKubernetesClient = new DefaultKubernetesClient(configBuilder.build());
//        this.setKubeContext(defaultKubernetesClient.getConfiguration().getCurrentContext().getName());
        this.setCaCrt(defaultKubernetesClient.getConfiguration().getCaCertData());
        this.setToken(defaultKubernetesClient.getConfiguration().getOauthToken());
        this.setHost(defaultKubernetesClient.getConfiguration().getMasterUrl());
        return new DefaultKubernetesClient(configBuilder.build());
    }
}
