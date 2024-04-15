package com.ushareit.dstask.utils;

import com.alibaba.fastjson.JSON;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.util.JsonFormat;
import lombok.extern.slf4j.Slf4j;

/**
 * @author fengxiao
 * @date 2022/12/4
 */
@Slf4j
public class ProtoUtils {

    public static String print(Object message) {
        if (message == null) {
            return null;
        }

        if (message instanceof MessageOrBuilder) {
            try {
                return JsonFormat.printer().print((MessageOrBuilder) message);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return null;
            }
        }
        return JSON.toJSONString(message);
    }

}
