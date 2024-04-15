package com.ushareit.dstask.common.vo;

import lombok.Data;

/**
 * @auther tiyongshuai
 * @data 2024/3/28
 * @description
 */

@Data
public class OwnerAppVO {

    private Integer id;
    private String name;
    private Integer ownerId;
    private Integer orgId;
}
