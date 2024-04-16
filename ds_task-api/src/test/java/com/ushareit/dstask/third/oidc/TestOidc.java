package com.ushareit.dstask.third.oidc;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.MessageDigestAlgorithms;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.junit.Test;

/**
 * @author fengxiao
 * @date 2022/12/30
 */
public class TestOidc {

    @Test
    public void testSha1() {
        String salt = RandomStringUtils.randomAlphanumeric(30);
        System.out.println(salt);

        String info = "hello world";

        String toDigestStr = info + salt;
        String digest = new DigestUtils(MessageDigestAlgorithms.SHA_1).digestAsHex(toDigestStr);
        System.out.println(digest);
    }

}
