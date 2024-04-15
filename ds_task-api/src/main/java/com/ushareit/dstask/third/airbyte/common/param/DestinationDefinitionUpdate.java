package com.ushareit.dstask.third.airbyte.common.param;

import com.ushareit.dstask.bean.ActorDefinition;
import com.ushareit.dstask.bean.CloudResouce;
import com.ushareit.dstask.third.airbyte.config.ActorDefinitionResourceRequirements;
import com.ushareit.dstask.third.airbyte.json.Jsons;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import com.ushareit.dstask.web.factory.CloudFactory;
import com.ushareit.dstask.web.utils.DataCakeConfigUtil;
import com.ushareit.dstask.web.utils.cloud.CloudBaseClientUtil;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author fengxiao
 * @date 2022/7/22
 */
@Data
public class DestinationDefinitionUpdate {

    private String name;

    @NotNull(message = "ID 不能为空")
    private Integer destinationDefinitionId;

    @NotBlank(message = "镜像Tag不能为空")
    private String dockerImageTag;

    private MultipartFile iconImage;

    private ActorDefinitionResourceRequirements resourceRequirements;

    public ActorDefinition toUpdateEntity(CloudFactory cloudFactory) {
        String iconUrl = null;
        if (iconImage != null && !iconImage.isEmpty()) {
            CloudResouce.DataResource defaultRegionConfig = DataCakeConfigUtil.getCloudResourcesService().getDefaultRegionConfig();
            String regionAlias = defaultRegionConfig.getRegionAlias();
            CloudBaseClientUtil cloudClientUtil = cloudFactory.getCloudClientUtil(regionAlias);
            iconUrl = cloudClientUtil.upload(iconImage, "datasource", regionAlias);
        }

        ActorDefinition updateParam = ActorDefinition.builder()
                .dockerImageTag(dockerImageTag)
                .icon(iconUrl)
                .build();

        if (StringUtils.isNotBlank(name)) {
            updateParam.setName(name);
        }

        if (resourceRequirements != null) {
            updateParam.setResourceRequirements(Jsons.serialize(resourceRequirements));
        }

        updateParam.setId(destinationDefinitionId);
        updateParam.setUpdateBy(InfTraceContextHolder.get().getUserName());
        return updateParam;
    }

}
