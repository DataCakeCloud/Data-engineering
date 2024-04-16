package com.ushareit.dstask.web.factory.kubernetes;

import com.ushareit.dstask.web.factory.flink.param.RuntimeConfig;
import org.apache.flink.api.common.JobID;
import org.apache.flink.util.Preconditions;

import java.util.List;
import java.util.Properties;

/**
 * @author wuyan
 * @date 2021/12/9
 */
public class ConfigYaml extends BaseYaml{
    private List<RuntimeConfig.Kv> params;

    private static final String LOG4J = "\n  log4j.properties: |+\n" +
            "    # This affects logging for both user code and Flink\n" +
            "    rootLogger.level = INFO\n" +
            "    rootLogger.appenderRef.console.ref = ConsoleAppender\n" +
            "    rootLogger.appenderRef.rolling.ref = RollingFileAppender\n" +
            "\n" +
            "    # Uncomment this if you want to _only_ change Flink's logging\n" +
            "    #logger.flink.name = org.apache.flink\n" +
            "    #logger.flink.level = INFO\n" +
            "\n" +
            "    # The following lines keep the log level of common libraries/connectors on\n" +
            "    # log level INFO. The root logger does not override this. You have to manually\n" +
            "    # change the log levels here.\n" +
            "    logger.akka.name = akka\n" +
            "    logger.akka.level = INFO\n" +
            "    logger.kafka.name= org.apache.kafka\n" +
            "    logger.kafka.level = INFO\n" +
            "    logger.hadoop.name = org.apache.hadoop\n" +
            "    logger.hadoop.level = INFO\n" +
            "    logger.zookeeper.name = org.apache.zookeeper\n" +
            "    logger.zookeeper.level = INFO\n" +
            "\n" +
            "    # Log all infos to the console\n" +
            "    appender.console.name = ConsoleAppender\n" +
            "    appender.console.type = CONSOLE\n" +
            "    appender.console.layout.type = PatternLayout\n" +
            "    appender.console.layout.pattern = %d{yyyy-MM-dd HH:mm:ss,SSS} %-5p %-60c %x - %m%n\n" +
            "\n" +
            "    # Log all infos in the given rolling file\n" +
            "    appender.rolling.name = RollingFileAppender\n" +
            "    appender.rolling.type = RollingFile\n" +
            "    appender.rolling.append = false\n" +
            "    appender.rolling.fileName = ${sys:log.file}\n" +
            "    appender.rolling.filePattern = ${sys:log.file}.%i\n" +
            "    appender.rolling.layout.type = PatternLayout\n" +
            "    appender.rolling.layout.pattern = %d{yyyy-MM-dd HH:mm:ss,SSS} %-5p %-60c %x - %m%n\n" +
            "    appender.rolling.policies.type = Policies\n" +
            "    appender.rolling.policies.size.type = SizeBasedTriggeringPolicy\n" +
            "    appender.rolling.policies.size.size=100MB\n" +
            "    appender.rolling.strategy.type = DefaultRolloverStrategy\n" +
            "    appender.rolling.strategy.max = 10\n" +
            "\n" +
            "    # Suppress the irrelevant (wrong) warnings from the Netty channel handler\n" +
            "    logger.netty.name = org.apache.flink.shaded.akka.org.jboss.netty.channel.DefaultChannelPipeline\n" +
            "    logger.netty.level = OFF";
    public ConfigYaml(Properties jobProps, List<RuntimeConfig.Kv> params) {
        super(jobProps);
        Preconditions.checkNotNull(jobProps.getProperty("uid"), "UID cannot be null!");
        Preconditions.checkNotNull(jobProps.getProperty("flinkConfigName"), "flink_config_name cannot be null!");
        this.params = params;
    }

    @Override
    public String replaceYamlVars(String fileContent) {
        fileContent = super.replaceYamlVars(fileContent)
                .replace("${UID}", jobProps.getProperty("uid"))
                .replace("${flink_config_name}", jobProps.getProperty("flinkConfigName"))
                .replace("${initContainer_dependJars}", jobProps.getProperty("initContainerDependJars"))
                .replace("${flink_ha_cluster_name}", jobProps.getProperty("hAClusterName"))
                .replace("${flink_execution_packages}", jobProps.getProperty("flinkExecutionPackages"))
                .replace("${checkpoint_dir}", jobProps.getProperty("checkpointDir"))
                .replace("${savepoint_dir}", jobProps.getProperty("savepointDir"))
                .replace("${ha_storage_dir}", jobProps.getProperty("haStorageDir"))
                .replace("${node_selector}", jobProps.getProperty("nodeSelector"))
                .replace("${tolerations}", jobProps.getProperty("tolerations"))
                .replace("${namespace}", jobProps.getProperty("namespace"))
        ;

        if (params != null) {
            for (RuntimeConfig.Kv param : params) {
                fileContent = fileContent.concat("\n    ").concat(param.key).concat(": ").concat(param.value);
            }
        }

        fileContent = fileContent.concat(LOG4J);
        return fileContent;
    }
}
