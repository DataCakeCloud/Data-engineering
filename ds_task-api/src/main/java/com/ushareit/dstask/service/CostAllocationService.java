package com.ushareit.dstask.service;


import com.ushareit.dstask.bean.AccessGroup;
import com.ushareit.dstask.bean.CostAllocation;
import com.ushareit.dstask.bean.Task;

import java.util.List;


/**
 * @author xuebotao
 * @date 2022/12/2
 */
public interface CostAllocationService extends BaseService<CostAllocation> {

    void updateCostAllocation(Task task);

    List<CostAllocation> selectByTaskId(Integer taskId);

}
