package com.ushareit.dstask.utils;


import com.ushareit.dstask.bean.CurrentUser;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.exception.ServiceException;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.keys.HmacKey;

import java.security.Key;

/**
 * author :xuebotao
 * date :2022-11-22
 */
public class TokenUtil {


    /**
     * 生产用户唯一token
     *
     * @param accessUser
     * @param expire
     * @return
     * @throws Exception
     */
    public static String generateToken(CurrentUser accessUser, int expire) {
        JwtClaims claims = new JwtClaims();
        claims.setSubject(accessUser.getName() != null ? accessUser.getName() : "");
        claims.setClaim("CONTEXT_USER_ID", accessUser.getId() != null ? accessUser.getId() : 0);
        claims.setClaim("CONTEXT_GROUP_IDS", accessUser.getGroupIds() != null ? accessUser.getGroupIds() : "");
        claims.setClaim("CONTEXT_EMAIL", accessUser.getEmail() != null ? accessUser.getEmail() : "");
        claims.setClaim("CONTEXT_TENANT_ID", accessUser.getTenantId() != null ? accessUser.getTenantId() : 0);
        claims.setClaim("CONTEXT_TENANT_NAME", accessUser.getTenantName() != null ? accessUser.getTenantName() : "");
        claims.setClaim("CONTEXT_USER_PASSWORD", accessUser.getPassword() != null ? accessUser.getPassword() : "");
        claims.setClaim("CONTEXT_USER_TENANCY_CODE", accessUser.getGroup() != null ? accessUser.getGroup() : "");
        claims.setClaim("CONTEXT_USER_ORG", accessUser.getGroupName() != null ? accessUser.getGroupName() : 0);
        claims.setExpirationTimeMinutesInTheFuture(expire == 0 ? 60 * 24 : expire);

        Key key = null;
        String token = null;
        try {
            key = new HmacKey("JWT_PRIVATE_KEY".getBytes("UTF-8"));

            JsonWebSignature jws = new JsonWebSignature();
            jws.setPayload(claims.toJson());
            jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.HMAC_SHA256);
            jws.setKey(key);
            jws.setDoKeyValidation(false); // relaxes the key length requirement

            token = jws.getCompactSerialization();
        } catch (Exception e) {
            throw new ServiceException(BaseResponseCodeEnum.USER_TOKEN_CREATE_FAIL);
        }

        return token;
    }

    /**
     * 解析token
     *
     * @param token
     * @return
     * @throws Exception
     */
    public static CurrentUser getInfoFromToken(String token) {

//        if (StringUtils.isEmpty(token)) {
//            return null;
//        }
        Key key = null;
        JwtClaims processedClaims = null;
        CurrentUser build = null;
        try {
            key = new HmacKey("JWT_PRIVATE_KEY".getBytes("UTF-8"));
            JwtConsumer jwtConsumer = new JwtConsumerBuilder()
                    .setRequireExpirationTime()
                    .setAllowedClockSkewInSeconds(30)
                    .setRequireSubject()
                    .setVerificationKey(key)
                    .setRelaxVerificationKeyValidation()
                    .build();

            processedClaims = jwtConsumer.processToClaims(token);

            build = new CurrentUser();
            build.setName(processedClaims.getSubject());
            build.setEmail(processedClaims.getClaimValue("CONTEXT_EMAIL").toString());
            build.setPassword(processedClaims.getClaimValue("CONTEXT_USER_PASSWORD").toString());
            build.setTenantId(Integer.parseInt(processedClaims.getClaimValue("CONTEXT_TENANT_ID").toString()));
            build.setGroup(processedClaims.getClaimValue("CONTEXT_USER_TENANCY_CODE").toString());
            build.setGroupName(processedClaims.getClaimValue("CONTEXT_USER_ORG").toString());
            build.setTenantName(processedClaims.getClaimValue("CONTEXT_TENANT_NAME").toString());
            build.setGroupIds(processedClaims.getClaimValue("CONTEXT_GROUP_IDS").toString());
            build.setId(Integer.parseInt(processedClaims.getClaimValue("CONTEXT_USER_ID").toString()));
            build.setUserId(processedClaims.getSubject());
            build.setUserName(processedClaims.getSubject());
        } catch (Exception e) {
            throw new ServiceException(BaseResponseCodeEnum.USER_TOKEN_PARSE_FAIL);
        }

        return build;
    }


}
