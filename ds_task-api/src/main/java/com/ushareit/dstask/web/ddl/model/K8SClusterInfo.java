package com.ushareit.dstask.web.ddl.model;

import lombok.Data;

/**
 * Created by caozhen on 2019/9/19
 */
@Data
public class K8SClusterInfo {

    private String host;
    private String caCrt;
    private String token;

}
