package com.ushareit.dstask.constant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: licg
 * @create: 2021-05-12 15:24
 */
public enum SourceTypeEnum {
    /**
     * 数据源类型
     */
    kafka_topic("kafka", "airbyte/source-kafka"),
    rdbms_table("mysql", "airbyte/source-mysql"),
    sql_server("sqlserver", "airbyte/source-mssql"),
    oracle("oracle", "airbyte/source-oracle"),
    doris("doris", "airbyte/destination-doris"),
    postgres("postgres", "airbyte/source-postgres"),
    file("file", "airbyte/source-flie"),
    es("es", "airbyte/source-es"),
    sharestore("sharestore", "airbyte/source-sharestore"),
    iceberg("iceberg", "airbyte/source-iceberg"),
    hive_table("hive", "airbyte/source-hive"),
    clickhouse("clickhouse", "airbyte/source-clickhouse"),
    metis("metis", "airbyte/source-metis"),
    metis_dev("metis", "airbyte/source-metis_dev"),
    metis_test("metis", "airbyte/source-metis_test"),
    metis_pro("metis", "airbyte/source-metis_pro"),
    s3("s3","airbyte/source-s3"),
    hdfs("hdfs","airbyte/source-hdfs"),
    mongodb("mongodb","airbyte/source-mongodb-v2"),
    oss("oss","airbyte/source-oss"),
    hana("hana","airbyte/source-hana"),
    ks3("ks3","airbyte/source-ks3");


    public static List<SourceTypeEnum> SourceTypeList = new ArrayList<>();
    public static Map<String, SourceTypeEnum> SourceTypeMap = new HashMap<>();

    static {
        SourceTypeList.add(SourceTypeEnum.hive_table);
        SourceTypeList.add(SourceTypeEnum.rdbms_table);
        SourceTypeList.add(SourceTypeEnum.clickhouse);
        SourceTypeList.add(SourceTypeEnum.metis);
        SourceTypeList.add(SourceTypeEnum.iceberg);
        SourceTypeList.add(SourceTypeEnum.sql_server);
        SourceTypeList.add(SourceTypeEnum.oracle);
        SourceTypeList.add(SourceTypeEnum.doris);
        SourceTypeList.add(SourceTypeEnum.postgres);
        SourceTypeList.add(SourceTypeEnum.s3);
        SourceTypeList.add(SourceTypeEnum.ks3);
        SourceTypeList.add(SourceTypeEnum.hdfs);
        SourceTypeList.add(SourceTypeEnum.oss);
        SourceTypeList.add(SourceTypeEnum.mongodb);
        SourceTypeList.add(SourceTypeEnum.hana);
        for (SourceTypeEnum sourceTypeEnum : SourceTypeList) {
            SourceTypeMap.put(sourceTypeEnum.name(), sourceTypeEnum);
            SourceTypeMap.put(sourceTypeEnum.getType(), sourceTypeEnum);
            SourceTypeMap.put(sourceTypeEnum.getSourceDefinition(), sourceTypeEnum);
        }
    }


    public String type;
    public String sourceDefinition;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSourceDefinition() {
        return sourceDefinition;
    }

    public void setSourceDefinition(String sourceDefinition) {
        this.sourceDefinition = sourceDefinition;
    }

    SourceTypeEnum(String type, String sourceDefinition) {
        this.type = type;
        this.sourceDefinition = sourceDefinition;
    }

    public static boolean isValid(String type) {
        for (SourceTypeEnum tmpType : SourceTypeEnum.values()) {
            if (tmpType.type.equalsIgnoreCase(type)) {
                return true;
            }
        }

        return false;
    }
}
