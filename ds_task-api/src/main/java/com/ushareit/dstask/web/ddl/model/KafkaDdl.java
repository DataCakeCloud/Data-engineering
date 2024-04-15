package com.ushareit.dstask.web.ddl.model;

import com.google.common.base.Joiner;
import com.ushareit.dstask.constant.SymbolEnum;
import com.ushareit.dstask.web.ddl.SqlDdl;
import com.ushareit.dstask.web.ddl.metadata.Table;
import com.ushareit.dstask.web.factory.flink.param.WithInfo;
import com.ushareit.dstask.web.utils.UuidUtil;
import org.apache.commons.lang3.StringUtils;

/**
 * @author: wuyan
 * @create: 2021-05-12 15:24
 */
public class KafkaDdl extends SqlDdl{
    protected Integer taskId = 0;
    private static final String INFO_DEMO =
            "   'connector' = 'kafka',\n" +
            "   'topic' = '%s',\n" +
            "   'properties.bootstrap.servers' = '%s',\n" +
            "   'properties.group.id' = '%s',\n" +
            "   'scan.startup.mode' = 'latest-offset',\n" +
            "   'format' = 'json'";

    public KafkaDdl(Table table, Integer taskId) {
        super(table);
        this.taskId = taskId;
    }

    protected String getInfoDemo() {
        return INFO_DEMO;
    }

    @Override
    protected String transformDataType(String type) {
        return super.transformDataType(type);
    }

    /** 元数据返回的信息快照 时间2021-12-1 以防元数据返回变了说是ds代码的问题
     * guid: "8ef74354-2b36-46bb-905e-7fb34d9e413e"
     * name: "rst_web_report"
     * owner: "songxin"
     * qualifiedName: "kafka.sg2.rst.rst_web_report"
     * topic: "rst_web_report"
     * typeName: "kafka_topic"
     * uri: "prod.report.web.rst.sg2.mq"
     * @return
     * @throws Exception
     */
    @Override
    public String getInfo() throws Exception {
        String topic = table.getName();
        String bootstrap_servers = table.getParameters().get("uri") + ":9092";
        String groupId = (topic + "_kafka_" + taskId).replaceAll("-", "_");
        if (runtimeConfig != null && !StringUtils.isEmpty(runtimeConfig.getGroupId())) {
            groupId = runtimeConfig.getGroupId();
        }
        return String.format(getInfoDemo(), topic, bootstrap_servers, groupId);
    }

    @Override
    public String getSchema() throws Exception {
        return super.getSchema() + ",\n proc_time as PROCTIME() ";
    }

    @Override
    public String getDisplayTableName()  throws Exception {
        return Joiner.on(SymbolEnum.UNDERLINE.getSymbol())
                .skipNulls()
                .join("kafka", getName(),"inc_hourly").toLowerCase();
    }
}
