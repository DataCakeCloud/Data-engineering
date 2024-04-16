package com.ushareit.dstask.third.airbyte.common.param;

import com.alibaba.fastjson.JSONObject;
import com.ushareit.dstask.bean.Actor;
import com.ushareit.dstask.bean.CloudResouce;
import com.ushareit.dstask.constant.ActorTypeEnum;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import com.ushareit.dstask.web.utils.DataCakeConfigUtil;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author fengxiao
 * @date 2022/7/22
 */
@Data
public class DestinationCreate {

    @NotBlank(message = "名字不能为空")
    private String name;

    @NotNull(message = "destination ID 不能为空")
    private Integer destinationDefinitionId;

    @NotNull(message = "配置信息不能为空")
    private JSONObject connectionConfiguration;

    @NotBlank(message = "region 信息不能为空")
    private String region;

    public Actor toAddEntity() {
        CloudResouce cloudResource = DataCakeConfigUtil.getCloudResourcesService().getCloudResource();
        List<String> collect = cloudResource.getList().stream().map(CloudResouce.DataResource::getRegionAlias).collect(Collectors.toList());

        if (StringUtils.isNotEmpty(region) && !collect.contains(region)) {
            throw new ServiceException(BaseResponseCodeEnum.NOT_SUPPORT_REGION);
        }

        Actor actor = Actor.builder()
                .name(name)
                .actorDefinitionId(destinationDefinitionId)
                .actorType(ActorTypeEnum.destination.name())
                .configuration(connectionConfiguration.toString())
                .region(region)
                .build();

        actor.setCreateBy(InfTraceContextHolder.get().getUserName());
        actor.setUpdateBy(InfTraceContextHolder.get().getUserName());
        return actor;
    }

}
