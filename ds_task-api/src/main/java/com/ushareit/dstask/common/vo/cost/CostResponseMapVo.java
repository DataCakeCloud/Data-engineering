package com.ushareit.dstask.common.vo.cost;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CostResponseMapVo {
    private String key;
    private List<CostResponseVo> list;
}
