package com.ushareit.dstask.third.scmp.vo;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;

/**
 * @author fengxiao
 * @date 2022/3/10
 */
@Data
public class SCMPMemberItem implements Serializable {
    private static final long serialVersionUID = -5967446899785611613L;

    private Integer id;

    @JSONField(name = "shareid", alternateNames = "shareId")
    private String shareId;
    private String name;
}
