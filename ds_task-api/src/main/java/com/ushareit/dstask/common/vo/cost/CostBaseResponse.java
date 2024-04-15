package com.ushareit.dstask.common.vo.cost;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ushareit.dstask.common.vo.cost.CostTotalVo;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author swq
 * @date 2018/3/16
 */
@Slf4j
@Data
public class CostBaseResponse<T> {
    private Integer code = 0;
    /**
     * 元数据接口返回数据
     */
    public static final String RESPONSE_SUCCESS_CODE = "0";

    /**
     * 错误码
     */
    private String codeStr = BaseResponseCodeEnum.SUCCESS.name();
    /**
     * 消息
     */
    private String message;
    /**
     * 响应内容
     */
    private T data;

    private CostTotalVo costTotalVo;

    public CostBaseResponse() {
    }

    private CostBaseResponse(T data) {
        this.data = data;
    }

    private CostBaseResponse(String codeStr, String message, T data) {
        if (!BaseResponseCodeEnum.SUCCESS.name().equals(codeStr)){
            if (RESPONSE_SUCCESS_CODE.equals(codeStr)) {
                // 适配元数据接口成功时code返回0, message返回为"成功"
                this.code = 200;
                this.codeStr = BaseResponseCodeEnum.SUCCESS.name();
            }else {
                if (codeStr.contains(BaseResponseCodeEnum.WARN.name())) {
                    this.code = 502;
                }else if (codeStr.contains(BaseResponseCodeEnum.NO_LOGIN.name())) {
                    this.code = 403;
                }else {
                    this.code = 500;
                }
                this.codeStr = BaseResponseCodeEnum.SYS_ERR.name();
            }
        }
        this.message = message;
        this.data = data;
    }

    public static <T> CostBaseResponse<T> success() {
        return new CostBaseResponse<>();
    }

    public static <T> CostBaseResponse<T> success(T data) {
        return new CostBaseResponse<>(data);
    }

    public static <T> CostBaseResponse<T> success(BaseResponseCodeEnum responseCodeEnum, T data) {
        return getInstance(responseCodeEnum.name(), responseCodeEnum.getMessage(),data);
    }

    public static <T> CostBaseResponse<T> error(BaseResponseCodeEnum responseCodeEnum) {
        return getInstance(responseCodeEnum.name(), responseCodeEnum.getMessage());
    }

    public static <T> CostBaseResponse<T> error(BaseResponseCodeEnum responseCodeEnum, T data) {
        return getInstance(responseCodeEnum.name(), responseCodeEnum.getMessage(), data);
    }

    public static <T> CostBaseResponse<T> error(String code, String message) {
        return getInstance(code, message);
    }
    public static <T> CostBaseResponse<T> error(String code, String message,T data) {
        return getInstance(code, message,data);
    }

    public static <T> CostBaseResponse<T> getInstance(String code, String message) {
        return getInstance(code, message, null);
    }

    public static <T> CostBaseResponse<T> getInstance(String code, String message, T data) {
        return new CostBaseResponse<>(code, message, data);
    }


    /**
     * 解决cannot evaluate BaseResponse.toString()的exception
     * @return
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);

    }

    public JSONObject get(){
        return JSON.parseObject(data.toString());
    }
}