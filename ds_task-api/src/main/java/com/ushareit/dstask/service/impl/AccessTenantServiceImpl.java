package com.ushareit.dstask.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageInfo;
import com.ushareit.dstask.annotation.DisLock;
import com.ushareit.dstask.bean.*;
import com.ushareit.dstask.constant.*;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.mapper.AccessTenantMapper;
import com.ushareit.dstask.mapper.AccessUserMapper;
import com.ushareit.dstask.mapper.AkSkMapper;
import com.ushareit.dstask.mapper.AkSkTokenMapper;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import com.ushareit.dstask.service.*;
import com.ushareit.dstask.third.lakecat.LakeCatService;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import com.ushareit.dstask.web.utils.DataCakeConfigUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.ushareit.dstask.web.utils.ItUtil.getId;
import static com.ushareit.dstask.web.utils.ItUtil.getLenthId;

/**
 * @author wuyan
 * @date 2022/4/7
 */
@Slf4j
@Service
public class AccessTenantServiceImpl extends AbstractBaseServiceImpl<AccessTenant> implements AccessTenantService, CommandLineRunner {

    @Resource
    private AccessTenantMapper accessTenantMapper;
    @Resource
    private AccessUserService accessUserService;
    @Resource
    private AccessTenantProductService accessTenantProductService;
    @Resource
    private AccessRoleService accessRoleService;
    @Resource
    private AccessTenantRoleService accessTenantRoleService;
    @Resource
    private AccessUserRoleService accessUserRoleService;
    @Resource
    private LakeCatService lakeCatService;
    @Resource
    private AccessProductService accessProductService;

    @Resource
    private AkSkMapper akskMapper;

    @Resource
    private AkSkTokenMapper akSkTokenMapper;

    @Resource
    private AccessUserMapper accessUserMapper;

    @Resource
    private AccessGroupService accessGroupService;

    @Override
    public CrudMapper<AccessTenant> getBaseMapper() {
        return accessTenantMapper;
    }

    @Override
    @Transactional
    public Object save(AccessTenant accessTenant) {
        Pattern r = Pattern.compile(DsTaskConstant.EMAIL_PATTERN);
        // 现在创建 matcher 对象
        Matcher matcher = r.matcher(accessTenant.getManagerEmail());
        if (!matcher.find()) {
            throw new ServiceException(BaseResponseCodeEnum.USER_EMAIL_FORMAT_CHECK_FAIL);
        }

        //1.参数预校验
        preCheckCommon(accessTenant);
        super.save(accessTenant);

        // 创建数据库
        initDatabases(accessTenant.getName(), DataCakeConfigUtil.getDataCakeSourceConfig().getSqlFiles());

        // lakecat 创建租户
        lakeCatService.createTenant(accessTenant.getName());

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }

