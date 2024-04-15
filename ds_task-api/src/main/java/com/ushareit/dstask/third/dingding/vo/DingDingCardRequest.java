package com.ushareit.dstask.third.dingding.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.Entity;
import java.util.List;
import java.util.Map;

/**
 * @author fengxiao
 * @date 2021/2/3
 */
@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class DingDingCardRequest {

    private String msgtype;
    private Map<String, Object> action_card;
    private List<String> receiver;

}
