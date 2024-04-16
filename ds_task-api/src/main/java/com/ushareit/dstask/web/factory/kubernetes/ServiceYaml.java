package com.ushareit.dstask.web.factory.kubernetes;

import org.apache.flink.util.Preconditions;

import java.util.Properties;

/**
 * @author wuyan
 * @date 2021/12/9
 */
public class ServiceYaml extends BaseYaml{
    public ServiceYaml(Properties jobProps) {
        super(jobProps);
        Preconditions.checkNotNull(jobProps.getProperty("uid"), "UID cannot be null!");
    }

    @Override
    public String replaceYamlVars(String fileContent) {
        fileContent = super.replaceYamlVars(fileContent)
                .replace("${UID}", jobProps.getProperty("uid"))
        ;
        return fileContent;
    }
}
