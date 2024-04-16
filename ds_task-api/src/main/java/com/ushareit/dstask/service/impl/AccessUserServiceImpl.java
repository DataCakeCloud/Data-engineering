package com.ushareit.dstask.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageInfo;
import com.ushareit.dstask.bean.*;
import com.ushareit.dstask.constant.BaseConstant;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.constant.DsTaskConstant;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.mapper.AccessMenuMapper;
import com.ushareit.dstask.mapper.AccessRoleMapper;
import com.ushareit.dstask.mapper.AccessUserMapper;
import com.ushareit.dstask.mapper.UserGroupRelationMapper;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import com.ushareit.dstask.service.*;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import com.ushareit.dstask.utils.TokenUtil;
import com.ushareit.dstask.web.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Timestamp;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author wuyan
 * @date 2022/4/11
 */
@Slf4j
@Service
public class AccessUserServiceImpl extends AbstractBaseServiceImpl<AccessUser> implements AccessUserService {
    @Resource
    private AccessUserMapper accessUserMapper;

    @Resource
    private AccessUserRoleService accessUserRoleService;

    @Resource
    private AccessUserGroupService accessUserGroupService;

    @Resource
    private AccessRoleService accessRoleService;

    @Resource
    private AccessTenantService accessTenantService;

    @Resource
    private LdapUtil ldapUtil;

    @Resource
    private EmailUtils emailUtils;

    @Resource
    private AccessGroupService accessGroupService;

    @Resource
    private AccessMenuMapper accessMenuMapper;

    @Resource
    private UserGroupRelationMapper userGroupRelationMapper;

    @Resource
    private AccessRoleMapper accessRoleMapper;

    @Value("${spring.mail.username}")
    public String datacakeManagerEmail;

    @Value("${server-url.host}")
    public String serverUrlHost;

    @Value("${shimo-url.host}")
    public String shimoHost;


    @Override
    public CrudMapper<AccessUser> getBaseMapper() {
        return accessUserMapper;
    }

    @Override
    public Object save(AccessUser accessUser) {
        AccessTenant byName = accessTenantService.getByName(accessUser.getTenantName());
        if (byName == null) {
            throw new ServiceException(BaseResponseCodeEnum.TENANT_NOT_EXISTS);
        }
        AccessUser build = AccessUser.builder().tenantId(byName.getId()).email(accessUser.getEmail()).build();
        build.setDeleteStatus(0);

        //校验租户下的邮箱是否重复
        super.checkOnUpdate(selectOne(build), accessUser);

        List<Integer> collect = Arrays.stream(accessUser.getUserRoleIds().split(",")).map(Integer::parseInt)
                .collect(Collectors.toList());
        List<AccessRole> accessRoleList = accessRoleService.listByIds(collect);
        String role = "common";
        String code = RandGenUtils.getCode(8);
        String password;
        try {
            password = EncryptUtil.encrypt(code, DsTaskConstant.METADATA_PASSWDKEY);
        } catch (Exception e) {
            throw new ServiceException(BaseResponseCodeEnum.ENCRYPT_FAILURE, e);
        }
        String topic = "[DataCake]";
        String content = "[%s]您好，恭喜您成为 DataCake 平台的用户。</br>\n" +
                "</br>\n" +
                "平台的访问链接为：<a href ='%s'>%s</a></br>\n" +
                "租户名称：%s</br>\n" +
                "登录邮箱：%s</br>\n" +
                "登录密码：%s</br>\n" +
                "隶属角色：%s</br>\n" +
                "用户手册：<a href ='%s'>%s</a></br>\n" +
                "</br>\n" +
                "以上如有问题您可以联系 %s，进行反馈。";
        if (StringUtils.isNotEmpty(accessUser.getUserRoleIds())) {
            if (accessUser.getUserRoleIds().equals("0")) {
                role = accessUser.getRole();
            } else {
                role = accessRoleList.stream().map(AccessRole::getName).collect(Collectors.joining(","));
            }
        }
        accessUser.setName(accessUser.getEmail().split("@")[0])
                .setEmail(accessUser.getEmail())
                .setPassword(password).setTenantId(byName.getId())
                .setCreateBy(InfTraceContextHolder.get().getUserName())
                .setUpdateBy(InfTraceContextHolder.get().getUserName());
        //TODO 写入分库表中
        super.save(accessUser);

        String loginURL = serverUrlHost;
        emailUtils.sendMessage(accessUser.getEmail(), topic, String.format(content,
                accessUser.getName(), loginURL, loginURL, byName.getName(),
                accessUser.getEmail(), code, role, shimoHost, shimoHost, datacakeManagerEmail));

        //附加common权限
        AccessUser newUser = selectByNameTenant(byName.getId(), accessUser.getEmail());
        if (StringUtils.isEmpty(accessUser.getUserRoleIds())) {
            AccessRole commonRole = accessRoleService.getByName("common");
            //TODO 写入分库表中
            accessUserRoleService.addUsers(commonRole.getId(), newUser.getId().toString());
        }
        if (accessUser.getUserRoleIds().equals("0")) {
            return accessUser;
        }

        List<Integer> ids = accessRoleList.stream().map(BaseEntity::getId).collect(Collectors.toList());
        for (Integer id : ids) {
            //TODO 写入分库表中
            accessUserRoleService.addUsers(id, newUser.getId().toString());
        }

        return accessUser;
    }

