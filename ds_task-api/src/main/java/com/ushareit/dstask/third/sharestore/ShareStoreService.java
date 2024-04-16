package com.ushareit.dstask.third.sharestore;

/**
 * @author fengxiao
 * @date 2023/2/17
 */
public interface ShareStoreService {

    /**
     * 获取目标 segment 的分片数
     *
     * @param restEndpoint ShareStore 的连接地址
     * @param cluster      ShareStore 的集群名称
     * @param segment      ShareStore 表名
     * @return 分片数
     */
    int getPartitionNum(String restEndpoint, String cluster, String segment);

    /**
     * 校验 segment 是否存在
     *
     * @param restEndpoint ShareStore 的连接地址
     * @param cluster      ShareStore 的集群名称
     * @param segment      ShareStore 表名
     * @return true - 存在，false - 不存在
     */
    boolean segmentExist(String restEndpoint, String cluster, String segment);

}
