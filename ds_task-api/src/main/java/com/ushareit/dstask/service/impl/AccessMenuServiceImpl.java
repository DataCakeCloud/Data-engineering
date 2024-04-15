package com.ushareit.dstask.service.impl;

import com.ushareit.dstask.bean.AccessMenu;
import com.ushareit.dstask.bean.DeleteEntity;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.mapper.AccessMenuMapper;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import com.ushareit.dstask.service.AccessMenuService;
import com.ushareit.dstask.service.AccessRoleService;
import com.ushareit.dstask.service.AccessUserRoleService;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import com.ushareit.dstask.web.utils.DataCakeConfigUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author wuyan
 * @date 2022/4/12
 */
@Slf4j
@Service
public class AccessMenuServiceImpl extends AbstractBaseServiceImpl<AccessMenu> implements AccessMenuService {
    @Resource
    private AccessMenuMapper accessMenuMapper;

    @Resource
    private AccessUserRoleService accessUserRoleService;

    @Resource
    private AccessRoleService accessRoleService;


    @Override
    public CrudMapper<AccessMenu> getBaseMapper() {
        return accessMenuMapper;
    }

    @Override
    public Object save(AccessMenu accessMenu) {
        String url = accessMenu.getUrl();
        accessMenu
                .setUrl(url == null ? null : url.trim())
                .setCreateBy(InfTraceContextHolder.get().getUserName())
                .setUpdateBy(InfTraceContextHolder.get().getUserName())
                .setCreateTime(new Timestamp(System.currentTimeMillis()))
                .setUpdateTime(new Timestamp(System.currentTimeMillis()));

        super.save(accessMenu);
        return accessMenu;
    }

    @Override
    public AccessMenu getById(Object id) {
        AccessMenu accessMenu = super.getById(id);
        return accessMenu;
    }

    @Override
    public void update(@RequestBody @Valid AccessMenu accessMenu) {
        checkExist(accessMenu.getId());

        String url = accessMenu.getUrl();
        accessMenu
                .setUrl(url == null ? null : url.trim())
                .setUpdateBy(InfTraceContextHolder.get().getUserName())
                .setUpdateTime(new Timestamp(System.currentTimeMillis()));
        super.update(accessMenu);
    }

    @Override
    public List<AccessMenu> list(Integer userId) {
        // 查到这个用户是否有root角色，如果没有，则不能将菜单展示出来
        List<Integer> roleIds = accessUserRoleService.selectByUserId(userId);

        boolean isRoot = InfTraceContextHolder.get().getUserInfo().isAdmin();//list.stream().filter(role -> role.getDeleteStatus() == 0).anyMatch(role -> "admin".equalsIgnoreCase(role.getName()));

        if (!isRoot) {
            log.info("非root角色，不能展示菜单");
            return new ArrayList<>();
        }

        // root角色，展示表所有菜单
        List<AccessMenu> allMenus = accessMenuMapper.selectAll().stream().filter(menu -> menu.getDeleteStatus() == 0)
                .collect(Collectors.toList());

        List<AccessMenu> result = new ArrayList<>();

        // 先找到一级菜单
        for (AccessMenu menu : allMenus) {
            if (menu.getLevel() == 1) {
                result.add(menu);
            }
        }

        // 为一级菜单设置子菜单，getChild是递归调用的
        for (AccessMenu menu : result) {
            menu.setChildren(getChild(menu.getId(), allMenus));
        }

        return result;
    }

    @Override
    public List<AccessMenu> getChild(Integer id, List<AccessMenu> allMenus) {
        // 子菜单
        List<AccessMenu> child = new ArrayList<>();
        for (AccessMenu menu : allMenus) {
            if (id.equals(menu.getParentMenuId())) {
                child.add(menu);
            }
        }

        // 将子菜单的子菜单再循环一遍
        for (AccessMenu menu : child) {
            menu.setChildren(getChild(menu.getId(), allMenus));
        }

        if (child.size() == 0) {
            return null;
        }
        return child;
    }

    @Override
    public List<AccessMenu> selectByUrl(List<String> urls) {
        return accessMenuMapper.selectByUrl(urls);
    }

    @Override
    public List<AccessMenu> produceMenus(List<AccessMenu> allMenus) {
        if (allMenus == null || allMenus.size() == 0) {
            return new ArrayList<>();
        }

        List<AccessMenu> result = new ArrayList<>();

        // 先找到一级菜单
        for (AccessMenu menu : allMenus) {
            if (menu.getLevel() == 1) {
                result.add(menu);
            }
        }

        // 为一级菜单设置子菜单，getChild是递归调用的
        for (AccessMenu menu : result) {
            menu.setChildren(getChild(menu.getId(), allMenus));
        }

        return result;
    }

    @Override
    public List<AccessMenu> selectMenusByRoleId(Integer roleId) {
        List<AccessMenu> accessMenus = accessMenuMapper.selectByRoleId(roleId);
        return accessMenus;
    }

    @Override
    public List<AccessMenu> selectMenusByUserId(Integer userId) {
        List<AccessMenu> accessMenus = accessMenuMapper.selectByUserId(userId);
        return accessMenus;
    }

    @Override
    public List<AccessMenu> selectCheckedMenusByRoleId(Integer roleId) {
        return accessMenuMapper.selectCheckedMenusByRoleId(roleId);
    }

    @Override
    public void initMenus(String tenantName) {
        InfTraceContextHolder.get().setTenantName(DataCakeConfigUtil.getDataCakeSourceConfig().getSuperTenant());
        Example example = new Example(AccessMenu.class);
        example.or()
                .andEqualTo("deleteStatus", DeleteEntity.NOT_DELETE);
        List<AccessMenu> accessMenuList = accessMenuMapper.selectByExample(example);

        InfTraceContextHolder.get().setTenantName(tenantName);
        accessMenuList.stream().peek(item -> {
            item.setCreateBy("admin");
            item.setUpdateBy("admin");
            item.setCreateTime(new Timestamp(System.currentTimeMillis()));
            item.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        }).forEach(accessMenuMapper::insertSelective);
    }

    @Override
    public void delete(Object id) {
        AccessMenu accessMenu = checkExist(id);
        List<AccessMenu> accessMenus = accessMenuMapper.selectByParentId(accessMenu.getId());
        if (accessMenus.size() != 0) {
            throw new ServiceException(BaseResponseCodeEnum.EXIST_CHILD_MENU);
        }
        accessMenu.setDeleteStatus(1).setUpdateBy(InfTraceContextHolder.get().getUserName());
        super.update(accessMenu);
    }

    private AccessMenu checkExist(Object id) {
        AccessMenu accessMenu = super.getById(id);
        if (accessMenu == null) {
            throw new ServiceException(BaseResponseCodeEnum.DATA_NOT_FOUND, "菜单不存在");
        }

        if (accessMenu.getDeleteStatus() == 1) {
            throw new ServiceException(BaseResponseCodeEnum.DATA_NOT_FOUND, "菜单已删除");
        }
        return accessMenu;
    }
}