    //批量添加 迁移用户
    @Override
    public void batchAddUser() throws Exception {
        List<AccessUser> accessUserList = batchInitUser();

        for (AccessUser accessUser : accessUserList) {
            AccessUser build = AccessUser.builder().tenantId(1).email(accessUser.getEmail()).build();
            build.setDeleteStatus(0);

            AccessUser dbUser = selectOne(build);
            if (dbUser == null) {
                continue;
            }

            String role = "common";
            String code = RandGenUtils.getCode(8);
            String password;
            try {
                password = EncryptUtil.encrypt(code, DsTaskConstant.METADATA_PASSWDKEY);
            } catch (Exception e) {
                throw new ServiceException(BaseResponseCodeEnum.ENCRYPT_FAILURE, e);
            }
            String topic = "通知:原Datastudio迁移至Datacake";
            String content = "%s您好，恭喜您成为 DataCake 平台的用户，DataCake 是原 Datastudio的SaaS版本。</br>\n" +
                    "</br>\n" +
                    "平台的访问链接为：<a href ='%s'>%s</a></br>\n" +
                    "租户名称：%s</br>\n" +
                    "登录邮箱：%s</br>\n" +
                    "登录密码：%s</br>\n" +
                    "隶属角色：%s</br>\n" +
                    "角色对应功能说明：<a href ='https://shimo.im/docs/R13j8ormzvhXQXk5'>https://shimo.im/docs/R13j8ormzvhXQXk5</a></br>\n" +
                    "</br>\n" +
                    "我们预计2月14日21点开始迁移，原datastudio 所有产品模块下的内容至 datacake 平台，届时会提前在数据问题反馈群提前通知，预计1小时完成迁移" +
                    "</br>\n" +
                    "更多关于DataCake迁移的FAQ详见：\n" +
                    "<a href ='https://shimo.im/docs/L9kBMwzba4Hl9PqK'>https://shimo.im/docs/L9kBMwzba4Hl9PqK</a></br>\n" +
                    "</br>\n" +
                    "以上如有问题您可以联系 datacake@ushareit.com，进行反馈。";
            dbUser.setPassword(password)
                    .setCreateBy(InfTraceContextHolder.get().getUserName())
                    .setUpdateBy(InfTraceContextHolder.get().getUserName());

            super.update(dbUser);

            String loginURL = serverUrlHost;
            emailUtils.sendMessage(accessUser.getEmail(), topic, String.format(content,
                    accessUser.getName(), loginURL, loginURL, DsTaskConstant.SHAREIT_TENANT_NAME,
                    accessUser.getEmail(), code, role));
        }
    }

    @Override
    public List<AccessUser> selectByNames(List<String> userNmes) {
        Example example = new Example(AccessUser.class);
        example.or().andIn("name", userNmes).andEqualTo("deleteStatus", DeleteEntity.NOT_DELETE);
        return accessUserMapper.selectByExample(example);
    }

