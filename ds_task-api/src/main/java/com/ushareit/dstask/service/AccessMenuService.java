package com.ushareit.dstask.service;

import com.ushareit.dstask.bean.AccessMenu;

import java.util.List;
import java.util.Map;

/**
 * @author wuyan
 * @date 2022/4/12
 */
public interface AccessMenuService  extends BaseService<AccessMenu>{
    List<AccessMenu> list(Integer userId);

    List<AccessMenu> produceMenus(List<AccessMenu> accessMenus);

    /**
     * 通过角色id找到租户，再找到产品，再找到菜单
     * 通过角色id找到这个角色所拥有的所有菜单的权限，注意不是找到这个角色选择了哪些菜单
     * @param roleId
     * @return
     */
    List<AccessMenu> selectMenusByRoleId(Integer roleId);

    List<AccessMenu> selectMenusByUserId(Integer userId);

    /**
     * 通过角色id这个角色选择了哪些菜单
     * @param roleId
     * @return
     */
    List<AccessMenu> selectCheckedMenusByRoleId(Integer roleId);

    List<AccessMenu> getChild(Integer id, List<AccessMenu> allMenus);

    List<AccessMenu> selectByUrl(List<String> urls);

    /**
     *
     * @param tenantName
     */
    void initMenus(String tenantName);
}
