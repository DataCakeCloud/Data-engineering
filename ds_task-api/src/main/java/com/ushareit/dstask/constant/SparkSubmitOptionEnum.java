package com.ushareit.dstask.constant;

public enum SparkSubmitOptionEnum {
    CONF("--conf"),
    DRIVER_CORES("--driver-cores"),
    DRIVER_MEMORY("--driver-memory"),
    EXECUTOR_MEMORY("--executor-memory"),
    FILES("--files"),
    JARS("--jars"),
    PROPERTIES_FILE("--properties-file"),
    PY_FILES("--py-files"),
    TOTAL_EXECUTOR_CORES("--total-executor-cores"),
    ARCHIVES("--archives"),
    EXECUTOR_CORES("--executor-cores"),
    NUM_EXECUTORS("--num-executors"),
    PACKAGES("--packages"),
    REPOSITORIES("--repositories"),
    O("-o"),
    SEP("-sep"),
    NAME("--name"),
    EXCLUDS_PACKAGES("--exclude-packages"),
    DRIVER_JAVA_OPTIONS("--driver-java-options"),
    DRIVER_LIBRARY_PATH("--driver-library-path"),
    DRIVER_CLASS_PATH("--driver-class-path"),
    PARTITION_NUM("--partitions_num"),
    VERBEROS("--verbose"),
    PROXY_USER("--proxy-user"),
    VERSION("--version"),
    SUPERVISE("--supervise"),
    KILL("--kill"),
    STATUS("--status"),
    PRINCIPAL("--principal"),
    KEYTAB("--keytab"),
    QUEUE("--queue");


    private String option;

    SparkSubmitOptionEnum(String option) {
        this.option = option;
    }

    public static boolean isValid(String option) {
        for (SparkSubmitOptionEnum tmpType : SparkSubmitOptionEnum.values()) {
            if (tmpType.option.equalsIgnoreCase(option)) {
                return true;
            }
        }
        return false;
    }

}
