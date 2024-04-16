package com.ushareit.engine.param;

import lombok.Data;

import java.util.List;

@Data
public class Catalog {
    private String table_type;
    private String sourceDb;
    private String targetDb;//datax
    private List<Table> tables;
    private Integer sync_mode;
}
