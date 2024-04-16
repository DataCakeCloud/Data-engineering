package com.ushareit.dstask.bean.meta;

import com.ushareit.dstask.bean.BaseEntity;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.Entity;

/**
 * @author wuyan
 * @date 2022/9/8
 */
@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@ApiModel("detask")
public class DeTask extends BaseEntity {
    private String statusCode;
    private String createTime;
}
