package com.ushareit.dstask.third.airbyte.common.param;

import com.ushareit.dstask.bean.ActorDefinition;
import com.ushareit.dstask.bean.CloudResouce;
import com.ushareit.dstask.constant.ActorTypeEnum;
import com.ushareit.dstask.constant.DsTaskConstant;
import com.ushareit.dstask.third.airbyte.common.Constant;
import com.ushareit.dstask.third.airbyte.common.enums.ReleaseStage;
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

/**
 * @author fengxiao
 * @date 2022/7/22
 */
@Data
public class DestinationDefinitionCreate {

    @NotBlank(message = "名字不能为空")
    private String name;

    @NotBlank(message = "镜像地址不能为空")
    private String dockerRepository;

    @NotBlank(message = "镜像Tag不能为空")
    private String dockerImageTag;

    private String documentationUrl;

    private MultipartFile iconImage;

    private ActorDefinitionResourceRequirements resourceRequirements;

    public ActorDefinition toAddEntity(CloudFactory cloudFactory) {
        String iconUrl = null;
        if (iconImage != null && !iconImage.isEmpty()) {
            CloudResouce.DataResource defaultRegionConfig = DataCakeConfigUtil.getCloudResourcesService().getDefaultRegionConfig();
            String regionAlias = defaultRegionConfig.getRegionAlias();
            CloudBaseClientUtil cloudClientUtil = cloudFactory.getCloudClientUtil(regionAlias);
            iconUrl = cloudClientUtil.upload(iconImage, "datasource", regionAlias);
        }

        ActorDefinition actorDefinition = ActorDefinition.builder()
                .name(name)
                .actorType(ActorTypeEnum.destination.name())
                .dockerRepository(dockerRepository)
                .dockerImageTag(dockerImageTag)
                .documentationUrl(StringUtils.defaultIfBlank(documentationUrl, StringUtils.EMPTY))
                .icon(StringUtils.defaultIfBlank(iconUrl, Constant.DEFAULT_ICON))
                .releaseStage(ReleaseStage.CUSTOM.toString())
                .build();

        if (resourceRequirements != null) {
            actorDefinition.setResourceRequirements(Jsons.serialize(resourceRequirements));
        }

        actorDefinition.setCreateBy(InfTraceContextHolder.get().getUserName());
        actorDefinition.setUpdateBy(InfTraceContextHolder.get().getUserName());
        return actorDefinition;
    }

}
