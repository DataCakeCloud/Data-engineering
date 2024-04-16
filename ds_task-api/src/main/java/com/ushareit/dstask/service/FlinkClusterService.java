package com.ushareit.dstask.service;

import com.ushareit.dstask.bean.FlinkCluster;

import java.util.List;

/**
 * @author: licg
 * @create: 2021-05-12 15:24
 */
public interface FlinkClusterService extends BaseService<FlinkCluster> {
    List<FlinkCluster> listAutoScaleClusters();

    List<FlinkCluster> listNonAutoScaleClusters();

}
