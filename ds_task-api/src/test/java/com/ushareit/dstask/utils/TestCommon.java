package com.ushareit.dstask.utils;

import com.ushareit.dstask.constant.CommonConstant;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

public class TestCommon {

    @Test
    public void testIgnore() {
        String a = "aa";
        String[] split = a.split("\\.");
        System.out.println(split[0]);
    }

}