    @Override
    public List<AccessUser> likeByName(String userName) {
        Example example = new Example(AccessUser.class);
        example.or().andLike("name", "%" + userName + "%").andEqualTo("deleteStatus", DeleteEntity.NOT_DELETE);
        return accessUserMapper.selectByExample(example);
    }

    public List<AccessUser> batchInitUser() throws Exception {
        List<AccessUser> userList = new ArrayList<>();
//        "/data/code/k8s/user"
//        FileReader fr = new FileReader("C:\\Users\\User\\IdeaProjects\\ds_task\\ds_task-api\\k8s\\user");
        FileReader fr = new FileReader("/data/code/k8s/user");
        BufferedReader br = new BufferedReader(fr);
        String line = "";
        String[] arrs = null;
        while ((line = br.readLine()) != null) {
            arrs = line.split("\t");
            AccessUser accessUser = new AccessUser();
            accessUser.setName(arrs[0].split("@")[0]);
            accessUser.setEmail(arrs[0]);
            userList.add(accessUser);
        }
        br.close();
        fr.close();
        return userList;
    }

    @Override
    public AccessUser getById(Object id) {
        AccessUser accessUser = getBaseMapper().selectByPrimaryKey(id);
        accessUser.setPassword(null);
        return accessUser;
    }

    @Override
    public PageInfo<AccessUser> listByPage(int pageNum, int pageSize, Map<String, String> paramMap) {
        PageInfo<AccessUser> pageInfo = super.listByPage(pageNum, pageSize, paramMap);
        List<AccessUser> list = pageInfo.getList();

        list.stream().forEach(user -> {
            List<Integer> roleIds = accessUserRoleService.selectByUserId(user.getId());
            List<AccessRole> accessRoles = accessRoleService.listByIds(roleIds.stream());
            List<AccessRole> filter = accessRoles.stream().filter(role -> role.getDeleteStatus() == 0).collect(Collectors.toList());
            List<String> names = filter.stream().map(AccessRole::getName).collect(Collectors.toList());
            List<Integer> ids = filter.stream().map(AccessRole::getId).collect(Collectors.toList());
            List<String> groups=userGroupRelationMapper.selectByUserId(user.getId());
            user.setRoles(names);
            user.setPassword(null);
            user.setRoleIds(ids);
            user.setGroups(groups);
        });

        return pageInfo;
    }


    @Override
    public Boolean isRootUser(String userId) {
        return InfTraceContextHolder.get().getAdmin();
//        Integer tenantId = InfTraceContextHolder.get().getTenantId();
//        AccessUser accessUserBuilder = AccessUser.builder().tenantId(tenantId).name(userId).build();
//        accessUserBuilder.setDeleteStatus(0);
//        AccessUser accessUser = selectOne(accessUserBuilder);
//        List<Integer> list=accessMenuMapper.existAdminAndSupperAdminRole(accessUser.getId());
//        if (CollectionUtils.isNotEmpty(list)&&list.contains(BaseConstant.ADMINMENUID)){
//            return true;
//        }
//        return false;
    }

    @Override
    public void sendCode(String tenantName, String email) {
        AccessTenant byName = accessTenantService.getByName(tenantName);
        if (byName == null) {
            throw new ServiceException(BaseResponseCodeEnum.TENANT_NOT_EXISTS);
        }

        InfTraceContextHolder.get().setTenantName(byName.getName());
        InfTraceContextHolder.get().setTenantId(byName.getId());

        AccessUser build = AccessUser.builder().tenantId(byName.getId()).email(email).build();
        build.setDeleteStatus(0);
        AccessUser accessUser = accessUserMapper.selectOne(build);
        if (accessUser == null) {
            throw new ServiceException(BaseResponseCodeEnum.USER_NOT_FOUND);
        }

        String code = RandGenUtils.getCode(4);
        accessUser.setLatestCode(code);
        update(accessUser);

        String topic = "[DataCake]";
        String content = "[DataCake]:您的校验码为:%s,请勿泄露给其他人。";
        emailUtils.sendMessage(email, topic, String.format(content, code));
    }

