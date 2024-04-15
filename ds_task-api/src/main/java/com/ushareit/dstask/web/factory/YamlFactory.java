package com.ushareit.dstask.web.factory;

import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.web.factory.flink.param.RuntimeConfig;
import com.ushareit.dstask.web.factory.kubernetes.*;

import java.util.List;
import java.util.Properties;

/**
 * @author wuyan
 * @date 2021/12/9
 */
public class YamlFactory {
    public static Yaml getYaml(String prefix, Properties jobProps, List<RuntimeConfig.Kv> params) {
        switch (prefix) {
            case "1-jobmanager-application.yaml":
                return new JmYaml(jobProps);
            case "2-flink-configuration-configmap.yaml":
                return new ConfigYaml(jobProps, params);
            case "3-jobmanager-rest-service.yaml":
                return new RestServiceYaml(jobProps);
            case "4-jobmanager-service.yaml":
                return new ServiceYaml(jobProps);
            case "5-taskmanager-job-deployment.yaml":
                return new TmYaml(jobProps);
            default:
                throw new ServiceException(BaseResponseCodeEnum.CLI_PARAM_ILLEGAL, "Can not support this type:" + prefix);
        }
    }
}
