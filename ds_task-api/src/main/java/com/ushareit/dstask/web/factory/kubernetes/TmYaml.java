package com.ushareit.dstask.web.factory.kubernetes;

import org.apache.flink.util.Preconditions;

import java.util.Properties;

/**
 * @author wuyan
 * @date 2021/12/9
 */
public class TmYaml extends BaseYaml{

    public TmYaml(Properties jobProps) {
        super(jobProps);
        Preconditions.checkNotNull(jobProps.getProperty("taskmanagerName"), "flink_tm_name cannot be null!");
        Preconditions.checkNotNull(jobProps.getProperty("uid"), "UID cannot be null!");
        Preconditions.checkNotNull(jobProps.getProperty("flinkConfigVolume"), "flink_config_volume cannot be null!");
        Preconditions.checkNotNull(jobProps.getProperty("flinkConfigName"), "flink_config_name cannot be null!");

    }

    @Override
    public String replaceYamlVars(String fileContent) {
        fileContent = super.replaceYamlVars(fileContent);
        fileContent = super.replaceIams(fileContent)
                .replace("${flink_tm_name}", jobProps.getProperty("taskmanagerName"))
                .replace("${UID}", jobProps.getProperty("uid"))
                .replace("${flink_config_volume}", jobProps.getProperty("flinkConfigVolume"))
                .replace("${flink_config_name}", jobProps.getProperty("flinkConfigName"))
                .replace("${tm_cpu}", jobProps.getProperty("tmCpu"))
                .replace("${tm_memory}", jobProps.getProperty("tmMemory"))
                .replace("${region}", jobProps.getProperty("region"))
                .replace("${env}", jobProps.getProperty("env"))
                .replace("${parallelism}", jobProps.getProperty("parallelism"))
                .replace("${flink_image}", jobProps.getProperty("flinkImage"))
                .replace("${pvc_volumeMounts}", jobProps.getProperty("pvc_volumeMounts"))
                .replace("${pvc_volumes}", jobProps.getProperty("pvc_volumes"))
                .replace("${nodeSelector_tolerations}", jobProps.getProperty("nodeSelector_tolerations"))
        ;
        return fileContent;
    }
}
