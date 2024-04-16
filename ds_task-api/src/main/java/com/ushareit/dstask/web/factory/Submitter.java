package com.ushareit.dstask.web.factory;

import com.ushareit.dstask.web.vo.BaseResponse;

import java.util.Map;

/**
 * the interface is to submit task with autoscale or non-autoscale
 * @author wuyan
 * @date 2021/12/8
 */
public interface Submitter {
    /**
     * async submit flink task
     *
     * @param
     * @param
     * @param
     * @return
     */
    BaseResponse submitAsync() throws Exception;

    /**
     * offlineSubmitter专用
     * @return
     */
    Map<String, String> update();

    void processException() throws Exception;

    String getFlinkUiDns(String appName, String region);

    void deleteResource() throws Exception;

    void autoScaleTm(Integer count);

    Integer getTmNum();
}
