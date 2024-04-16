package com.ushareit.dstask.common.param;

import com.ushareit.dstask.bean.DeleteEntity;
import com.ushareit.dstask.bean.WorkflowVersion;
import lombok.Data;
import tk.mybatis.mapper.entity.Example;

import javax.validation.constraints.NotNull;

/**
 * @author fengxiao
 * @date 2022/11/17
 */
@Data
public class WorkflowVersionSearchParam {

    @NotNull(message = "分页值不能为空")
    private Integer pageNo;

    @NotNull(message = "分页大小不能为空")
    private Integer pageSize;

    @NotNull(message = "工作流ID不能为空")
    private Integer workflowId;

    public Example toExample() {
        Example example = new Example(WorkflowVersion.class);
        example.or()
                .andEqualTo("workflowId", workflowId)
                .andEqualTo("deleteStatus", DeleteEntity.NOT_DELETE);
        example.setOrderByClause("version desc");
        return example;
    }
}
