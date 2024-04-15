package com.ushareit.dstask.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;

/**
 * @auther tiyongshuai
 * @data 2024/3/28
 * @description 表归属人
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "table_owner")
public class TableOwner extends DeleteEntity {

    private String dnName;
    private String tableName;
    private Integer ownerAppId;

}