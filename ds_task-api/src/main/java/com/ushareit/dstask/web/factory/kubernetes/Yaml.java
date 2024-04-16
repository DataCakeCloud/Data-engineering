package com.ushareit.dstask.web.factory.kubernetes;

/**
 * @author wuyan
 * @date 2021/12/9
 */
public interface Yaml {
    String replaceYamlVars(String fileContent);
}
