package com.ushareit.dstask.bean.qe;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.Entity;

/**
 * @author wuyan
 * @date 2022/9/6
 */
@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class PersonQuery {
    private String createTime;
    private Integer queryNum;
}
