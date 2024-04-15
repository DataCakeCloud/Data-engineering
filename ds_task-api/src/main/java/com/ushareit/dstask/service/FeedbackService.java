package com.ushareit.dstask.service;

import com.github.pagehelper.PageInfo;
import com.ushareit.dstask.bean.Feedback;
import com.ushareit.dstask.bean.FeedbackProcessItem;
import com.ushareit.dstask.bean.meta.TaskUsage;
import com.ushareit.dstask.common.param.FeedbackSearchParam;
import com.ushareit.dstask.common.vo.FeedbackVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import tk.mybatis.mapper.entity.Example;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;

/**
 * @author fengxiao
 * @date 2021/10/8
 */
public interface FeedbackService extends BaseService<Feedback> {

    /**
     * 受理工单
     *
     * @param feedbackId 工单ID
     */
    void accept(Integer feedbackId, String source, String accept);

    /**
     * 转让工单
     *
     * @param feedbackId 工单ID
     * @param assignee   受让人
     */
    void assign(Integer feedbackId, String assignee, String reason, String module);

    /**
     * 修改解决方案
     *
     * @param feedbackId 工单ID
     * @param reason     解决方案
     */
    void modify(Integer feedbackId, String reason);

    /**
     * 关闭工单
     *
     * @param feedbackId 工单ID
     * @param reason     关闭原因 / 工单解决方案
     */
    void close(Integer feedbackId, String reason);

    /**
     * 重开工单
     *
     * @param param 工单ID
     */
    Feedback reopen(FeedbackProcessItem param);

    /**
     * 修改工单
     *
     * @param param 工单ID
     */
    Feedback updateAndSelect(Feedback param);

    /**
     * 给工单评分
     *
     * @param feedbackId 工单ID
     * @param score      评分
     */
    void score(Integer feedbackId, int score);

    PageInfo<FeedbackVO> page(Integer pageNum, Integer pageSize, Example param);

    void export(FeedbackSearchParam param, HttpServletResponse response);

    void monitorFeedbackTimeout();
    
    List<TaskUsage> selectDayFeedbacks(String create, String start, String end);
}