    @Override
    public Boolean checkLatestCode(String tenantName, String email, String code) {
        AccessTenant byName = accessTenantService.getByName(tenantName);
        if (byName == null) {
            throw new ServiceException(BaseResponseCodeEnum.TENANT_NOT_EXISTS);
        }

        InfTraceContextHolder.get().setTenantName(byName.getName());
        InfTraceContextHolder.get().setTenantId(byName.getId());

        AccessUser build = AccessUser.builder().tenantId(byName.getId()).email(email).build();
        build.setDeleteStatus(0);
        AccessUser accessUser = accessUserMapper.selectOne(build);
        if (!accessUser.getLatestCode().equalsIgnoreCase(code)) {
            throw new ServiceException(BaseResponseCodeEnum.CLI_IDENTIFY_CODE_ERROR);
        }
        return true;
    }

    @Override
    public void updatePassword(String tenantName, String email, String password) {
        AccessTenant byName = accessTenantService.getByName(tenantName);
        if (byName == null) {
            throw new ServiceException(BaseResponseCodeEnum.TENANT_NOT_EXISTS);
        }

        InfTraceContextHolder.get().setTenantName(byName.getName());
        InfTraceContextHolder.get().setTenantId(byName.getId());

        AccessUser build = AccessUser.builder().tenantId(byName.getId()).email(email).build();
        build.setDeleteStatus(0);
        AccessUser accessUser = accessUserMapper.selectOne(build);
        accessUser.setPassword(password)
                .setUpdateBy(InfTraceContextHolder.get().getUserName())
                .setUpdateTime(new Timestamp(System.currentTimeMillis()));
        update(accessUser);
    }

    @Override
    public void resetPassword(Integer userId) {
        AccessUser accessUser = checkExist(userId);
        String password = RandGenUtils.getCode(8);
        String basePassword = null;
        try {
            basePassword = EncryptUtil.encrypt(password, DsTaskConstant.METADATA_PASSWDKEY);
        } catch (Exception e) {
            throw new ServiceException(BaseResponseCodeEnum.ENCRYPT_FAILURE, e);
        }
        String topic = "[DataCake]";
        String content = "[DataCake]:您的用户[%s]，密码已被重置为:[%s]，请勿泄露给其他人，如有问题请联系的系统管理员";
        emailUtils.sendMessage(accessUser.getEmail(), topic, String.format(content, accessUser.getEmail(), password));
        accessUser.setPassword(basePassword)
                .setUpdateBy(InfTraceContextHolder.get().getUserName())
                .setUpdateTime(new Timestamp(System.currentTimeMillis()));
        update(accessUser);

    }

