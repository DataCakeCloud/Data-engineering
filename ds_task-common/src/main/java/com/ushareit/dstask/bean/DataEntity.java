package com.ushareit.dstask.bean;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 在数据库中拥有创建人和更新人的实体
 *
 * @author wuyan
 * @date 2018/11/5
 **/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class DataEntity extends OperatorEntity {
    /**
     * 描述信息
     */
    private String description;
}