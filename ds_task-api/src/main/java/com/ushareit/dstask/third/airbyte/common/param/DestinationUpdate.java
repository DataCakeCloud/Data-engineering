package com.ushareit.dstask.third.airbyte.common.param;

import com.alibaba.fastjson.JSONObject;
import com.ushareit.dstask.bean.Actor;
import com.ushareit.dstask.bean.CloudResouce;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import com.ushareit.dstask.web.utils.DataCakeConfigUtil;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author fengxiao
 * @date 2022/7/22
 */
@Data
public class DestinationUpdate {

    @NotNull(message = "数据 destination ID 不能为空")
    private Integer destinationId;
    private JSONObject connectionConfiguration;
    private String name;

    private String region;

    public Actor toUpdateEntity() {

        CloudResouce cloudResource = DataCakeConfigUtil.getCloudResourcesService().getCloudResource();
        List<String> collect = cloudResource.getList().stream().map(CloudResouce.DataResource::getRegionAlias).collect(Collectors.toList());

        if (StringUtils.isNotEmpty(region) && !collect.contains(region)) {
            throw new ServiceException(BaseResponseCodeEnum.NOT_SUPPORT_REGION);
        }

        Actor updateParam = Actor.builder().build();

        if (connectionConfiguration != null) {
            updateParam.setConfiguration(connectionConfiguration.toString());
        }

        if (StringUtils.isNotBlank(name)) {
            updateParam.setName(name);
        }

        if (StringUtils.isNotBlank(region)) {
            updateParam.setRegion(region);
        }

        updateParam.setId(destinationId);
        updateParam.setUpdateBy(InfTraceContextHolder.get().getUserName());
        return updateParam;
    }

}