    @Override
    public AccessUser login(AccessUser accessUser) {
        if (StringUtils.isEmpty(accessUser.getTenantName()) || StringUtils.isEmpty(accessUser.getEmail())
                || StringUtils.isEmpty(accessUser.getPassword())) {
            throw new ServiceException(BaseResponseCodeEnum.USER_LOGIN_INFO_NOT_NULL);
        }

        if (!accessUser.getEmail().contains("@")) {
            accessUser.setEmail(accessUser.getEmail() + DsTaskConstant.EMAIL_SUFFIX);
        }
        Pattern r = Pattern.compile(DsTaskConstant.EMAIL_PATTERN);
        Matcher matcher = r.matcher(accessUser.getEmail());
        if (!matcher.find()) {
            throw new ServiceException(BaseResponseCodeEnum.USER_EMAIL_FORMAT_CHECK_FAIL);
        }

        AccessTenant accessTenant = accessTenantService.getByName(accessUser.getTenantName());
        if (accessTenant == null) {
            throw new ServiceException(BaseResponseCodeEnum.TENANT_NOT_EXISTS);
        }

        InfTraceContextHolder.get().setTenantName(accessTenant.getName());
        InfTraceContextHolder.get().setTenantId(accessTenant.getId());

        String loginMode = "";
        Map<String, String> map = null;
        AccessUser resUser = new AccessUser();
        JSONObject config = JSON.parseObject(accessTenant.getConfig());
        if (config != null && !config.isEmpty()) {
            loginMode = config.getString("login_mode");
            resUser.setIsMFA(config.getBoolean("is_enable_mfa"));
        }

        String mail = accessUser.getEmail();
        if(loginMode.equals(DsTaskConstant.LOGIN_MODE)) {
            try {
                map = ldapUtil.ldapLogin(accessUser.getEmail(), accessUser.getPassword());
            } catch (ServiceException e) {
                if(e.getCodeStr().equals(BaseResponseCodeEnum.USER_OR_PASSWORD_ERROR.name())) {
                    log.info("test--mail:" + mail);
                    AccessUser build = AccessUser.builder().tenantId(accessTenant.getId()).email(mail).build();
                    build.setDeleteStatus(0);
                    build.setFreezeStatus(0);
                    AccessUser checkUser = selectOne(build);
                    if (checkUser == null) {
                        throw new ServiceException(BaseResponseCodeEnum.USER_NOT_EXIST);
                    }
                    if (!accessUser.getPassword().equals(checkUser.getPassword())) {
                        throw new ServiceException(BaseResponseCodeEnum.USER_PASSWORD_ERROR);
                    }
                }
            }
        }

        AccessUser build = AccessUser.builder().tenantId(accessTenant.getId()).email(mail).build();
        build.setDeleteStatus(0);
        build.setFreezeStatus(0);
        AccessUser dbAccessUser = selectOne(build);

        if (dbAccessUser == null) {
            if (loginMode.equals(DsTaskConstant.LOGIN_MODE)) {
                String name = accessUser.getEmail().split("@")[0];
                accessUser.setName(name);
                accessUser.setPassword("");
                accessUser.setTenantId(accessTenant.getId());
                accessUser.setEmail(mail);
                accessUser.setPhone(map.get("telephoneNumber"));
                accessUser.setWeChatId(map.get("uid"));
                accessUser.setEName(map.get("displayName"));
                accessUser.setCreateBy(name);
                accessUser.setUpdateBy(name);

                if (accessUser.getTenantName().equals(BaseConstant.defaultTenantName)) {
                    accessUser.setRole(BaseConstant.COMMON_ROLE);
                }

                addUser(accessUser);
            } else {
                throw new ServiceException(BaseResponseCodeEnum.USER_NOT_EXISTS);
            }
        }

        if (!loginMode.equals(DsTaskConstant.LOGIN_MODE) && !accessUser.getPassword().equals(dbAccessUser.getPassword())) {
            throw new ServiceException(BaseResponseCodeEnum.USER_PASSWORD_ERROR);
        }

        if (InfTraceContextHolder.get().getAuthentication() == null) {
            Integer userId = dbAccessUser != null ? dbAccessUser.getId() : accessUser.getId();
            String name = dbAccessUser != null ? dbAccessUser.getName() : accessUser.getName();
            String email = dbAccessUser != null ? dbAccessUser.getEmail() : accessUser.getEmail();
            String password = dbAccessUser != null ? dbAccessUser.getPassword() : accessUser.getPassword();
            String org = dbAccessUser != null ? dbAccessUser.getOrg() : accessUser.getOrg();
            String tenancyCode = dbAccessUser != null ? dbAccessUser.getTenancyCode() : accessUser.getTenancyCode();
            List<AccessGroup> accessGroupList = accessGroupService.selectByUserIds(Collections.singletonList(userId));
            List<Integer> collect = accessGroupList.stream().map(BaseEntity::getId).collect(Collectors.toList());
            String groupIds = StringUtils.join(collect, ",");
            CurrentUser currentUser = CurrentUser.builder().name(name).id(userId)
                    .email(email).tenantId(accessTenant.getId()).tenantName(accessTenant.getName())
                    .password(password).groupName(org)
                    .groupIds(groupIds).group(tenancyCode).build();
            resUser.setToken(TokenUtil.generateToken(currentUser, 0));
        }

        //得先判断是否有绑定MFA
        if (dbAccessUser != null) {
            resUser.setIsBindmfa(dbAccessUser.getIsBindmfa());

            if (dbAccessUser.getIsBindmfa() != null && dbAccessUser.getIsBindmfa().equals("1")) {
                dbAccessUser.setTenantName(accessUser.getTenantName());
                //没绑定
                String secret = MultiFactorAuthenticatorUtil.generateSecretKey();
                String qrCodeLink = MultiFactorAuthenticatorUtil.getQRBarcodeURL(dbAccessUser, secret);
                resUser.setQrCodeLink(qrCodeLink);
                dbAccessUser.setMfaSecret(secret);
                update(dbAccessUser);
            }
        }
        resUser.setIsHasCloudResource(DataCakeConfigUtil.getCloudResourcesService().getIsHasCloudResource(accessTenant.getId()));

        return resUser;

//        CurrentUser currentUser = CurrentUser.builder().name(dbAccessUser.getName()).id(dbAccessUser.getId())
//                .email(dbAccessUser.getEmail()).tenantId(dbAccessUser.getTenantId())
//                .password(dbAccessUser.getPassword()).groupName(dbAccessUser.getOrg())
//                .group(dbAccessUser.getTenancyCode()).build();
//        resUser.setToken(TokenUtil.generateToken(currentUser, 0));
//        return resUser;
    }

