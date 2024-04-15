package com.ushareit.dstask.service.impl;


import com.ushareit.dstask.bean.CurrentUser;
import com.ushareit.dstask.bean.DeleteEntity;
import com.ushareit.dstask.bean.WechatAlert;
import com.ushareit.dstask.bean.Workflow;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.mapper.WechatAlertMapper;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import com.ushareit.dstask.service.WechatAlertService;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.List;

@Slf4j
@Service
public class WeChatAlertServiceImpl extends AbstractBaseServiceImpl<WechatAlert> implements WechatAlertService {

    @Resource
    private WechatAlertMapper wechatAlertMapper;

    @Override
    public CrudMapper<WechatAlert> getBaseMapper() {
        return wechatAlertMapper;
    }

    @Override
    public void addWechat(WechatAlert wechatAlert, CurrentUser currentUser) {
        InfTraceContextHolder.get().setTenantName(currentUser.getTenantName());
        InfTraceContextHolder.get().setTenantId(currentUser.getTenantId());

        WechatAlert old = wechatAlertMapper.selectByNameUuid(wechatAlert);
        if (old != null) {
            throw new ServiceException(BaseResponseCodeEnum.WECHAT_REPEAT);
        }
        wechatAlert.setCreateBy(currentUser.getUserName());
        wechatAlert.setUpdateBy(currentUser.getUserName());
        Timestamp now = new Timestamp(System.currentTimeMillis());
        wechatAlert.setCreateTime(now);
        wechatAlert.setUpdateTime(now);
        Integer groupId = Integer.valueOf(InfTraceContextHolder.get().getGroupId());
        String currentGroup = InfTraceContextHolder.get().getCurrentGroup();
        wechatAlert.setUserGroupId(groupId);
        wechatAlert.setUserGroupName(currentGroup);
        wechatAlertMapper.insert(wechatAlert);
    }

    @Override
    public void updateWechat(WechatAlert wechatAlert, CurrentUser currentUser) {
        InfTraceContextHolder.get().setTenantName(currentUser.getTenantName());
        InfTraceContextHolder.get().setTenantId(currentUser.getTenantId());

        WechatAlert old = wechatAlertMapper.selectByPrimaryKey(wechatAlert);
        if (old == null) {
            throw new ServiceException(BaseResponseCodeEnum.WECHAT_NO_EXIST);
        }

        WechatAlert alert = wechatAlertMapper.selectByNameUuid(wechatAlert);
        if (alert != null && alert.getName().equals(wechatAlert.getName())) {
            throw new ServiceException(BaseResponseCodeEnum.WECHAT_REPEAT);
        }

        wechatAlert.setUpdateBy(currentUser.getUserName());
        wechatAlertMapper.updateByPrimaryKeySelective(wechatAlert);
    }

    @Override
    public void deleteWechat(Integer id, CurrentUser currentUser) {
        InfTraceContextHolder.get().setTenantName(currentUser.getTenantName());
        InfTraceContextHolder.get().setTenantId(currentUser.getTenantId());

        WechatAlert wechatAlert = wechatAlertMapper.selectByPrimaryKey(id);
        if (wechatAlert == null) {
            return;
        }
        wechatAlert.setDeleteStatus(DeleteEntity.DELETE);
        wechatAlert.setUpdateBy(currentUser.getUserName());
        wechatAlertMapper.updateByPrimaryKeySelective(wechatAlert);
    }

    @Override
    public List<WechatAlert> getWechat(WechatAlert wechatAlert, CurrentUser currentUser) {
        InfTraceContextHolder.get().setTenantName(currentUser.getTenantName());
        InfTraceContextHolder.get().setTenantId(currentUser.getTenantId());
        return wechatAlertMapper.selectByConditions(wechatAlert);
    }

    @Override
    public List<WechatAlert> selectTockenByUserGroupId(Integer id) {
        Example example = new Example(WechatAlert.class);
        example.and()
                .andEqualTo("userGroupId", id)
                .andEqualTo("deleteStatus", DeleteEntity.NOT_DELETE);
        List<WechatAlert> wechatAlerts = wechatAlertMapper.selectByExample(example);
        return wechatAlerts;
    }

    @Override
    public List<WechatAlert> selectTockenByUserGroupName(String name) {
        Example example = new Example(Workflow.class);
        example.and()
                .andEqualTo("userGroupName", name)
                .andEqualTo("deleteStatus", DeleteEntity.NOT_DELETE);
        List<WechatAlert> wechatAlerts = wechatAlertMapper.selectByExample(example);
        return wechatAlerts;
    }
}
