package com.ushareit.dstask.web.factory.kubernetes;

import org.apache.flink.util.Preconditions;

import java.util.Properties;

/**
 * @author wuyan
 * @date 2021/12/9
 */
public class BaseYaml implements Yaml{
    protected Properties jobProps;

    public BaseYaml(Properties jobProps) {
        Preconditions.checkNotNull(jobProps, "jobmanager jobProp cannot be null!");
        Preconditions.checkNotNull(jobProps.getProperty("jobmanagerName"), "flink_jm_name cannot be null!");
        Preconditions.checkNotNull(jobProps.getProperty("jmTmLabelName"), "jm_tm_label_name cannot be null!");
        Preconditions.checkNotNull(jobProps.getProperty("owner"), "jm_tm_label_owner cannot be null!");
        Preconditions.checkNotNull(jobProps.getProperty("template"), "jm_tm_label_template cannot be null!");
        Preconditions.checkNotNull(jobProps.getProperty("id"), "jm_tm_label_id cannot be null!");
        this.jobProps = jobProps;
    }

    @Override
    public String replaceYamlVars(String fileContent) {
        fileContent = fileContent
                .replace("${flink_jm_name}", jobProps.getProperty("jobmanagerName"))
                .replace("${jm_tm_label_name}", jobProps.getProperty("jmTmLabelName"))
                .replace("${jm_tm_label_owner}", jobProps.getProperty("owner"))
                .replace("${jm_tm_label_template}", jobProps.getProperty("template"))
                .replace("${jm_tm_label_id}", jobProps.getProperty("id"))
        ;
        return fileContent;
    }

    protected String replaceIams(String fileContent) {
        fileContent = fileContent
                .replace("${awsIam}", jobProps.getProperty("awsIam", "BDP-Developer"))
                .replace("${huaweiIam}", jobProps.getProperty("huaweiIam", "BDP-Developer-Flink"))
        ;
        return fileContent;
    }
}
