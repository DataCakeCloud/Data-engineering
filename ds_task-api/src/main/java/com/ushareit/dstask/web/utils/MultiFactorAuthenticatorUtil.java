package com.ushareit.dstask.web.utils;

import com.ushareit.dstask.bean.AccessUser;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * author :xuebotao
 * date :2022-12-1
 */
@Slf4j
public class MultiFactorAuthenticatorUtil {
    // this is the issuer, you can change it to your company/project name.
    private static final String ISSUER = "DataCake";
    private static final String IMAGE_QR_CODE_API = "https://api.qrserver.com/v1/create-qr-code/?size=150x150&data=";
    private static final String SEED = "thisisgoogleauthenticator";
    private static final String RANDOM_NUMBER_ALGORITHM = "SHA1PRNG";
    private static final int SECRET_SIZE = 10;
    // suggest: the smaller the value, the safer it is. (from 1 to 17)
    private static final int WINDOW_SIZE = 1;

    public static String generateSecretKey() {
        try {
            SecureRandom sr = SecureRandom.getInstance(RANDOM_NUMBER_ALGORITHM);
            sr.setSeed(Base64.decodeBase64(SEED));
            byte[] buffer = sr.generateSeed(SECRET_SIZE);
            Base32 codec = new Base32();
            byte[] bEncodedKey = codec.encode(buffer);
            return new String(bEncodedKey);
        } catch (NoSuchAlgorithmException e) {
            log.error("generate secret exception：{[]}", e);
        }
        return null;
    }

    public static String getQRBarcodeURL(AccessUser user, String secret) {
        String env = InfTraceContextHolder.get().getEnv();
        String format = IMAGE_QR_CODE_API + "otpauth://totp/%s?secret=%s%%26issuer=%s";
        String userName = "DataCake :" + user.getEmail();
        String PROJECT = ISSUER + "-" + user.getTenantName() + "-" + env;
        String imageUrl = String.format(format, userName, secret, PROJECT);
        log.info(imageUrl);
        return imageUrl;
    }

    public static boolean checkCode(String secret, long code, long time) {
        Base32 codec = new Base32();
        byte[] decodedKey = codec.decode(secret);
        // convert unix msec time into a 30 second "window"
        // this is per the TOTP spec (see the RFC for details)
        long t = (time / 1000L) / 30L;
        for (int i = -WINDOW_SIZE; i <= WINDOW_SIZE; ++i) {
            long hash;
            try {
                hash = verifyCode(decodedKey, t + i);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e.getMessage());

            }
            if (hash == code) {
                return true;
            }
        }
        return false;
    }

    private static int verifyCode(byte[] key, long t) throws NoSuchAlgorithmException, InvalidKeyException {
        byte[] data = new byte[8];
        long value = t;
        for (int i = 8; i-- > 0; value >>>= 8) {
            data[i] = (byte) value;
        }
        SecretKeySpec signKey = new SecretKeySpec(key, "HmacSHA1");
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(signKey);
        byte[] hash = mac.doFinal(data);
        int offset = hash[20 - 1] & 0xF;
        long truncatedHash = 0;
        for (int i = 0; i < 4; ++i) {
            truncatedHash <<= 8;
            truncatedHash |= (hash[offset + i] & 0xFF);
        }
        truncatedHash &= 0x7FFFFFFF;
        truncatedHash %= 1000000;
        return (int) truncatedHash;
    }

    private MultiFactorAuthenticatorUtil() {
    }
}