    @Override
    public List<AccessUser> selectByNames(List<String> userNmes, Integer tenantId) {
        List<AccessUser> accessUserList = new ArrayList<>();
        for (String name : userNmes) {
            List<AccessUser> resList = accessUserMapper.selectByNames(name, tenantId);
            accessUserList.addAll(resList);
        }
        return accessUserList;
    }

    @Override
    public AccessUser checkMFACode(AccessUser accessUser) {
        AccessUser resUser = new AccessUser();
        AccessTenant accessTenant = accessTenantService.getByName(accessUser.getTenantName());

        InfTraceContextHolder.get().setTenantName(accessTenant.getName());
        InfTraceContextHolder.get().setTenantId(accessTenant.getId());

        AccessUser build = AccessUser.builder().tenantId(accessTenant.getId()).email(accessUser.getEmail())
                .build();
        build.setDeleteStatus(0);
        AccessUser dbAccessUser = selectOne(build);
        if (dbAccessUser == null) {
            throw new ServiceException(BaseResponseCodeEnum.USER_NOT_EXISTS);
        }

        String secret = dbAccessUser.getMfaSecret();
        long code = Long.parseLong(accessUser.getCodeInput());
        boolean result = MultiFactorAuthenticatorUtil.checkCode(secret, code, System.currentTimeMillis());
        if (!result) {
            throw new ServiceException(BaseResponseCodeEnum.USER_MFA_ERROR);
        }
        //校验通过 判断是否是初次校验
        if (dbAccessUser.getIsBindmfa().equals("1")) {
            dbAccessUser.setIsBindmfa("0");
            update(dbAccessUser);
        }


        List<AccessGroup> accessGroupList = accessGroupService.selectByUserIds(Collections.singletonList(dbAccessUser.getId()));

        List<Integer> collect = accessGroupList.stream().map(BaseEntity::getId).collect(Collectors.toList());
        String groupIds = StringUtils.join(collect, ",");
        CurrentUser currentUser = CurrentUser.builder().name(dbAccessUser.getName()).id(dbAccessUser.getId())
                .email(dbAccessUser.getEmail()).tenantId(dbAccessUser.getTenantId()).tenantName(accessTenant.getName())
                .password(dbAccessUser.getPassword()).groupName(dbAccessUser.getOrg())
                .groupIds(groupIds).group(dbAccessUser.getTenancyCode()).build();
        resUser.setToken(TokenUtil.generateToken(currentUser, 0));

        resUser.setTenantId(dbAccessUser.getTenantId())
                .setUserId(dbAccessUser.getId())
                .setTenantName(accessTenant.getName())
                .setName(dbAccessUser.getName())
                .setIsHasCloudResource(DataCakeConfigUtil.getCloudResourcesService().getIsHasCloudResource(dbAccessUser.getTenantId()));
        return resUser;
    }



    @Override
    public void unbundlingMFA(AccessUser accessUser) {
        AccessTenant accessTenant = accessTenantService.getByName(accessUser.getTenantName());

        InfTraceContextHolder.get().setTenantName(accessTenant.getName());
        InfTraceContextHolder.get().setTenantId(accessTenant.getId());

        AccessUser build = AccessUser.builder().tenantId(accessTenant.getId()).email(accessUser.getEmail())
                .build();
        build.setDeleteStatus(0);
        AccessUser dbAccessUser = selectOne(build);
        if (dbAccessUser == null) {
            throw new ServiceException(BaseResponseCodeEnum.USER_NOT_EXISTS);
        }
        dbAccessUser.setMfaSecret("").setIsBindmfa("1");
        update(dbAccessUser);
    }


