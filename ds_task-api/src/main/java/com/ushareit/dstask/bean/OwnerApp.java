package com.ushareit.dstask.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;

/**
 * 归属应用表
 * @author tiyongshuai
 * @date 2024/03/28
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "owner_app")
public class OwnerApp extends DeleteEntity{

    private String name;
    private Integer ownerId;
    private Integer orgId;

}

