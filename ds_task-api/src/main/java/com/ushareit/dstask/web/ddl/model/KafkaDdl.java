package com.ushareit.dstask.web.ddl.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Joiner;
import com.ushareit.dstask.bean.Actor;
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
            "   'scan.startup.mode' = '%s',\n" +
            "   'format' = 'json'";
    private static final String KAFKA_AUTH =
            " 'properties.security.protocol' = '%s',\n" +
                    " 'properties.sasl.mechanism' = 'SCRAM-SHA-512',\n" +
                    " 'properties.sasl.jaas.config' = '%s'";

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
        String bootstrap_servers = runtimeConfig.getBootstrap();
        String groupId = runtimeConfig.getConsumerGroup();
        String startConumerPosition = runtimeConfig.getStartConumerPosition();
        if (StringUtils.isEmpty(groupId)) {
            groupId = "datacake_" + taskId + "_conumer";
        }
        String format = String.format(getInfoDemo(), topic, bootstrap_servers, groupId, startConumerPosition);
        Actor sourceActor = runtimeConfig.getSourceActor();
        String configuration = sourceActor.getConfiguration();
        JSONObject connectionConfiguration = JSON.parseObject(configuration);

        JSONObject protocol = connectionConfiguration.getJSONObject("protocol");
        if (protocol != null) {
            String security_protocol = protocol.getString("security_protocol");
            if (StringUtils.isNotEmpty(security_protocol)) {
                String sasl_jaas_config = protocol.getString("sasl_jaas_config");
                if (StringUtils.isEmpty(sasl_jaas_config)) {
                    sasl_jaas_config = "";
                }
                String kafkaAuth = String.format(KAFKA_AUTH, security_protocol, sasl_jaas_config);
                return format + ",\n " + kafkaAuth;
            }
        }

        return format;
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