    @Override
    public AccessUser selectByNameTenant(Integer tenantId, String name) {
        AccessUser.AccessUserBuilder accessUserBuilder = AccessUser.builder()
                .tenantId(tenantId);
        if (name.contains("@")) {
            accessUserBuilder.email(name);
        } else {
            accessUserBuilder.name(name);
        }
        AccessUser accessUser = accessUserBuilder.build();
        accessUser.setDeleteStatus(0);
        return getBaseMapper().selectOne(accessUser);
    }

    @Override
    public void superSave(AccessUser accessUser) {
        super.save(accessUser);
    }

    @Override
    public void addUser(AccessUser accessUser) {
       /* List<AccessUser> accessUserList = accessUserMapper.selectByEmail(accessUser);
        if (CollectionUtils.isNotEmpty(accessUserList)){
            throw new ServiceException(BaseResponseCodeEnum.EMAIL_IS_NOT_UNIQUE);
        }*/
        AccessUser old = accessUserMapper.selectByName(accessUser.getName());
        if (old != null){
            throw new ServiceException(BaseResponseCodeEnum.NAME_IS_NOT_UNIQUE);
        }
        String username = InfTraceContextHolder.get().getUserName();
        String createBy = accessUser.getCreateBy() != null ? accessUser.getCreateBy() : username;
        String updateBy = accessUser.getUpdateBy() != null ? accessUser.getCreateBy() : username;
        accessUser.setCreateBy(createBy);
        accessUser.setUpdateBy(updateBy);
        accessUser.setCreateTime(new Timestamp(System.currentTimeMillis()));
        accessUser.setUpdateTime(accessUser.getUpdateTime());
        accessUser.setDeleteStatus(DeleteEntity.NOT_DELETE);
        accessUser.setFreezeStatus(0);
        accessUser.setTenantId(InfTraceContextHolder.get().getTenantId());
        accessUser.setTenantName(InfTraceContextHolder.get().getTenantName());

        AccessRole accessRole = accessRoleMapper.selectByName(BaseConstant.COMMON_ROLE);
        if (accessRole == null) {
            throw new ServiceException(BaseResponseCodeEnum.ROLE_NOT_FOUND);
        }
        accessUserMapper.insert(accessUser);
        accessUserRoleService.addRole(accessUser.getId(), accessRole.getId().toString());
    }

    @Override
    public void editUser(AccessUser accessUser) {
       /* List<AccessUser> accessUserList=accessUserMapper.selectByEmail(accessUser);
        if (CollectionUtils.isNotEmpty(accessUserList)&&accessUserList.size()>1){
            throw new ServiceException(BaseResponseCodeEnum.EMAIL_IS_NOT_UNIQUE);
        }*/
        List<AccessUser> accessUsers=accessUserMapper.selectByUserName(accessUser);
        if (CollectionUtils.isNotEmpty(accessUsers)&&accessUsers.size()>1){
            throw new ServiceException(BaseResponseCodeEnum.NAME_IS_NOT_UNIQUE);
        }
        AccessUser old=accessUsers.get(0);
        old.setEmail(accessUser.getEmail());
        old.setName(accessUser.getName());
        old.setPhone(accessUser.getPhone());
        old.setEName(accessUser.getEName());
        old.setWeChatId(accessUser.getWeChatId());
        accessUserMapper.updateByPrimaryKey(old);
    }

    @Override
    public void freeze(Integer id, Integer freeze) {
        AccessUser accessUser = checkExist(id);
        accessUser.setFreezeStatus(freeze).setUpdateBy(InfTraceContextHolder.get().getUserName());
        super.update(accessUser);
    }

    @Override
    public AccessUser checkExist(Object id) {
        AccessUser accessUser = super.getById(id);

        if (accessUser == null) {
            throw new ServiceException(BaseResponseCodeEnum.DATA_NOT_FOUND, "用户不存在");
        }

        if (accessUser.getDeleteStatus() == 1) {
            throw new ServiceException(BaseResponseCodeEnum.DATA_NOT_FOUND, "用户已删除");
        }

        return accessUser;
    }

