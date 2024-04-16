package com.ushareit.dstask.web.vo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author swq
 * @date 2018/3/16
 */
@Slf4j
@Data
public class BaseResponse<T> {
    public static final Integer RESPONSE_FAILED_CODE = 500;

    private Integer code;
    private String codeStr;
    private String message;
    private Boolean msgIngore;
    private String traceId = InfTraceContextHolder.get().getTraceId();

    /**
     * 响应内容
     */
    private T data;

    public BaseResponse() {
        this.code = BaseResponseCodeEnum.SUCCESS.getCode();
        this.codeStr = BaseResponseCodeEnum.SUCCESS.name();
        this.message = BaseResponseCodeEnum.SUCCESS.getMessage();
    }

    public BaseResponse(BaseResponseCodeEnum responseCodeEnum) {
        this.code = responseCodeEnum.getCode();
        this.codeStr = responseCodeEnum.name();
        this.message = responseCodeEnum.getMessage();
    }

    private BaseResponse(BaseResponseCodeEnum responseCodeEnum, T data) {
        this.code = responseCodeEnum.getCode();
        this.codeStr = responseCodeEnum.name();
        this.message = responseCodeEnum.getMessage();
        this.data = data;
    }

    public BaseResponse(Integer code, String codeStr, String message, String suggest, Boolean msgIngore, T data) {
        this.code = code == null ? RESPONSE_FAILED_CODE : code;
        this.codeStr = codeStr;
        this.message = message;
        this.data = data == null ? (T)suggest : data;
        this.msgIngore = msgIngore;
    }

    public static <T> BaseResponse<T> success() {
        return new BaseResponse<>(BaseResponseCodeEnum.SUCCESS);
    }

    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(BaseResponseCodeEnum.SUCCESS, data);
    }

    public static <T> BaseResponse<T> success(BaseResponseCodeEnum responseCodeEnum, T data) {
        return getInstance(responseCodeEnum, data);
    }

    public static <T> BaseResponse<T> error(BaseResponseCodeEnum responseCodeEnum) {
        return getInstance(responseCodeEnum);
    }

    public static <T> BaseResponse<T> error(BaseResponseCodeEnum responseCodeEnum, T data) {
        return getInstance(responseCodeEnum, data);
    }

    public static <T> BaseResponse<T> error(String codeStr, String message) {
        return getInstance(RESPONSE_FAILED_CODE, codeStr, message, null);
    }

    public static <T> BaseResponse<T> error(String codeStr, String message, T data) {
        return getInstance(RESPONSE_FAILED_CODE,codeStr, message, data);
    }

    public static <T> BaseResponse<T> error(Integer code,String codeStr, String message, T data) {
        return getInstance(code, codeStr, message, data);
    }

    public static <T> BaseResponse<T> getInstance(BaseResponseCodeEnum responseCodeEnum) {
        return getInstance(responseCodeEnum, null);
    }

    public static <T> BaseResponse<T> getInstance(BaseResponseCodeEnum responseCodeEnum, T data) {
        return getInstance(responseCodeEnum.getCode(), responseCodeEnum.name(), responseCodeEnum.getMessage(), responseCodeEnum.getSuggest(), responseCodeEnum.getMsgIgnore(), data);
    }

    public static <T> BaseResponse<T> getInstance(Integer code, String message, T data) {
        return getInstance(code, null, message, null, false, data);
    }

    public static <T> BaseResponse<T> getInstance(Integer code,String codeStr, String message, T data) {
        return getInstance(code, codeStr, message, null, false, data);
    }

    public static <T> BaseResponse<T> getInstance(Integer code, String codeStr, String message, String suggest, Boolean msgIngore, T data) {
        return new BaseResponse<>(code, codeStr, message, suggest, msgIngore, data);
    }


    /**
     * 解决cannot evaluate BaseResponse.toString()的exception
     *
     * @return
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);

    }

    public JSONObject get() {
        return JSON.parseObject(data.toString());
    }
}