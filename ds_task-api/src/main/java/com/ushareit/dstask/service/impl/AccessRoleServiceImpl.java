package com.ushareit.dstask.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageInfo;
import com.ushareit.dstask.bean.*;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.constant.SymbolEnum;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.mapper.AccessRoleMapper;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import com.ushareit.dstask.service.*;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author wuyan
 * @date 2022/4/12
 */
@Slf4j
@Service
public class AccessRoleServiceImpl extends AbstractBaseServiceImpl<AccessRole> implements AccessRoleService {

    @Resource
    private AccessRoleMapper accessRoleMapper;
    @Resource
    private AccessTenantService accessTenantService;
    @Resource
    private AccessUserService accessUserService;
    @Resource
    private AccessMenuService accessMenuService;
    @Resource
    private AccessUserRoleService accessUserRoleService;
    @Resource
    private AccessTenantRoleService accessTenantRoleService;
    @Resource
    private AccessRoleMenuService accessRoleMenuService;

    @Override
    public CrudMapper<AccessRole> getBaseMapper() {
        return accessRoleMapper;
    }

    @Override
    public Object save(AccessRole accessRole) {
        Integer tenantId = accessRole.getId();
        accessTenantService.checkExist(tenantId);
        accessRole.setId(null);
        AccessRole old=accessRoleMapper.selectByName(accessRole.getName());
        if (old!=null){
            throw new ServiceException(BaseResponseCodeEnum.ROLE_EXSITTS);
            //return old;
        }
        //存入role表
        accessRole.setCreateBy(InfTraceContextHolder.get().getUserName())
                .setUpdateBy(InfTraceContextHolder.get().getUserName());
        super.save(accessRole);

        //存入tenant-role表
        AccessTenantRole accessTenantRole = new AccessTenantRole(tenantId, accessRole.getId());
        accessTenantRole.setCreateBy(InfTraceContextHolder.get().getUserName());
        accessTenantRole.setUpdateBy(InfTraceContextHolder.get().getUserName());
        accessTenantRoleService.save(accessTenantRole);


        return accessRole;
    }

    @Override
    public void update(@RequestBody @Valid AccessRole accessRoleFromWeb) {
        AccessRole accessRoleFromDb = checkExist(accessRoleFromWeb.getId());
        super.checkOnUpdate(accessRoleFromDb, accessRoleFromWeb);
        accessRoleFromWeb.setCreateBy(accessRoleFromDb.getCreateBy())
                .setUpdateBy(InfTraceContextHolder.get().getUserName())
                .setCreateTime(accessRoleFromDb.getCreateTime())
                .setUpdateTime(new Timestamp(System.currentTimeMillis()));
        super.update(accessRoleFromWeb);
    }

    @Override
    public AccessRole checkExist(Object id) {
        AccessRole accessRole = super.getById(id);

        if (accessRole == null) {
            throw new ServiceException(BaseResponseCodeEnum.DATA_NOT_FOUND, "角色不存在");
        }

        if (accessRole.getDeleteStatus() == 1) {
            throw new ServiceException(BaseResponseCodeEnum.DATA_NOT_FOUND, "角色已删除");
        }

        return accessRole;
    }

    @Override
    public void updateMenus(Integer id, String menuIds) {
        checkExist(id);

        // 根据角色id ，删除角色下所有对应的旧菜单
        accessRoleMenuService.deleteByRoleId(id);

        if (StringUtils.isEmpty(menuIds)) {
            return;
        }

        String[] split = menuIds.split(",");
        List<AccessRoleMenu> accessRoleMenus = Arrays.asList(split).stream().map(menuId -> {
            int i = Integer.parseInt(menuId);
            AccessRoleMenu accessRoleMenu = new AccessRoleMenu(id, i);
            accessRoleMenu.setCreateBy(InfTraceContextHolder.get().getUserName())
                    .setUpdateBy(InfTraceContextHolder.get().getUserName())
                    .setCreateTime(new Timestamp(System.currentTimeMillis()))
                    .setUpdateTime(new Timestamp(System.currentTimeMillis()));
            return accessRoleMenu;
        }).collect(Collectors.toList());

        accessRoleMenuService.save(accessRoleMenus);
    }

    @Override
    public List<AccessRole> selectByUserName(String userName) {
        return accessRoleMapper.selectByUserName(userName);
    }

    @Override
    public List<AccessRole> selectByUserId(Integer userId) {
        return accessRoleMapper.selectByUserId(userId);
    }

    @Override
    public void delete(Object id) {
        AccessRole accessRole = checkExist(id);
        accessRole.setDeleteStatus(1)
                .setUpdateBy(InfTraceContextHolder.get().getUserName());
        super.update(accessRole);
    }

