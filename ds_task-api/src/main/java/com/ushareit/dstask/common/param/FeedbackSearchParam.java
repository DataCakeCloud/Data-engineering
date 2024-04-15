package com.ushareit.dstask.common.param;

import com.ushareit.dstask.bean.Feedback;
import com.ushareit.dstask.constant.DsTaskConstant;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import com.ushareit.dstask.web.utils.DataCakeConfigUtil;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.format.annotation.DateTimeFormat;
import tk.mybatis.mapper.entity.Example;

import javax.persistence.Column;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * @author fengxiao
 * @date 2022/3/10
 */
@Data
public class FeedbackSearchParam {

    @ApiModelProperty("工单ID")
    private Integer feedbackId;

    @ApiModelProperty("租户ID")
    private Integer tenantId;

    @ApiModelProperty("问题模块")
    private String module;

    @ApiModelProperty("创建人")
    private String createBy;

    @ApiModelProperty("工单提交时间，开始时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date createStartTime;

    @ApiModelProperty("工单提交时间，结束时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date createEndTime;

    @ApiModelProperty(value = "工单状态", allowableValues = "UN_ACCEPT, ACCEPTED, SOLVED, SCORED")
    private String status;

    private String appName;

    @ApiModelProperty(value = "问题类别")
    private String type;

    @ApiModelProperty(value = "处理人")
    private String handleBy;

    @ApiModelProperty(value = "任务id")
    private String taskId;

    public Example toExample() {

        Integer tenantId = InfTraceContextHolder.get().getTenantId();
        String tenantName = InfTraceContextHolder.get().getTenantName();

        Example example = new Example(Feedback.class);
        Example.Criteria criteria = example.or();

        if (feedbackId != null) {
            criteria.andEqualTo("id", feedbackId);
        }

        if (!tenantName.equals(DataCakeConfigUtil.getDataCakeSourceConfig().getSuperTenant())) {
            criteria.andEqualTo("tenantId", tenantId);
        }

        if (StringUtils.isNotBlank(appName)) {
            criteria.andEqualTo("appName", appName);

//            if (StringUtils.equalsIgnoreCase(appName, "ds-work") && !InfTraceContextHolder.get().getAdmin()) {
//                criteria.andEqualTo("createBy", InfTraceContextHolder.get().getUserName());
//            }
        }

        if (createStartTime != null) {
            criteria.andGreaterThanOrEqualTo("createTime", createStartTime);
        }

        if (createEndTime != null) {
            Calendar c = Calendar.getInstance();
            c.setTime(createEndTime);
            c.add(Calendar.DATE, 1);
            createEndTime = c.getTime();
            criteria.andLessThan("createTime", createEndTime);
        }

        if (type != null) {
            criteria.andLike("type", "%" + type + "%");
        }
        if (module != null) {
            criteria.andLike("module", "%" + module + "%");
        }
        if (StringUtils.isNotEmpty(createBy)) {
            criteria.andLike("createBy", "%" + createBy + "%");
        }

        if (StringUtils.isNotBlank(status)) {
            criteria.andEqualTo("status", status);
        }

        if (StringUtils.isNotBlank(handleBy)) {
            criteria.andEqualTo("handleBy", handleBy);
        }
        example.setOrderByClause(" FIELD(status,\"UN_ACCEPT\",\"ACCEPTED\",\"SOLVED\", \"UN_SCORE\",\"SCORED\") ,id DESC ");
        return example;
    }
}
