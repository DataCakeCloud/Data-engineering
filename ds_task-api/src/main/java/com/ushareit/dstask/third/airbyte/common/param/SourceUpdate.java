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
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author fengxiao
 * @date 2022/7/22
 */
@Data
public class SourceUpdate {

    @NotNull(message = "数据 source ID 不能为空")
    private Integer sourceId;

    private JSONObject connectionConfiguration;

    private String name;

    private String region;

    private String groups;

    private Integer ownerAppId;

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

        if (StringUtils.isNotBlank(groups)) {
            updateParam.setGroups(groups);
        }

        updateParam.setId(sourceId);
        updateParam.setUpdateBy(InfTraceContextHolder.get().getUserName());
        updateParam.setOwnerAppId(ownerAppId);
        return updateParam;
    }

}
