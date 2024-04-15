package com.ushareit.dstask.web.utils;

import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.constant.DsTaskConstant;
import com.ushareit.dstask.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author tianxu
 * @date 2023/7/20 21:42
 **/
@Slf4j
@Service
public class LdapUtil {

    @Value("${ldap.url}")
    public String url;

    @Value("${ldap.base}")
    private String base;

    @Value("${ldap.securityPrincipal}")
    private String securityPrincipal;

    @Value("${ldap.filter}")
    private String ldapFilter;

    @Value("${ldap.attrPerson}")
    private String attrPerson;

    private LdapContext ctx = null;

    private void ldapConnect(String url, String user, String password) {
        Hashtable<String, Object> env = new Hashtable<>();
        //LDAP 工厂
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        //url 格式：协议://ip:端口/组,域   ,直接连接到域或者组上面
        env.put(Context.PROVIDER_URL, url);
        //验证的类型
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        //用户名称，cn,ou,dc 分别：用户，组，域
        env.put(Context.SECURITY_PRINCIPAL, user);
        //用户密码 cn 的密码
        env.put(Context.SECURITY_CREDENTIALS, password);
        try {
            ctx = new InitialLdapContext(env, null);
        } catch (javax.naming.AuthenticationException e) {
            log.error(String.format("%s ldap failed to authentication: %s", user, CommonUtil.printStackTraceToString(e)));
            throw new ServiceException(BaseResponseCodeEnum.USER_OR_PASSWORD_ERROR);
        } catch (Exception e) {
            log.error(String.format("%s ldap authentication exception: %s", user, CommonUtil.printStackTraceToString(e)));
            throw new ServiceException(BaseResponseCodeEnum.USER_OR_PASSWORD_ERROR);
        }
    }

    public Map<String, String> ldapLogin(String user, String password) {
        try {
            String uid = user.split("@")[0];  // todo 根据实际需求待调整，用户登录账号不一定是邮箱
            try {
                password = EncryptUtil.decrypt(password, DsTaskConstant.METADATA_PASSWDKEY);
            } catch (Exception e) {
                log.error(String.format("%s failed to decrypt password when ldapLogin: %s", user, CommonUtil.printStackTraceToString(e)));
                throw new ServiceException(BaseResponseCodeEnum.USER_OR_PASSWORD_ERROR);
            }
            if ((user.endsWith(DsTaskConstant.SHAREIT_EMAIL))) {
                ldapConnect(url, String.format(securityPrincipal, uid), password);
            } else {
                ldapConnect(url, user, password);
            }
            String filter = String.format(ldapFilter, user);
            SearchControls searchControls = new SearchControls();
            searchControls.setSearchScope(2);
            searchControls.setReturningAttributes(attrPerson.split(","));
            NamingEnumeration<SearchResult> answer = ctx.search(base, filter, searchControls);
            Map<String, String> map = new HashMap<>();
            while (answer.hasMore()) {
                SearchResult result = answer.next();
                NamingEnumeration<? extends Attribute> attrs = result.getAttributes().getAll();
                while (attrs.hasMore()) {
                    Attribute attr = attrs.next();
                    map.put(attr.getID(), (String) attr.get());
                }
            }
            return map;
        } catch (NamingException e) {
            log.error(String.format("%s ldapLogin namingException: %s", user, CommonUtil.printStackTraceToString(e)));
            throw new ServiceException(BaseResponseCodeEnum.LDAP_EXCEPTION);
        } finally {
            closeConnect();
        }
    }

    private void closeConnect() {
        if (ctx != null) {
            try {
                ctx.close();
            } catch (NamingException e) {
                log.error("ldap failed to close: " + CommonUtil.printStackTraceToString(e));
            }
        }
    }
}
