package com.ushareit.dstask.validator.impl;

import com.alibaba.fastjson.JSON;
import com.amazonaws.util.StringUtils;
import com.ushareit.dstask.bean.Task;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.constant.TemplateEnum;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.validator.TaskContext;
import com.ushareit.dstask.validator.ValidFor;
import com.ushareit.dstask.validator.ValidType;
import com.ushareit.dstask.validator.Validator;
import com.ushareit.dstask.web.factory.flink.param.RuntimeConfig;
import com.ushareit.dstask.web.utils.DataCakeConfigUtil;
import com.ushareit.dstask.web.utils.ParseParamUtil;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @author fengxiao
 * @date 2023/2/9
 */
@Component
@ValidFor(type = ValidType.IMAGE_AND_CLUSTER_IN_SAME_CLOUD)
public class ImageClusterInSameCloudValidator implements Validator {
    private static final String SPARK_K8S_CONTAINER_IMAGE = "spark.kubernetes.container.image";


    @Override
    public void validateImpl(Task task, TaskContext context) {

        if (DataCakeConfigUtil.getDataCakeConfig().getDcRole()) {
            return;
        }
        RuntimeConfig runtimeConfig = JSON.parseObject(task.getRuntimeConfig(), RuntimeConfig.class);
        String region = runtimeConfig.getSourceRegion();
        String image = "";
        if (task.getTemplateCode().equalsIgnoreCase(TemplateEnum.PythonShell.name())){
            image = runtimeConfig.getImage();
        } else {
            String batchParams = runtimeConfig.getBatchParams();
            if (StringUtils.isNullOrEmpty(batchParams)){
                return;
            }

            String replaceBatchParams = batchParams.replace("--", " --");
            //参数替换成固定格式
            Map<String,String> formatBatchParams = ParseParamUtil.formatParamNew(replaceBatchParams);
            if (!formatBatchParams.containsKey(SPARK_K8S_CONTAINER_IMAGE)){
                return;
            }
            image = formatBatchParams.get(SPARK_K8S_CONTAINER_IMAGE);
        }

        String imageRegion = getImageRegion(image);
        if (!region.equalsIgnoreCase(imageRegion)){
            throw new ServiceException(BaseResponseCodeEnum.CLI_PARAM_ILLEGAL.name(), "镜像和运行集群不在同一区域，请确认！");
        }
    }

    private String getImageRegion(String image) {
        if (image.contains("ecr.ap-southeast-1.amazonaws.com")) {
            return "sg1";
        } else if (image.contains("ecr.us-east-1.amazonaws.com")) {
            return "ue1";
        } else if (image.contains("swr.ap-southeast-3.myhuaweicloud.com")) {
            return "sg2";
        } else if(image.contains("asia-southeast1-docker.pkg.dev")){
            return "sg3";
        }
        else {
            throw new ServiceException(BaseResponseCodeEnum.SYS_ERR.name(), "不支持任务配置的image镜像：" + image);
        }
    }
}
