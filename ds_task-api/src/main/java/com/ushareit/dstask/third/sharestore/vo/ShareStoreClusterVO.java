package com.ushareit.dstask.third.sharestore.vo;

import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

/**
 * @author fengxiao
 * @date 2023/2/20
 */
@Data
public class ShareStoreClusterVO {

    private String id;
    private List<String> idealStates;
    private List<String> externalViews;

    public boolean contains(String segment) {
        if (segment == null) {
            return false;
        }

        if (CollectionUtils.isNotEmpty(idealStates) && idealStates.contains(segment)) {
            return true;
        }

        return CollectionUtils.isNotEmpty(externalViews) && externalViews.contains(segment);
    }

}
