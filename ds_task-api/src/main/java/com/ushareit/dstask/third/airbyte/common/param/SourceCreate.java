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

import javax.persistence.Column;
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
public class SourceCreate {

    @NotNull(message = "数据 source ID 不能为空")
    private Integer sourceDefinitionId;

    @NotNull(message = "参数信息不能为空")
    private JSONObject connectionConfiguration;

    @NotBlank(message = "数据 source 名字不能为空")
    private String name;

    @NotBlank(message = "region 信息不能为空")
    private String region;

    @NotBlank(message = "部门信息")
    private String groups;

    public Actor toAddEntity() {

        CloudResouce cloudResource = DataCakeConfigUtil.getCloudResourcesService().getCloudResource();
        List<String> collect = cloudResource.getList().stream().map(CloudResouce.DataResource::getRegionAlias).collect(Collectors.toList());

        if (StringUtils.isNotEmpty(region) && !collect.contains(region)) {
            throw new ServiceException(BaseResponseCodeEnum.NOT_SUPPORT_REGION);
        }

        Actor actor = Actor.builder()
                .name(name)
                .region(region)
                .actorDefinitionId(sourceDefinitionId)
                .actorType(ActorTypeEnum.source.name())
                .configuration(connectionConfiguration.toString())
                .groups(groups)
                .build();

        actor.setCreateBy(InfTraceContextHolder.get().getUserName());
        actor.setUpdateBy(InfTraceContextHolder.get().getUserName());
        return actor;
    }
}
