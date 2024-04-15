package com.ushareit.dstask.service;

import com.ushareit.dstask.bean.Label;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/**
 * @author wuyan
 * @date 2021/9/22
 */
public interface LabelService extends BaseService<Label> {

    Map<String, List<Label>> list(Label label);

    Label checkExist(Integer id);

    Label getLabel(Integer id);

    List<Label> getLabels(List<Integer> ids);

    Integer count(Timestamp time);

    /**
     * 获取默认创建或者关注的 label 列表
     *
     * @param username 用户
     * @return label 集合
     */
    List<Label> getList(String username);
}
