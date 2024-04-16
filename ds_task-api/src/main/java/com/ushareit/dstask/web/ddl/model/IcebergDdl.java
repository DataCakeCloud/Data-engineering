package com.ushareit.dstask.web.ddl.model;

import com.google.common.base.Joiner;
import com.ushareit.dstask.bean.CloudResouce;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.constant.DsTaskConstant;
import com.ushareit.dstask.constant.SymbolEnum;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.service.impl.TaskServiceImpl;
import com.ushareit.dstask.web.ddl.SqlDdl;
import com.ushareit.dstask.web.ddl.metadata.Table;
import com.ushareit.dstask.web.utils.DataCakeConfigUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author: licg
 * @create: 2021-05-12 15:24
 */
@Slf4j
public class IcebergDdl extends SqlDdl{
    private static final String DATEPART = "datapart";

    private static final String HOUR = "`hour`";

    public String formatVersion = "1";

    public String primaryKey;

    public String partitionKeys;

    public TaskServiceImpl taskServiceImp;

    private final String INFO_DEMO =
            "   'write.upsert.enabled'='true',\n" +
            "   'write.format.default'='parquet',\n" +
            "   'format-version' = '%s',\n" +
            "   'location' = '%s'";

    public IcebergDdl(Table table, String formatVersion, String primaryKey, String partitionKeys, TaskServiceImpl taskServiceImp) {
        super(table);
        this.formatVersion = formatVersion;
        this.primaryKey = primaryKey;
        this.partitionKeys = partitionKeys;
        this.taskServiceImp = taskServiceImp;
    }

    @Override
    public String getSchema() throws Exception {
        String schema = super.getSchema();
//        if (StringUtils.isNotEmpty(primaryKey)) {
//            schema += String.format(",\n   %s STRING,\n   %s STRING,\n   PRIMARY KEY(datapart, `hour`, %s)  NOT ENFORCED ", DATEPART, HOUR, primaryKey);
//        }else {
//            schema += String.format(",\n   %s STRING,\n   %s STRING,\n   PRIMARY KEY(datapart, `hour`)  NOT ENFORCED ", DATEPART, HOUR);;
//        }
        String priKey ="";
        if(StringUtils.isNotEmpty(primaryKey)){
            priKey= priKey+ primaryKey;
        }
        if(StringUtils.isNotEmpty(partitionKeys)){
            priKey= priKey+ partitionKeys;
        }

        if (StringUtils.isNotEmpty(priKey)) {
            schema += String.format(",\n    PRIMARY KEY(%s)  NOT ENFORCED ", processSpecialFields(priKey));
        }
        return schema ;
    }

    public String processSpecialFields(String fields){
        if(StringUtils.isEmpty(fields)){
            return "";
        }
        if(!fields.contains(".")){
            return "`"+fields.trim()+"`";
        }
        String[] split = fields.split("\\.");
        String collect = Arrays.asList(split).stream().map(data -> "`" + data.trim() + "`")
                .collect(Collectors.joining(","));
        return collect;
    }

    @Override
    public String getPartition() throws Exception {
//        if (!StringUtils.isEmpty(partitionKeys)) {
//            return String.format("PARTITIONED BY (%s, %s, %s)", DATEPART, HOUR, partitionKeys);
//        }
//        return String.format("PARTITIONED BY (%s, %s)", DATEPART, HOUR);

        if (!StringUtils.isEmpty(partitionKeys)) {
            return String.format(" PARTITIONED BY (%s) ", partitionKeys);
        }
        return " ";
    }

    @Override
    public String getInfo() throws Exception {
        String tableLocation = getRegionLocation(table.getRegion()) + table.getName() + "/";
        return String.format(INFO_DEMO, formatVersion, tableLocation);
    }

    @Override
    public String getName() throws Exception {
        String replace = table.getName().replace("-", "_");
        StringBuilder tableName = new StringBuilder();
        String[] split = replace.split("\\.");
        for (int i = 0; i < split.length; i++) {
            tableName.append("`").append(split[i]).append("`");
            if (i != split.length - 1) {
                tableName.append(".");
            }
        }
        return tableName.toString();
    }

    private String getRegionLocation(String region) {
        CloudResouce.DataResource cloudResource = DataCakeConfigUtil.getCloudResourcesService().getCloudResource(region);
        String location = cloudResource.getStorage() + "datacake/";
        return location;
    }
}
