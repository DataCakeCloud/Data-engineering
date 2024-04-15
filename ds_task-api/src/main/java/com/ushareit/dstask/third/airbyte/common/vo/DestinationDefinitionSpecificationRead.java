package com.ushareit.dstask.third.airbyte.common.vo;

import com.alibaba.fastjson.JSONObject;
import com.ushareit.dstask.bean.ActorDefinition;
import com.ushareit.dstask.third.airbyte.config.ConnectorSpecification;
import com.ushareit.dstask.third.airbyte.json.Jsons;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * @author fengxiao
 * @date 2022/7/22
 */
@Data
public class DestinationDefinitionSpecificationRead {

    private Integer destinationDefinitionId;
    private String documentationUrl;
    private String readMeUrl;
    private JSONObject connectionSpecification;

    public DestinationDefinitionSpecificationRead(ActorDefinition actorDefinition) {
        this.destinationDefinitionId = actorDefinition.getId();
        ConnectorSpecification spec = Jsons.deserialize(actorDefinition.getSpec(), ConnectorSpecification.class);
        this.documentationUrl = spec.getDocumentationUrl().toString();
        if (StringUtils.isNotBlank(actorDefinition.getDocumentationUrl())) {
            String docKey = actorDefinition.getDocumentationUrl().substring(actorDefinition.getDocumentationUrl().lastIndexOf("/") + 1);
            this.readMeUrl = String.format("https://cbs-flink-sg.obs.ap-southeast-3.myhuaweicloud.com/docs/destinations/%s.md", docKey);
        }

        this.connectionSpecification = JSONObject.parseObject(actorDefinition.getSpec());
    }
}
