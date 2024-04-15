package com.ushareit.dstask.third.airbyte.common;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * @author fengxiao
 * @date 2022/6/24
 */
public class Constant {

    public static final int PORT1 = 9877;
    public static final int PORT2 = 9878;
    public static final int PORT3 = 9879;
    public static final int PORT4 = 9880;
    public static final Set<Integer> PORTS = Sets.newHashSet(PORT1, PORT2, PORT3, PORT4);

    public static final String DEFAULT_ICON = "https://cbs-flink-sg.obs.ap-southeast-3.myhuaweicloud.com/icons/air_default.jpg";

}
