package com.ushareit.dstask.web.metadata.airbyte;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * author:xuebotao
 * date:2022-08-12
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DbConfig {

    public String jdbc_url_params;
    public String database;
    public String username;
    public String password;
    public String port;
    public String queryPort;  //doris
    public String httpPort;  //doris
    public String replication_method;
    public String host;
    public String sid;  //oracle
    public List<String> schemas;  //oracle
    public Map<String, String> tunnel_method;

}
