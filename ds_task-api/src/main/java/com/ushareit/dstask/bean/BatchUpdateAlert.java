package com.ushareit.dstask.bean;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

/**
 * @auther tiyongshuai
 * @data 2024/4/1
 * @description
 */
@Data
public class BatchUpdateAlert {

    Integer[] ids;
    String regularAlert;
    String alertModel;

}
