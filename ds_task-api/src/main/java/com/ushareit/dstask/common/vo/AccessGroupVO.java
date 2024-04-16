package com.ushareit.dstask.common.vo;

import com.ushareit.dstask.bean.AccessGroup;
import lombok.Data;

/**
 * @author fengxiao
 * @date 2023/1/17
 */
@Data
public class AccessGroupVO {

    private Integer id;
    private String name;

    public AccessGroupVO(AccessGroup accessGroup) {
        this.id = accessGroup.getId();
        this.name = accessGroup.getName();
    }
}
