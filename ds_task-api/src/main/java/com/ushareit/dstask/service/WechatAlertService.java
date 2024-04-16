package com.ushareit.dstask.service;


import com.ushareit.dstask.bean.CurrentUser;
import com.ushareit.dstask.bean.WechatAlert;
import io.swagger.models.auth.In;

import java.util.List;

public interface WechatAlertService extends BaseService<WechatAlert> {

    void addWechat(WechatAlert wechatAlert, CurrentUser currentUser);
    void updateWechat(WechatAlert wechatAlert, CurrentUser currentUser);
    void deleteWechat(Integer id, CurrentUser currentUser);
    List<WechatAlert> getWechat(WechatAlert wechatAlert, CurrentUser currentUser);
    List<WechatAlert> selectTockenByUserGroupId(Integer id);

    List<WechatAlert> selectTockenByUserGroupName(String name);
}