    @Override
    public AccessUser checkExistAndFreeze(Object id) {
        AccessUser accessUser = checkExist(id);

        if (accessUser.getFreezeStatus() == 1) {
            throw new ServiceException(BaseResponseCodeEnum.DATA_NOT_FOUND, "用户已冻结");
        }

        return accessUser;
    }

    @Override
    public AccessUser getByName(String name) {
        Integer tenantId = InfTraceContextHolder.get().getTenantId();
        AccessUser accessUserBuilder = AccessUser.builder().tenantId(tenantId).name(name).build();
        accessUserBuilder.setDeleteStatus(0);
        AccessUser accessUser = selectOne(accessUserBuilder);
        if (accessUser == null) {
            throw new ServiceException(BaseResponseCodeEnum.DATA_NOT_FOUND, "用户不存在");
        }

        if (accessUser.getDeleteStatus() == 1) {
            throw new ServiceException(BaseResponseCodeEnum.DATA_NOT_FOUND, "用户已删除");
        }

        if (accessUser.getFreezeStatus() == 1) {
            throw new ServiceException(BaseResponseCodeEnum.DATA_NOT_FOUND, "用户已冻结");
        }
        return accessUser;
    }

    @Override
    public List<AccessUser> selectByRoleId(Integer roleId) {
        List<AccessUser> accessUsers = accessUserMapper.selectByRoleId(roleId);
        return accessUsers;
    }

    @Override
    public List<AccessUser> selectByGroupId(Integer groupId) {
        return accessUserMapper.selectByGroupId(groupId);
    }

    @Override
    public List<AccessUser> selectByGroupIds(List<Integer> groupIds) {
        return accessUserMapper.selectByGroupIds(groupIds);
    }

    @Override
    public List<AccessUser> selectByTenantId(Integer tenantId) {
        List<AccessUser> accessUsers = accessUserMapper.selectByTenantId(tenantId);
        return accessUsers;
    }


    @Override
    public List<AccessUser> listByExample(AccessUser accessUser) {
        Map<String, String> map = new HashMap<>();
        map.put("name", accessUser.getName());
        map.put("tenantId", accessUser.getTenantId().toString());
        map.put("freeze", "0");

        Page<AccessUser> accessUsers = accessUserMapper.listByMap(map);
        List<AccessUser> result = accessUsers.getResult();
        for (AccessUser au : result) {
            au.setPassword(null);
        }
        return accessUsers;
    }

    @Override
    public void addGroup(Integer userId, String groupIds) {
        // roleIds 包含新的和旧的
        checkExistAndFreeze(userId);

        // 根据用户id ，删除用户下所有对应的旧角色
        accessUserRoleService.deleteByUserId(userId);

        if (StringUtils.isEmpty(groupIds)) {
            return;
        }

        String[] arr = groupIds.split(",");
        List<String> groups = Arrays.asList(arr);
        List<Integer> newGroupList = groups.stream().map(role -> Integer.parseInt(role)).collect(Collectors.toList());

        List<AccessUserGroup> result = newGroupList.stream().map(groupId -> {
            AccessUserGroup accessUserGroup = new AccessUserGroup(userId, groupId);
            accessUserGroup.setCreateBy(InfTraceContextHolder.get().getUserName())
                    .setCreateTime(new Timestamp(System.currentTimeMillis()))
                    .setUpdateBy(InfTraceContextHolder.get().getUserName())
                    .setUpdateTime(new Timestamp(System.currentTimeMillis()));
            return accessUserGroup;
        }).collect(Collectors.toList());
        accessUserGroupService.save(result);
    }

    @Override
    public void delete(Object id) {
        AccessUser accessUser = checkExistAndFreeze(Integer.parseInt(id.toString()));
        accessUser.setDeleteStatus(1).setUpdateBy(InfTraceContextHolder.get().getUserName());
        super.update(accessUser);
    }

    @Override
    public List<AccessUser> selectByIds(String ids) {
        return accessUserMapper.selectByIds(ids);
    }


}
