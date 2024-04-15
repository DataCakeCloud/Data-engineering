package com.ushareit.dstask.bean;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

@Data
@Table(name = "operate_log")
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@ApiModel("日志审计")
public class ApiGateway extends BaseEntity {

    @ApiModelProperty(value = "请求用户")
    @Column(name = "user_name")
    private String userName;

    @ApiModelProperty(value = "请求模块")
    @Column(name = "source")
    private String source;

    @ApiModelProperty(value = "请求API")
    @Column(name = "uri")
    private String uri;

    @ApiModelProperty(value = "请求时间")
    @Column(name = "request_time")
    private String requestTime;

    @ApiModelProperty(value = "响应时间")
    @Column(name = "response_time")
    private String responseTime;

    @ApiModelProperty(value = "请求参数")
    @Column(name = "params")
    private String params;

    @ApiModelProperty(value = "响应耗时")
    @Column(name = "cost_time")
    private String costTime;

    @ApiModelProperty(value = "返回状态码")
    @Column(name = "result_code")
    private BigInteger resultCode;

    @ApiModelProperty(value = "返回异常信息")
    @Column(name = "result_message")
    private String resultMessage;

    @Transient
    private String eventName;

    @Transient
    private List<String> eventUri;

    @Transient
    private Long startTime;

    @Transient
    private Long endTime;

    @Transient
    private String startTimeFormat;

    @Transient
    private String endTimeFormat;

    @Transient
    private Integer pageNum = 1;

    @Transient
    private Integer pageSize = 50;

    @Transient
    private Map<String, Object> param;

//    @ApiModelProperty(value = "返回数据")
//    @Column(name = "result_data")
//    private String resultData;
}
