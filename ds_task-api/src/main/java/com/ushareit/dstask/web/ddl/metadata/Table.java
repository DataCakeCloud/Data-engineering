package com.ushareit.dstask.web.ddl.metadata;

import com.ushareit.dstask.bean.BaseEntity;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @author: licg
 * @create: 2021-05-12 15:24
 */
@Data
public class Table extends BaseEntity {
    private String guid = "";
    private String qualifiedName;
    private String description;
    private String displayName;
    private String name;
    private String comment;
    private List<Column> columns;
    private List<Column> partitionKeys;
    private String dbId;
    private String dbName;
    private String sourceType;
    private Map<String, String> parameters;
    private String region;
    private String tableType;
    private String typeName;
    private String securityLevel;

    /**
     * 兼容所有元数据返回
     */
    private String db;
    private String url;
    private String username;
    private String password;
    private String owner;
    private String userGroupName;

    private String ownerAppName;
    //适配使用
    private String actorId;

    //元数据类型
    private String type;

    private String sourceTable;
    private String partitions;
    private String targetTable;
    private String targetTablePart;
    private String primaryKey;

    //表是否存在
    private Boolean isTalbeExist = true;
    private String location;

}
