package com.ushareit.dstask.common.message;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * @author fengxiao
 * @date 2022/11/21
 */
@Data
public class MessageCard implements Serializable {
    private static final long serialVersionUID = 4199585451454508802L;

    @NotBlank(message = "消息类别不能为空")
    private String type;
    private Object data;

    public <DATA> DATA parseToObject(Class<DATA> dataClass) {
        if (data == null) {
            return null;
        }

        return JSONObject.parseObject(data.toString(), dataClass);
    }

    public <DATA> List<DATA> parseToList(Class<DATA> dataClass) {
        if (data == null) {
            return Collections.emptyList();
        }

        return JSON.parseArray(data.toString(), dataClass);
    }
}
