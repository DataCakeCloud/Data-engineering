package com.ushareit.dstask.web.utils;

import java.util.Random;

/**
 * author : xuebotao
 * date : 2022-11-14
 */
public class RandGenUtils {

    public static String getCode(Integer length) {
        String checkString = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        char[] VerificationCodeArray = checkString.toCharArray();
        Random random = new Random();
        int count = 0;
        StringBuilder stringBuilder = new StringBuilder();
        while (true) {
            int index = random.nextInt(VerificationCodeArray.length);
            char c = VerificationCodeArray[index];
            if (stringBuilder.indexOf(c + "") == -1) {
                stringBuilder.append(c);
                count++;
            }
            if (count == length) {
                break;
            }
        }
        return stringBuilder.toString();
    }

}