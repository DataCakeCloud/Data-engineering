package com.ushareit.dstask.service;

import com.ushareit.dstask.bean.Feedback;
import com.ushareit.dstask.bean.FeedbackProcessItem;

import java.util.List;

/**
 * @author fengxiao
 * @date 2022/3/8
 */
public interface FeedbackProcessItemService extends BaseService<FeedbackProcessItem> {

    /**
     * 添加反馈处理条目
     *
     * @param feedbackId 反馈（工单）ID
     * @param message    处理信息
     * @param operator   操作人
     */
    void saveItem(Integer feedbackId, String message, String operator, String attachIds);


    void saveItem(Integer feedbackId, String message, String operator);

    /**
     * 获取反馈ID的处理条目列表
     *
     * @param feedbackId 反馈ID
     * @return 处理条目列表
     */
    List<FeedbackProcessItem> getItemList(Integer feedbackId);

    Feedback getDetailInformation(FeedbackProcessItem feedbackProcessItem);

    Feedback messageSend(FeedbackProcessItem feedbackProcessItem);

    List<FeedbackProcessItem> getItemListByFids(List<Integer> feedbackId);

}
