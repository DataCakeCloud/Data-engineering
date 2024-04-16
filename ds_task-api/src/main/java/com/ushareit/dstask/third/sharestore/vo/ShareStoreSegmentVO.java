package com.ushareit.dstask.third.sharestore.vo;

import lombok.Data;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * @author fengxiao
 * @date 2023/2/17
 */
@Data
public class ShareStoreSegmentVO {

    private ShareStoreItemVO externalView;
    private ShareStoreItemVO idealState;

    public Integer getNum() {
        if (externalView != null && externalView.getSimpleFields() != null &&
                NumberUtils.isDigits(externalView.getSimpleFields().getNumPartition())) {
            return Integer.parseInt(externalView.getSimpleFields().getNumPartition());
        }

        if (idealState != null && idealState.getSimpleFields() != null &&
                NumberUtils.isDigits(idealState.getSimpleFields().getNumPartition())) {
            return Integer.parseInt(idealState.getSimpleFields().getNumPartition());
        }

        return null;
    }

}
