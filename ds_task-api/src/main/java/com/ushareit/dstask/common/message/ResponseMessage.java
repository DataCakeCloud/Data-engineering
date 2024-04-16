package com.ushareit.dstask.common.message;

import com.alibaba.fastjson.JSON;
import lombok.Data;

import java.io.Serializable;

/**
 * @author fengxiao
 * @date 2022/11/24
 */
@Data
public class ResponseMessage implements Serializable {
    private static final long serialVersionUID = 8408583369796724102L;

    private int code;
    private String message;
    private MessageCard data;

    public static String success() {
        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setCode(0);
        responseMessage.setMessage("成功");
        return JSON.toJSONString(responseMessage);
    }

    public static String success(MessageCard data) {
        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setCode(0);
        responseMessage.setData(data);
        return JSON.toJSONString(responseMessage);
    }

    public static String error(String errorMessage) {
        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setCode(-1);
        responseMessage.setMessage(errorMessage);
        return JSON.toJSONString(responseMessage);
    }
}
