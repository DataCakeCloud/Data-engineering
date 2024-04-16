package com.ushareit.dstask.validator;

import com.ushareit.dstask.bean.Task;
import com.ushareit.dstask.constant.TemplateEnum;
import com.ushareit.dstask.validator.vo.ValidTypeVO;
import com.ushareit.dstask.validator.vo.ValidateResult;

import java.util.List;

/**
 * @author fengxiao
 * @date 2023/1/31
 */
public interface ValidatorService {

    /**
     * 根据模板类型获取校验项列表
     *
     * @param templateEnum 模板类型
     * @return 校验项列表
     */
    List<ValidTypeVO> getValidList(TemplateEnum templateEnum);

    /**
     * 校验特定类型的任务参数
     *
     * @param validType 校验类型
     * @param task      任务详情信息
     * @return 校验结果
     */
    ValidateResult validate(ValidType validType, Task task);

    /**
     * 校验任务的参数项
     *
     * @param task 任务信息
     */
    void validTask(Task task);

}
