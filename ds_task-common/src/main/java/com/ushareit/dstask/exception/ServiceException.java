package com.ushareit.dstask.exception;

import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.web.vo.BaseResponse;

/**
 * @author swq
 */
public class ServiceException extends RuntimeException {
    private static final long serialVersionUID = -7828337362960040358L;
    private Integer code = 500;
    private String codeStr;

    private Object data;

    public ServiceException(String codeStr, String message) {
        super(message);
        this.codeStr = codeStr;
    }
    public ServiceException(Integer code, String codeStr, String message) {
        super(message);
        this.code = code;
        this.codeStr = codeStr;
    }

    public ServiceException(Integer code, String codeStr, String message, Object data) {
        super(message);
        this.code = code;
        this.codeStr = codeStr;
        this.data = data;
    }

    public ServiceException(String message, Throwable throwable) {
        super(message + ", " + throwable.getMessage(), throwable);
    }

    public ServiceException(BaseResponseCodeEnum serviceErrorCodeEnum, Throwable throwable) {
        super(serviceErrorCodeEnum.getMessage(), throwable);
    }

    public ServiceException(BaseResponseCodeEnum serviceErrorCodeEnum) {
        this(serviceErrorCodeEnum.getCode(), serviceErrorCodeEnum.name(), serviceErrorCodeEnum.getMessage());
    }

    public ServiceException(BaseResponseCodeEnum serviceErrorCodeEnum, String message) {
        this(serviceErrorCodeEnum.getCode(), serviceErrorCodeEnum.name(), serviceErrorCodeEnum.getMessage() + ":" + message);
    }

    public ServiceException(BaseResponseCodeEnum serviceErrorCodeEnum, String message, Object o) {
        this(serviceErrorCodeEnum.getCode(), serviceErrorCodeEnum.name(), message, o);
    }

    public ServiceException(BaseResponse baseResponse) {
        this(baseResponse.getCode(), baseResponse.getCodeStr(), baseResponse.getMessage(), baseResponse.getData());
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getCodeStr() {
        return codeStr;
    }

    public void setCodeStr(String codeStr) {
        this.codeStr = codeStr;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
