package com.ushareit.dstask.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;


@AllArgsConstructor
@NoArgsConstructor
@Data
public class WxMessageBean {

    public String msgtype = "text";

    public Map<String, Object> text;

}
