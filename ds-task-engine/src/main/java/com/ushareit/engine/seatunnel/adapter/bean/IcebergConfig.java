package com.ushareit.engine.seatunnel.adapter.bean;


import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
public class IcebergConfig implements Serializable {

    private static final long serialVersionUID = -470048238703035984L;

    public String database;
    @JSONField(name = "catalogType", alternateNames = "catalog_type")
    public String catalogType;
    @JSONField(name = "hiveThriftUri", alternateNames = "hive_thrift_uri")
    public String hiveThriftUri;
    @JSONField(name = "hadoopConf",alternateNames = "hadoop_conf")
    public Map<String,Object> hadoopConf;
    public String defaultFS;


    public IcebergConfig() {

    }

    public IcebergConfig(String database, String catalogType, String hiveThriftUri) {
        this.database = database;
        this.catalogType = catalogType;
        this.hiveThriftUri = hiveThriftUri;
    }

}
