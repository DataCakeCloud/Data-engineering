package com.ushareit.dstask.common.param;

import com.ushareit.dstask.bean.AccessGroup;
import com.ushareit.dstask.bean.DeleteEntity;
import com.ushareit.dstask.bean.Workflow;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import tk.mybatis.mapper.entity.Example;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.Arrays;
import java.util.List;

/**
 * @author fengxiao
 * @date 2022/11/17
 */
@Data
public class WorkflowSearchParam {

    @NotNull(message = "分页值不能为空")
    private Integer pageNo;

    @NotNull(message = "分页大小不能为空")
    private Integer pageSize;

    private String keyword;

    @Pattern(regexp = "name|id|updateTime|granularity|status")
    private String sortKey;

    @Pattern(regexp = "asc|desc")
    private String sortValue;

    private String workflows;//工作流id

    private Integer status;//状态

    private String owner;

    private Boolean comefromLabel;

    private Boolean onlyMine = Boolean.FALSE;

    public Example toExample(List<AccessGroup> groupList) {
        Example example = new Example(Workflow.class);
        Example.Criteria idOrNameCriteria = example.and();
        if (StringUtils.isNotBlank(keyword)) {
            idOrNameCriteria.orLike("name", "%" + keyword + "%");
            if (comefromLabel!=null&&comefromLabel==true){
                idOrNameCriteria.orLike("owner", "%" + keyword + "%");
            }
        }

        if (NumberUtils.isDigits(keyword)) {
            idOrNameCriteria.orEqualTo("id", Integer.parseInt(keyword));
        }

        if (onlyMine) {
            Example.Criteria ownerOrCollaboratorOrGroup = example.and();

            ownerOrCollaboratorOrGroup.orEqualTo("owner", InfTraceContextHolder.get().getUserName());
            ownerOrCollaboratorOrGroup.orCondition(String.format("find_in_set('%s', collaborators)",
                    InfTraceContextHolder.get().getUserName()));
            groupList.stream().map(AccessGroup::getId).distinct().forEach(item -> ownerOrCollaboratorOrGroup
                    .orCondition(String.format("find_in_set('%s', user_group)", item)));
        }

        Example.Criteria commonCriteria = example.and();
        commonCriteria.andEqualTo("deleteStatus", DeleteEntity.NOT_DELETE);

        if (StringUtils.isNotBlank(sortKey) && StringUtils.isNotBlank(sortValue)) {
            if (StringUtils.equalsIgnoreCase(sortKey, "updateTime")) {
                sortKey = "update_time";
            }

            example.setOrderByClause(String.format("%s %s", sortKey, sortValue));
        } else {
            example.setOrderByClause("update_time desc");
        }

        if (StringUtils.isNoneBlank(workflows)){
            Example.Criteria workflowsExample=example.and();
            workflowsExample.andIn("id",Arrays.asList(workflows.split(",")));
        }
        if (StringUtils.isNoneBlank(owner)){
            Example.Criteria ownerExample=example.and();
            ownerExample.andEqualTo("owner",owner);
        }
        if (status!=null){
            Example.Criteria statusExample=example.and();
            statusExample.andEqualTo("status",status);
        }
        return example;
    }

}
