package com.ushareit.dstask.web.ddl.model;

import com.google.common.base.Joiner;
import com.ushareit.dstask.constant.SymbolEnum;
import com.ushareit.dstask.web.ddl.SqlDdl;
import com.ushareit.dstask.web.ddl.metadata.Table;
import com.ushareit.dstask.web.factory.flink.param.WithInfo;
import com.ushareit.dstask.web.utils.UuidUtil;
import org.apache.commons.lang3.StringUtils;

import javax.swing.table.TableStringConverter;

/**
 * @author: licg
 * @create: 2021-05-12 15:24
 */
public class MetisDdl extends SqlDdl{
    private Integer taskId = 0;
    private static final String METIS_MESSAGE_CONTEXT = " metis_message_context ROW<metis_timestamp TIMESTAMP(3), metis_timestamp_raw STRING, client_ip STRING, route_key ROW<groupName STRING, appName STRING, logStore STRING, clusterName STRING>>";

    private final String INFO_DEMO =
            "   'connector' = 'metis',\n" +
            "   'group' = '%s',\n" +
            "   'project' = '%s',\n" +
            "   'logStore' = '%s',\n" +
            "   'region' = '%s',\n" +
            "   'properties.group.id' = '%s',\n" +
            "   'properties.auto.offset.reset' = 'latest'";
//            "   'properties.auto.offset.reset' = 'earliest'";

    public MetisDdl(Table table, Integer taskId) {
        super(table);
        this.taskId = taskId;
    }

    @Override
    protected String transformDataType(String type) {
        return super.transformDataType(type);
    }

    @Override
    public String getName() throws Exception {
        String appName = table.getParameters().get("appName");
        String groupName = table.getParameters().get("groupName");
        String logStore = table.getParameters().get("logStore");

        return (appName + "_" + groupName + "_" + logStore).replaceAll("-", "_");
    }

    @Override
    public String getSchema() throws Exception {
        return super.getSchema() + ",\n" + METIS_MESSAGE_CONTEXT;
    }

    @Override
    public String getInfo() throws Exception {
        String appName = table.getParameters().get("appName");
        String groupName = table.getParameters().get("groupName");
        String logStore = table.getParameters().get("logStore");
        String region = table.getParameters().get("region");
        String groupId = Joiner.on(SymbolEnum.UNDERLINE.getSymbol())
                .skipNulls()
                .join(WithInfo.METIS_CONNECTOR, appName, groupName, logStore, region, taskId).toLowerCase().replaceAll("-", "_");
        if (runtimeConfig != null && !StringUtils.isEmpty(runtimeConfig.getGroupId())) {
            groupId = runtimeConfig.getGroupId();
        }
        return String.format(INFO_DEMO, groupName, appName, logStore, region, groupId);
    }

    @Override
    public String getDisplayTableName()  throws Exception {
        return Joiner.on(SymbolEnum.UNDERLINE.getSymbol())
                .skipNulls()
                .join(WithInfo.METIS_CONNECTOR, getName(),"inc_hourly").toLowerCase();
    }
}
