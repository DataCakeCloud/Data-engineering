package com.ushareit.dstask.third.scmp.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author fengxiao
 * @date 2022/3/10
 */
@Data
public class SCMPResult<ITEM> implements Serializable {
    private static final long serialVersionUID = 6309654787063995758L;

    private Integer code;
    private List<ITEM> result;
}
