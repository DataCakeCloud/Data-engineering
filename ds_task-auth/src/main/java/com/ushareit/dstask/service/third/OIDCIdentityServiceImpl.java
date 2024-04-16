package com.ushareit.dstask.service.third;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.util.DefaultResourceRetriever;
import com.nimbusds.jose.util.ResourceRetriever;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;

/**
 * @author zhaopan
 * @date 2021/2/3
 */
@Slf4j
@Service
public class OIDCIdentityServiceImpl implements OIDCIdentityService {

    @Value("${oidc.server.code}")
    public String serverCode;

    @Value("${oidc.identity.url}")
    private String oidcIdentityUrl;

    @Override
    public boolean validate(String token) {
        if (StringUtils.isBlank(token)) {
            return false;
        }
        try {
            //增加超时设置默认500
            ResourceRetriever jwkRetriever = new DefaultResourceRetriever(5000, 5000, 51200);
            ConfigurableJWTProcessor<SecurityContext> processor = new DefaultJWTProcessor<>();

            JWKSource jwkSource = new RemoteJWKSet<>(new URL(oidcIdentityUrl), jwkRetriever);
            processor.setJWSKeySelector(
                    new JWSVerificationKeySelector<>(JWSAlgorithm.RS256, jwkSource)
            );
            processor.setJWTClaimsSetVerifier(new DefaultJWTClaimsVerifier<>(serverCode, null, null));

            JWTClaimsSet claimsSet = processor.process(token, null);
            log.info("request oidc result: {}", claimsSet.toJSONObject());
            return true;
        } catch (MalformedURLException | ParseException | BadJOSEException | JOSEException exception) {
            log.error("check token error: ", exception);
            return false;
        }

    }

}
