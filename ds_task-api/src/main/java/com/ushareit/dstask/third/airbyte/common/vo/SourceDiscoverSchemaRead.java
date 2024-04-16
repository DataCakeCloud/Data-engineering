package com.ushareit.dstask.third.airbyte.common.vo;

import com.alibaba.fastjson.JSONObject;
import com.ushareit.dstask.third.airbyte.config.AirbyteCatalog;
import com.ushareit.dstask.third.airbyte.json.Jsons;
import lombok.Data;

/**
 * @author fengxiao
 * @date 2022/7/22
 */
@Data
public class SourceDiscoverSchemaRead {

    private JSONObject catalog;
    private Integer catalogId;

    public SourceDiscoverSchemaRead(Integer catalogId, AirbyteCatalog catalog) {
        this.catalogId = catalogId;
        this.catalog = JSONObject.parseObject(Jsons.serialize(catalog));
    }
}
