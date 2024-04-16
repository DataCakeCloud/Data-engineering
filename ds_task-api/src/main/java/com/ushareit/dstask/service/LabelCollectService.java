package com.ushareit.dstask.service;

import com.ushareit.dstask.bean.LabelCollect;

import java.util.Optional;

/**
 * @author wuyan
 * @date 2022/1/19
 */
public interface LabelCollectService extends BaseService<LabelCollect> {

    LabelCollect collect(Integer labelId);

    void cancel(Integer labelId);

    LabelCollect get();

    /**
     * 获取用户的标签收藏集合
     *
     * @param username 用户
     * @return 标签收藏信息
     */
    Optional<LabelCollect> getLabelCollect(String username);
}
