package com.ushareit.dstask.web.factory.kubernetes;

import org.apache.flink.util.Preconditions;

import java.util.Properties;

/**
 * @author wuyan
 * @date 2021/12/9
 */
public class JmYaml extends BaseYaml {

    public JmYaml(Properties jobProps) {
        super(jobProps);
        Preconditions.checkNotNull(jobProps.getProperty("jmArgs"), "jm_args cannot be null!");
        Preconditions.checkNotNull(jobProps.getProperty("flinkConfigVolume"), "flink_config_volume cannot be null!");
        Preconditions.checkNotNull(jobProps.getProperty("flinkConfigName"), "flink_config_name cannot be null!");
    }

    @Override
    public String replaceYamlVars(String fileContent) {
        fileContent = super.replaceYamlVars(fileContent);
        fileContent = super.replaceIams(fileContent)
                .replace("${jm_args}", jobProps.getProperty("jmArgs"))
                .replace("${flink_config_volume}", jobProps.getProperty("flinkConfigVolume"))
                .replace("${flink_config_name}", jobProps.getProperty("flinkConfigName"))
                .replace("${region}", jobProps.getProperty("region"))
                .replace("${env}", jobProps.getProperty("env"))
                .replace("${flink_image}", jobProps.getProperty("flinkImage"))
                .replace("${nodeSelector_tolerations}", jobProps.getProperty("nodeSelector_tolerations"))
        ;
        return fileContent;
    }
}
