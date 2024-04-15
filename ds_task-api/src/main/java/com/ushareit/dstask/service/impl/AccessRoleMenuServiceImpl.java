package com.ushareit.dstask.service.impl;

import com.ushareit.dstask.bean.AccessRole;
import com.ushareit.dstask.bean.AccessRoleMenu;
import com.ushareit.dstask.mapper.AccessRoleMapper;
import com.ushareit.dstask.mapper.AccessRoleMenuMapper;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import com.ushareit.dstask.service.AccessRoleMenuService;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import com.ushareit.dstask.web.utils.DataCakeConfigUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author wuyan
 * @date 2022/4/12
 */
@Slf4j
@Service
public class AccessRoleMenuServiceImpl extends AbstractBaseServiceImpl<AccessRoleMenu> implements AccessRoleMenuService {

    @Resource
    private AccessRoleMenuMapper accessRoleMenuMapper;
    @Resource
    private AccessRoleMapper accessRoleMapper;


    @Override
    public CrudMapper<AccessRoleMenu> getBaseMapper() {
        return accessRoleMenuMapper;
    }

    @Override
    public void deleteByRoleId(Integer roleId) {
        accessRoleMenuMapper.deleteByRoleId(roleId);
    }

    @Override
    public List<AccessRoleMenu> selectByRoleId(Integer roleId) {
        return accessRoleMenuMapper.selectByRoleId(roleId);
    }

    @Override
    public void initRoleMenus(String tenantName, Collection<AccessRole> roleList) {
        if (CollectionUtils.isEmpty(roleList)) {
            return;
        }

        InfTraceContextHolder.get().setTenantName(DataCakeConfigUtil.getDataCakeSourceConfig().getSuperTenant());
        Example example = new Example(AccessRoleMenu.class);
        example.or()
                .andIn("roleId", roleList.stream().map(AccessRole::getId).collect(Collectors.toList()));
        List<AccessRoleMenu> accessRoleMenuList = accessRoleMenuMapper.selectByExample(example);

        InfTraceContextHolder.get().setTenantName(tenantName);
        accessRoleMenuList.stream().peek(item -> {
            item.setCreateBy("admin");
            item.setUpdateBy("admin");
            item.setCreateTime(new Timestamp(System.currentTimeMillis()));
            item.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        }).forEach(accessRoleMenuMapper::insertSelective);
    }
}