        return getByName(accessTenant.getName());
    }


    @Override
    public void insertAdminUser(AccessTenant webAccessTenant, AccessTenant dbAccessTenant) {

        AccessRole commonRole = accessRoleService.getByName("common");
        if (StringUtils.isNotEmpty(webAccessTenant.getUserRoleIds())
                && !webAccessTenant.getUserRoleIds().contains(commonRole.getId().toString())) {
            webAccessTenant.setUserRoleIds(webAccessTenant.getUserRoleIds() + "," + commonRole.getId());
        }

        //改变环境的租户
        InfTraceContextHolder.get().setTenantName(dbAccessTenant.getName());
        InfTraceContextHolder.get().setTenantId(dbAccessTenant.getId());

        //写入产品与租户关系表
        Example example = new Example(AccessProduct.class);

        Example.Criteria criteria = example.or();
        criteria.andEqualTo("deleteStatus", 0);
        List<AccessTenantProduct> list = new ArrayList<>();
        List<AccessProduct> accessProducts = accessProductService.listByExample(example);
        List<AccessTenantProduct> accessTenantProductList = accessProducts.stream().map(data -> {
            AccessTenantProduct build = AccessTenantProduct.builder().tenantId(dbAccessTenant.getId()).productId(data.getId())
                    .build();
            build.setCreateBy("admin").setUpdateBy("admin").setCreateTime(new Timestamp(System.currentTimeMillis()));
            list.add(build);
            return build;
        }).collect(Collectors.toList());

        accessTenantProductService.insertList(list);

        AccessUser accessUser = AccessUser.builder()
                .tenantName(dbAccessTenant.getName())
                .email(webAccessTenant.getManagerEmail())
                .name(webAccessTenant.getManagerEmail()).build();
        accessUser.setUserRoleIds("0");
        if (StringUtils.isEmpty(webAccessTenant.getUserRoleIds())) {
            //添加成root
            accessUser.setRole("admin");
            accessUserService.save(accessUser);
            AccessRole rootRole = accessRoleService.getByName("admin");
            webAccessTenant.setUserRoleIds(rootRole.getId().toString());
            AccessUser newUser = accessUserService.selectByNameTenant(dbAccessTenant.getId(), webAccessTenant.getManagerEmail());
            accessUserRoleService.addUsers(rootRole.getId(), newUser.getId().toString());
        } else {
            List<Integer> collect = Arrays.stream(webAccessTenant.getUserRoleIds().split(",")).map(Integer::parseInt)
                    .collect(Collectors.toList());
            List<AccessRole> accessRoleList = accessRoleService.listByIds(collect);
            List<Integer> ids = accessRoleList.stream().map(BaseEntity::getId).collect(Collectors.toList());
            accessUser.setRole(accessRoleList.stream().map(AccessRole::getName).collect(Collectors.joining(",")));
            accessUserService.save(accessUser);
            AccessUser newUser = accessUserService.selectByNameTenant(dbAccessTenant.getId(), webAccessTenant.getManagerEmail());
            for (Integer id : ids) {
                accessUserRoleService.addUsers(id, newUser.getId().toString());
            }

            //将角色附加给新租户
            List<Integer> collectData = collect.stream().map(data -> {
                AccessTenantRole build = AccessTenantRole.builder().tenantId(dbAccessTenant.getId())
                        .roleId(data).build();
                build.setCreateBy(InfTraceContextHolder.get().getUserName())
                        .setUpdateBy(InfTraceContextHolder.get().getUserName())
                        .setCreateTime(new Timestamp(System.currentTimeMillis()))
                        .setUpdateTime(new Timestamp(System.currentTimeMillis()));
                accessTenantRoleService.save(build);
                return data;
            }).collect(Collectors.toList());
        }

    }


    private void preCheckCommon(AccessTenant accessTenant) {
        //1.校验名称
//        preCheckName(accessTenant);

        //3.Name不重复校验
        super.checkOnUpdate(super.getByName(accessTenant.getName()), accessTenant);
    }

    private void preCheckName(AccessTenant accessTenant) {
        if (!match(accessTenant.getName(), DsTaskConstant.TENANT_NAME_PATTERN)) {
            throw new ServiceException(BaseResponseCodeEnum.NAME_NOT_MATCH);
        }
    }

    public AkSk getAkSk(CurrentUser currentUser) {
        AccessTenant accessTenant = super.getByName(currentUser.getTenantName());
        AkSk akSk = akskMapper.selectByName(currentUser.getTenantName());

        if (akSk == null) {
            akSk = AkSk.builder().ak(getId()).sk(getLenthId(8))
                    .tenantName(currentUser.getTenantName())
                    .applyUserId(currentUser.getUserId())
                    .tenantId(currentUser.getTenantId())
                    .valid(1)
                    .createBy(currentUser.getUserName())
                    .updateBy(currentUser.getUserName())
                    .createTime(new Timestamp(System.currentTimeMillis()))
                    .updateTime(new Timestamp(System.currentTimeMillis()))
                    .build();
            akskMapper.insert(akSk);
        }
        akSk.setDescription(accessTenant.getDescription());
        akSk.setEmail(accessTenant.getManagerEmail());
        return akSk;
    }

    public AkSk updateAkSk(AkSkRequest akskRequest, CurrentUser currentUser) {
        AkSk akSk = akskMapper.selectByName(currentUser.getTenantName());
        if (akSk != null) {
            AccessTenant accessTenant = super.getByName(currentUser.getTenantName());
            if (akskRequest.getType().equals(1)) {
                accessTenant.setDescription(akskRequest.getDescription());
                super.update(accessTenant);
            } else if (akskRequest.getType().equals(2)) {
                akSk.setAk(getId());
                akSk.setSk(getLenthId(8));
            }
            akSk.setUpdateBy(currentUser.getUserName());
            akSk.setUpdateTime(new Timestamp(System.currentTimeMillis()));
            akSk.setDescription(accessTenant.getDescription());
            akSk.setEmail(accessTenant.getManagerEmail());
            akskMapper.updateByPrimaryKey(akSk);
        } else {
            log.error(String.format("updateAkSk failed to get aksk[%s] info: %s", currentUser.getTenantName(), "该租户aksk信息为空"));
            throw new RuntimeException(ServiceErrorCodeEnum.SYS_AKSK_ERR.getMessage());
        }

        return akSk;
    }

    public String generateAkSkToken(AkSkTokenRequest akSkTokenRequest) throws UnsupportedEncodingException {
        AkSk akSk = akskMapper.selectByAksk(akSkTokenRequest.getAk(), akSkTokenRequest.getSk());
        if (akSk == null || !akSk.getValid().equals(1)) {
            return null;
        }

        AkSkToken info = new AkSkToken();
        info.setTenantName(akSk.getTenantName());
        info.setTenantId(akSk.getTenantId());
        info.setIsAdmin(1);
        String token = new String(Base64.getEncoder().encode(URLEncoder.encode(JSON.toJSONString(info), "UTF-8").getBytes()));
        AkSkToken akSkToken = AkSkToken.builder()
                .token(token)
                .tenantName(akSk.getTenantName())
                .tenantId(akSk.getTenantId())
                .isAdmin(1)
                .createBy(akSk.getUpdateBy())
                .createTime(new Timestamp(System.currentTimeMillis()))
                .build();
        akSkTokenMapper.insert(akSkToken);
        return akSkToken.getToken();
    }

    private void checkUser(CurrentUser currentUser) {
        InfTraceContextHolder.get().setTenantName(currentUser.getTenantName());
        InfTraceContextHolder.get().setTenantId(currentUser.getTenantId());

        AccessUser build = AccessUser.builder().tenantId(currentUser.getTenantId()).name(currentUser.getUserName()).build();
        build.setDeleteStatus(0);
        AccessUser accessUser = accessUserMapper.selectOne(build);
        if (accessUser == null || accessUser.getDeleteStatus().equals(1) || accessUser.getFreezeStatus().equals(1)) {
            throw new ServiceException(BaseResponseCodeEnum.USER_NOT_EXISTS, "当前用户已删除或已冻结");
        }
    }

    public String generateAkSkPersonalToken(AkSkTokenRequest akSkTokenRequest, CurrentUser currentUser) throws UnsupportedEncodingException {
        checkUser(currentUser);

        long now = System.currentTimeMillis();
        Timestamp expiration = new Timestamp(now + akSkTokenRequest.getEffectiveTime() * 86400000L);

        AkSkToken info = new AkSkToken();
        info.setTenantName(currentUser.getTenantName());
        info.setTenantId(currentUser.getTenantId());
        info.setExpiration(expiration);
        info.setUserName(currentUser.getUserName());
        String token = new String(Base64.getEncoder().encode(URLEncoder.encode(JSON.toJSONString(info), "UTF-8").getBytes()));
        AkSkToken akSkToken = AkSkToken.builder()
                .token(token)
                .tenantName(currentUser.getTenantName())
                .tenantId(currentUser.getTenantId())
                .EffectiveTime(akSkTokenRequest.getEffectiveTime())
                .expiration(expiration)
                .userName(currentUser.getUserName())
                .createBy(InfTraceContextHolder.get().getUserName())
                .createTime(new Timestamp(now))
                .build();
        akSkTokenMapper.insert(akSkToken);
        return akSkToken.getToken();
    }

    public AkSkResponse getAkSkPersonalToken(CurrentUser currentUser) {
        checkUser(currentUser);

        AkSkResponse akSkResponse = new AkSkResponse();
        AkSkToken akSkToken = akSkTokenMapper.selectByUser(currentUser.getTenantName(), currentUser.getTenantId(), currentUser.getUserName());
        if (akSkToken == null) {
            return akSkResponse.setDeadline("").setDatacakeToken("");
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String deadline = sdf.format(new Date(Long.parseLong(String.valueOf(akSkToken.getExpiration().getTime()))));

        akSkResponse.setDatacakeToken(akSkToken.getToken());
        akSkResponse.setDeadline(deadline);
        return akSkResponse;
    }

    public Map<String, Object> getUserInfo(String token) {
        AkSkToken akSkToken = akSkTokenMapper.selectBytoken(token);
        if (akSkToken == null) {
            throw new ServiceException(BaseResponseCodeEnum.TOKEN_INVALID);
        }

        AccessTenant accessTenant = super.getByName(akSkToken.getTenantName());
        if (accessTenant == null || accessTenant.getDeleteStatus().equals(1) || accessTenant.getFreezeStatus().equals(1)) {
            throw new ServiceException(BaseResponseCodeEnum.TENANT_NOT_EXISTS);
        }

        CurrentUser user;
        Map<String, Object> map = new HashMap<>();
        if (akSkToken.getIsAdmin() != null && akSkToken.getIsAdmin().equals(1)) {
            user = CurrentUser.builder()
                    .tenantId(akSkToken.getTenantId())
                    .tenantName(akSkToken.getTenantName())
                    .email(accessTenant.getManagerEmail())
                    .roles("common")
                    .userName("admin")
                    .userId("admin")
                    .build();
            map.put("data", user);
            map.put("expiration", new Timestamp(System.currentTimeMillis() + 360 * 86400000L));
            return map;
        }

        Timestamp now = new Timestamp(System.currentTimeMillis());
        int compare = akSkToken.getExpiration().compareTo(now);
        if (compare < 0) {
            throw new ServiceException(BaseResponseCodeEnum.USER_TOKEN_EXPIRED);
        }

        InfTraceContextHolder.get().setTenantName(akSkToken.getTenantName());
        InfTraceContextHolder.get().setTenantId(akSkToken.getTenantId());

        AccessUser build = AccessUser.builder().tenantId(akSkToken.getTenantId()).name(akSkToken.getUserName()).build();
        build.setDeleteStatus(0);
        AccessUser accessUser = accessUserMapper.selectOne(build);
        if (accessUser == null || accessUser.getDeleteStatus().equals(1) || accessUser.getFreezeStatus().equals(1)) {
            throw new ServiceException(BaseResponseCodeEnum.USER_NOT_EXISTS);
        }

        List<AccessGroup> accessGroupList = accessGroupService.getParentGroupList(InfTraceContextHolder.get().getTenantId(), accessUser.getName());
        List<Integer> collect = accessGroupList.stream().map(BaseEntity::getId).collect(Collectors.toList());
        String groupIds = StringUtils.join(collect, ",");

        user = CurrentUser.builder()
                .id(accessUser.getId())
                .userName(accessUser.getName())
                .userId(accessUser.getName())
                .email(accessUser.getEmail())
                .tenantName(akSkToken.getTenantName())
                .tenantId(accessUser.getTenantId())
                .group(accessUser.getTenancyCode())
                .groupIds(groupIds)
                .build();
        map.put("data", user);
        map.put("expiration", akSkToken.getExpiration().getTime());
        return map;
    }

    @Override
    public void freeze(Integer id, Integer freeze) {
        // 1表示冻结 0表示复活
        AccessTenant accessTenant = super.getById(id);

        if (accessTenant == null) {
            throw new ServiceException(BaseResponseCodeEnum.DATA_NOT_FOUND, "租户不存在");
        }

        if (accessTenant.getDeleteStatus() == 1) {
            throw new ServiceException(BaseResponseCodeEnum.DATA_NOT_FOUND, "租户已删除");
        }

        accessTenant.setFreezeStatus(freeze);

        accessTenant.setUpdateBy(InfTraceContextHolder.get().getUserName());
        // 租户与用户动作同步
        List<AccessUser> accessUsers = accessUserService.selectByTenantId(id);
        accessUsers.stream().forEach(user -> user.setFreezeStatus(freeze));

        super.update(accessTenant);
        accessUserService.update(accessUsers);
    }

    @Override
    public AccessTenant checkExist(Integer id) {
        AccessTenant accessTenant = super.getById(id);

        if (accessTenant == null) {
            throw new ServiceException(BaseResponseCodeEnum.DATA_NOT_FOUND, "租户不存在");
        }

        if (accessTenant.getDeleteStatus() == 1) {
            throw new ServiceException(BaseResponseCodeEnum.DATA_NOT_FOUND, "租户已删除");
        }

        if (accessTenant.getFreezeStatus() == 1) {
            throw new ServiceException(BaseResponseCodeEnum.DATA_NOT_FOUND, "租户已冻结");
        }

        return accessTenant;
    }

    @Override
    public void config(Integer id, String productIds) {
        AccessTenant accessTenant = checkExist(id);

        InfTraceContextHolder.get().setTenantName(accessTenant.getName());
        InfTraceContextHolder.get().setTenantId(accessTenant.getId());

        // 先按租户id删除旧的产品
        accessTenantProductService.deleteByTenantId(id);

        if (StringUtils.isEmpty(productIds)) {
            return;
        }

        // 存入tenant-product表
        String[] split = productIds.split(",");
        List<AccessTenantProduct> list = Arrays.asList(split).stream().map(productId -> {
                    int proId = Integer.parseInt(productId);
                    AccessTenantProduct accessTenantProduct = new AccessTenantProduct(id, proId);
                    accessTenantProduct.setCreateBy(InfTraceContextHolder.get().getUserName())
                            .setUpdateBy(InfTraceContextHolder.get().getUserName())
                            .setCreateTime(new Timestamp(System.currentTimeMillis()))
                            .setUpdateTime(new Timestamp(System.currentTimeMillis()));
                    return accessTenantProduct;
                })
                .collect(Collectors.toList());

        accessTenantProductService.save(list);
    }

    @Override
    public PageInfo<AccessTenant> listByPage(int pageNum, int pageSize, Map<String, String> paramMap) {
        PageInfo<AccessTenant> pageInfo = super.listByPage(pageNum, pageSize, paramMap);
        List<AccessTenant> list = pageInfo.getList();

//        list.stream().forEach(tenant -> {
//            if (StringUtils.isNotEmpty(tenant.getManagerEmail())) {
//                //改变环境的租户
//                InfTraceContextHolder.get().setTenantName(tenant.getName());
//                InfTraceContextHolder.get().setTenantId(tenant.getId());
//                AccessUser accessUser = accessUserService.selectByNameTenant(tenant.getId(), tenant.getManagerEmail());
//                List<Integer> roleIds = accessUserRoleService.selectByUserId(accessUser.getId());
//                List<AccessRole> accessRoles = accessRoleService.listByIds(roleIds.stream());
//                List<AccessRole> filter = accessRoles.stream().filter(role -> role.getDeleteStatus() == 0).collect(Collectors.toList());
//                String names = filter.stream().map(AccessRole::getName).collect(Collectors.joining(","));
//                tenant.setUserRoleIds(names);
//            }
//        });
        return pageInfo;
    }

    @Override
    public void delete(Object id) {
        AccessTenant accessTenant = checkExist(Integer.parseInt(id.toString()));
        accessTenant.setDeleteStatus(1).setUpdateBy(InfTraceContextHolder.get().getUserName());
        super.update(accessTenant);
    }

    @Override
    public AccessTenant current(Integer tenantId) {
        AccessTenant accessTenant = checkExist(tenantId);
        return accessTenant;
    }

    @Override
    public List<AccessTenant> getActiveList() {
        Example example = new Example(AccessTenant.class);
        example.or()
                .andEqualTo("freezeStatus", FreezeStatus.ACTIVE.getType())
                .andEqualTo("deleteStatus", DeleteEntity.NOT_DELETE);

        return accessTenantMapper.selectByExample(example);
    }

    @Override
    public void initDatabases(String tenantName, String sqlFiles) {
        long start = System.currentTimeMillis();
        DataCakeConfigUtil.getDataCakeSourceConfig().execute(connection -> {
            try {
                connection.setAutoCommit(false);
                Arrays.stream(sqlFiles.split(SymbolEnum.COMMA.getSymbol())).map(String::trim)
                        .forEach(item -> {
                            InputStreamResource streamResource = parseScript(tenantName, item);
                            EncodedResource er = new EncodedResource(streamResource, StandardCharsets.UTF_8);
                            ScriptUtils.executeSqlScript(connection, er);
                        });
                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                log.error(e.getMessage(), e);
                throw new ServiceException(BaseResponseCodeEnum.CLI_INIT_DB_FAIL, e);
            } finally {
                connection.close();
                log.info("init database total cost {} ms", System.currentTimeMillis() - start);
            }
        });
    }

    private InputStreamResource parseScript(String tenantName, String sqlPath) {
        FileSystemResource rc = new FileSystemResource("/data/code/sql/" + sqlPath.trim());
        Map<String, Object> params = new HashMap<>();
        params.put(CommonConstant.TENANT_NAME_KEY, tenantName);
        params.put("adminEmail", DataCakeConfigUtil.getDataCakeSourceConfig().getAdminEmail());
        StringSubstitutor sub = new StringSubstitutor(params);

        try {
            BufferedReader br = new BufferedReader(new FileReader(rc.getFile()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(sub.replace(line)).append("\n");
            }

            log.info("to execute sql is {}", sb);
            InputStream inputStream = IOUtils.toInputStream(sb.toString(), StandardCharsets.UTF_8);
            return new InputStreamResource(inputStream);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new ServiceException(BaseResponseCodeEnum.SYS_ERR, e.getMessage());
        }
    }

    //服务启动自己调个接口
    @Override
    @DisLock(key = "serviceInitData", expiredSeconds = 30, isRelease = false)
    public void run(String... args) throws Exception {
//        String env = DataCakeConfigUtil.getDataCakeSourceConfig().getActive();
//        String superTenant = DataCakeConfigUtil.getDataCakeSourceConfig().getSuperTenant();
//        log.info("initDatabases start ");
//        AccessTenant admin = getByName(superTenant);
//        if (admin != null || !DataCakeConfigUtil.getDataCakeConfig().getDcRole() ||
//                (StringUtils.isNotEmpty(env) && (env.contains("dev") || env.contains("test")))) {
//            return;
//        }
//        // 创建数据库
//        initDatabases(superTenant, DataCakeConfigUtil.getDataCakeSourceConfig().getInitAdminSqlFiles());
//        log.info("initDatabases is success ");
//
//        //更新不规则的用户
//        Example example = new Example(AccessUser.class);
//        Example.Criteria criteria = example.or();
//        criteria.andEqualTo("deleteStatus", DeleteEntity.NOT_DELETE);
//        List<AccessUser> accessUserList = accessUserService.listByExample(example);
//
//        List<AccessUser> collect = accessUserList.stream().map(data -> {
//            String name = data.getName();
//            if (name.contains("@")) {
//                String[] split = name.split("@");
//                data.setName(split[0]);
//                accessUserService.update(data);
//            }
//            return data;
//        }).collect(Collectors.toList());
//
//
//        // lakecat 创建租户
//        lakeCatService.createTenant("ninebot");
//        log.info(" lakeCatService create success ");
    }
}
