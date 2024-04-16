package com.ushareit.dstask.third.oidc;

/**
 * @author fengxiao
 * @date 2022/9/19
 */
public interface OidcService {

    /**
     * 获取某个 oidc 服务端的 token
     *
     * @param clientId 服务端ID
     * @return token
     */
    String getToken(String clientId);

}
