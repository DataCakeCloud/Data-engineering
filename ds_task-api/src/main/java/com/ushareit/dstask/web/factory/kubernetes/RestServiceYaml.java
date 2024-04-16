package com.ushareit.dstask.web.factory.kubernetes;

import org.apache.flink.util.Preconditions;

import java.util.Properties;

/**
 * @author wuyan
 * @date 2021/12/9
 */
public class RestServiceYaml extends BaseYaml {

    public RestServiceYaml(Properties jobProps) {
        super(jobProps);
        Preconditions.checkNotNull(jobProps.getProperty("restServiceName"), "flink_jm_rest_service_name cannot be null!");
        Preconditions.checkNotNull(jobProps.getProperty("uid"), "UID cannot be null!");
    }

    @Override
    public String replaceYamlVars(String fileContent) {
        fileContent = super.replaceYamlVars(fileContent)
                .replace("${flink_jm_rest_service_name}", jobProps.getProperty("restServiceName"))
                .replace("${UID}", jobProps.getProperty("uid"))
        ;
        return fileContent;
    }
}
