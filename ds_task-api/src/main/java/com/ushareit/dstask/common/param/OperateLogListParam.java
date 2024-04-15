package com.ushareit.dstask.common.param;

import com.ushareit.dstask.bean.OperateLog;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.format.annotation.DateTimeFormat;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;

/**
 * @author fengxiao
 * @date 2021/8/9
 */
@Data
public class OperateLogListParam {

    @ApiModelProperty("请求ID")
    private String traceId;

    @ApiModelProperty("请求方法")
    private String type;

    @ApiModelProperty("用户ID")
    private String userName;

    @ApiModelProperty("参数")
    private String params;

    @ApiModelProperty("请求路径")
    private String uri;

    @ApiModelProperty("返回码")
    private String resultCode;

    @ApiModelProperty("错误信息")
    private String resultMessage;

    @ApiModelProperty("请求开始时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date requestStartTime;

    @ApiModelProperty("请求结束时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date requestEndTime;

    public Example toExample() {
        Example example = new Example(OperateLog.class);
        Example.Criteria criteria = example.or();

        if (StringUtils.isNotBlank(params)) {
            criteria.andLike("params", "%" + params + "%");
        }

        if (StringUtils.isNotBlank(uri)) {
            criteria.andLike("uri", "%" + uri + "%");
        }

        if (StringUtils.isNotBlank(resultMessage)) {
            criteria.andLike("resultMessage", "%" + resultMessage + "%");
        }

        if (StringUtils.isNotBlank(resultCode)) {
            criteria.andEqualTo("resultCode", resultCode);
        }

        if (requestStartTime != null) {
            criteria.andGreaterThanOrEqualTo("requestTime", requestStartTime);
        }

        if (requestEndTime != null) {
            criteria.andLessThanOrEqualTo("requestTime", requestEndTime);
        }

        if (StringUtils.isNotBlank(traceId)) {
            criteria.andEqualTo("traceId", traceId);
        }

        if (StringUtils.isNotBlank(type)) {
            criteria.andEqualTo("type", type);
        }

        if (StringUtils.isNotBlank(userName)) {
            criteria.andEqualTo("userName", userName);
        }

        example.setOrderByClause("id desc");
        return example;
    }
}
