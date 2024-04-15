package com.ushareit.dstask.third.lakecat.vo;

import lombok.Data;

/**
 * @author fengxiao
 * @date 2023/2/14
 */
@Data
public class LakeCatResponse {

    private Integer code;
    private String errorCode;
    private String message;
    private String data;

}
