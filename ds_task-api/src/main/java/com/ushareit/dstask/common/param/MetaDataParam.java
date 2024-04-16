package com.ushareit.dstask.common.param;


import com.alibaba.fastjson.JSONObject;
import com.ushareit.dstask.constant.SourceTypeEnum;
import com.ushareit.dstask.web.factory.scheduled.param.Dataset;
import com.ushareit.dstask.web.metadata.MetaDataManager;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Component
public class MetaDataParam {

    //airbyte数据源id
    private String actorId;

    private String region;

    private String sourceParam;

    //元数据类型
    private String type;

    private String datasource;

    private String db;

    private String table;

    //新版元数据获取 guid传参用region,type,database,db,table, 代替
    private String guid;

    //新版元数据获取 qualifiedName,type,database,db,table, 代替
    private String qualifiedName;

    private String engine;

    //创建DB表的sql
    private String createTableSql;

    /**
     * 判断元数据获取走哪一套
     */
    private String metaFlag = "AIRBYTE";

    /**
     * 获取getdispalyddl使用
     */
    private Boolean isSql;

    //兼容db->hive getInsertSql

    //标识查询是判断是否表存在
    private Boolean judgeTable = false;

    private String userName;

    /**
     * 数据源名称
     */
    private String name;
    /**
     * 描述数据源类型的id
     */
    private Integer sourceDefinitionId;

    /**
     * 数据源配置信息
     */
    private JSONObject connectionConfiguration;

    /**
     * 是否刷新缓存
     */
    public Boolean clearCache = false;

    public String topic;


    public MetaDataParam(Dataset dataset) {
        Dataset.Metadata metadata = dataset.getMetadata();
        this.setType(metadata.getType());
        this.setRegion(metadata.getRegion());
        this.qualifiedName = dataset.getId();
        if (StringUtils.isNotEmpty(metadata.getType()) && (metadata.getType().equals(SourceTypeEnum.hive_table.getType())
                || metadata.getType().equals(SourceTypeEnum.rdbms_table.getType())
                || metadata.getType().equalsIgnoreCase(SourceTypeEnum.metis.getType()))) {
            this.actorId = metadata.getSource();
            this.metaFlag = MetaDataManager.AIRBYTE;
            this.qualifiedName = null;
        }
        this.setDatasource(metadata.getSource());
        this.setDb(metadata.getDb());
        this.setTable(metadata.getTable());
    }


    public boolean isLegal() {
        return !StringUtils.isEmpty(this.getQualifiedName()) || !StringUtils.isEmpty(this.getRegion())
                || !StringUtils.isEmpty(this.getGuid()) || !StringUtils.isEmpty(this.getType());
    }

    public boolean isExistOldInfo() {
        return StringUtils.isNotEmpty(this.getQualifiedName()) || StringUtils.isNotEmpty(this.getGuid());
    }

}
