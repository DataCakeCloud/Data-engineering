package com.ushareit.dstask;

import com.ushareit.dstask.constant.CommonConstant;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

/**
 * @author fengxiao
 * @date 2022/8/15
 */
public class TestFilter {

    public static boolean pathIsMatch(String requestPath, String[] mathPaths) {
        PathMatcher pathMatcherToUse = new AntPathMatcher();
        if (!ArrayUtils.isEmpty(mathPaths)) {
            for (String pattern : mathPaths) {
                if (pathMatcherToUse.match(pattern, requestPath)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Test
    public void testIgnore() {
        System.out.println(pathIsMatch("/actor/sources/discover_schema?sourceId=4", CommonConstant.IGNORE_INTERCEPT_PATHS));
    }

}