    @Override
    public PageInfo<AccessRole> listByPage(int pageNum, int pageSize, Map<String, String> paramMap) {
        Page<AccessRole> page = accessRoleMapper.listByMap(paramMap);

        List<AccessRole> list = page.getResult();
//        list.removeIf(accessRole -> "root".equalsIgnoreCase(accessRole.getName()));
        addUserNum(list);
        PageInfo<AccessRole> pageInfo = getPageInfo(pageNum, pageSize, list);

        return pageInfo;
    }

    private void addUserNum(List<AccessRole> accessRoles) {
        accessRoles.stream().forEach(accessRole -> {
            Integer id = accessRole.getId();
            int userNum = accessUserRoleService.selectByRoleId(id).size();
            accessRole.setUserNum(userNum);
        });
    }


    @Override
    public List<AccessRole> listByExample(AccessRole accessRole) {
        accessRole.setDeleteStatus(0);
        accessRole.setId(null);
        List<AccessRole> accessRoles = accessRoleMapper.select(accessRole);
        return accessRoles;
    }

    /**
     * @param id 主键
     * @return
     */
    @Override
    public AccessRole getById(Object id) {
        AccessRole accessRole = checkExist(Integer.parseInt(id.toString()));
        List<AccessUser> users = accessUserService.selectByRoleId(Integer.parseInt(id.toString()));
        accessRole.setUsers(users);

        // 角色id所属租户id
        // 所属租户的产品集合
        // 产品集合对应的菜单集合
        List<AccessMenu> menus = accessMenuService.selectMenusByRoleId(Integer.parseInt(id.toString()));

        List<AccessMenu> result = new ArrayList<>();

        // 先找到一级菜单
        for (AccessMenu menu : menus) {
            if (menu.getLevel() == 1) {
                result.add(menu);
            }
        }

        // 为一级菜单设置子菜单，getChild是递归调用的
        for (AccessMenu menu : result) {
            menu.setChildren(accessMenuService.getChild(menu.getId(), menus));
        }

        accessRole.setMenus(result);

        // 角色选了哪些菜单
        List<AccessMenu> checkMenus = accessMenuService.selectCheckedMenusByRoleId(Integer.parseInt(id.toString()));
        List<Integer> menuChecked = checkMenus.stream().filter(menu -> menu.getLevel() < 3).map(AccessMenu::getId).collect(Collectors.toList());
        List<Integer> actionChecked = checkMenus.stream().filter(menu -> menu.getLevel() == 3).map(AccessMenu::getId).collect(Collectors.toList());

        accessRole.setMenuChecked(menuChecked);
        accessRole.setActionChecked(actionChecked);

        return accessRole;
    }

    @Override
    public AccessRole getCommonRoleId(Integer id) {
        AccessRole accessRole = accessRoleMapper.getCommonRoleId(id);

        if (accessRole == null) {
            throw new ServiceException(BaseResponseCodeEnum.DATA_NOT_FOUND, "角色不存在");
        }

        if (accessRole.getDeleteStatus() == 1) {
            throw new ServiceException(BaseResponseCodeEnum.DATA_NOT_FOUND, "角色已删除");
        }

        return accessRole;
    }

    @Override
    public List<AccessRole> getRoleList(String tenantName, Collection<String> roleNames) {
        if (CollectionUtils.isEmpty(roleNames)) {
            return Collections.emptyList();
        }

        InfTraceContextHolder.get().setTenantName(tenantName);
        Example example = new Example(AccessRole.class);
        example.or()
                .andIn("name", roleNames)
                .andEqualTo("deleteStatus", DeleteEntity.NOT_DELETE);
        return accessRoleMapper.selectByExample(example);
    }

    @Override
    public void initRoles(String tenantName, Collection<AccessRole> roleList) {
        if (CollectionUtils.isEmpty(roleList)) {
            return;
        }

        InfTraceContextHolder.get().setTenantName(tenantName);
        roleList.stream()
                .filter(item -> item.getName().contains(SymbolEnum.UNDERLINE.getSymbol()))
                .map(item -> {
                    AccessRole accessRole = new AccessRole();
                    accessRole.setId(item.getId());
                    accessRole.setName(item.getName().replaceAll("^[^_]_+", StringUtils.EMPTY));
                    accessRole.setDescription(item.getDescription());
                    accessRole.setDeleteStatus(DeleteEntity.NOT_DELETE);
                    accessRole.setCreateBy("admin");
                    accessRole.setUpdateBy("admin");
                    accessRole.setCreateTime(new Timestamp(System.currentTimeMillis()));
                    accessRole.setCreateTime(new Timestamp(System.currentTimeMillis()));
                    return accessRole;
                }).forEach(accessRoleMapper::insertSelective);
    }
}
