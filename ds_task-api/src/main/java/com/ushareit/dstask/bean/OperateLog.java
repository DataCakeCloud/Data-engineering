package com.ushareit.dstask.bean;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import java.util.Date;

/**
 * @author fengxiao
 * @date 2021/8/9
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "operate_log")
public class OperateLog extends BaseEntity {
    private static final long serialVersionUID = 5522466138912999153L;

    @ApiModelProperty(value = "用户ID")
    private String userName;

    @ApiModelProperty(value = "来源")
    private String source;

    @ApiModelProperty(value = "日志ID")
    private String traceId;

    @ApiModelProperty(value = "操作类型")
    private String type;

    @ApiModelProperty(value = "请求路径")
    private String uri;

    @ApiModelProperty(value = "请求参数")
    private String params;

    @ApiModelProperty(value = "响应状态码")
    private String resultCode;

    @ApiModelProperty(value = "响应码不为0时，错误信息")
    private String resultMessage;

    @ApiModelProperty(value = "响应码为0时，返回结果")
    private String resultData;

    @ApiModelProperty(value = "响应耗时，单位毫秒")
    private Long costTime;

    @ApiModelProperty(value = "请求时间")
    private Date requestTime;

    @ApiModelProperty(value = "返回时间")
    private Date responseTime;

}
