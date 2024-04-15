package com.ushareit.dstask.common.vo;

import com.ushareit.dstask.bean.Attachment;
import com.ushareit.dstask.bean.Feedback;
import com.ushareit.dstask.constant.SymbolEnum;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author fengxiao
 * @date 2021/11/25
 */
@Data
public class FeedbackVO extends Feedback {
    private static final long serialVersionUID = -1315397903529180438L;

    public String allDetail = "";

    public FeedbackVO(Feedback feedback, Map<Integer, Attachment> attachmentMap) {
        BeanUtils.copyProperties(feedback, this);

        if (StringUtils.isBlank(feedback.getAttachmentIds())) {
            this.setAttachmentList(Collections.emptyList());
        } else {
            this.setAttachmentList(Arrays.stream(feedback.getAttachmentIds().split(SymbolEnum.COMMA.getSymbol()))
                    .filter(Objects::nonNull)
                    .distinct()
                    .map(Integer::parseInt)
                    .map(attachmentMap::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList()));
        }
    }
}
