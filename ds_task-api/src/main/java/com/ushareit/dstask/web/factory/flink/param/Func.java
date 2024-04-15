package com.ushareit.dstask.web.factory.flink.param;

import lombok.Data;
import java.util.List;

/**
 * @author: licg
 * @create: 2022-03-18 15:24
 */
@Data
public class Func {
    private String funcName;
    private List<String> funcParams;
}
