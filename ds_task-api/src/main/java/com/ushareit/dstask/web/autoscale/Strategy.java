package com.ushareit.dstask.web.autoscale;


/**
 * 策略接口
 * author xuebotao
 * date 2022-02-10
 */
public interface Strategy {
    /**
     * 获取本策略最大并行度
     *
     * @return
     * @throws Exception
     */
    Integer getMaxPar() throws Exception;

    /**
     * 获取本策略最小并行度
     *
     * @return
     * @throws Exception
     */
    Integer getMinPar() throws Exception;

    /**
     * 获取本当前并行度
     *
     * @return
     * @throws Exception
     */
    Integer getCurrentPar() throws Exception;

    /**
     * 获取本策略建议并行度
     *
     * @return
     * @throws Exception
     */
    Integer getAdvicePar() throws Exception;

    /**
     * 获取本策略建议并行度
     *
     * @return
     * @throws Exception
     */
    Integer getTargetPar() throws Exception;

    /**
     * 执行本策略
     *
     * @return
     * @throws Exception
     */
    void doStrategy() throws Exception;

    /**
     * 策略之后操作
     *
     * @return
     * @throws Exception
     */
    void execute() throws Exception;


}
