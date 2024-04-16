package com.ushareit.dstask.bean;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author wuyan
 * @date 2022/4/20
 */
@Data
@Entity
@Builder
@Table(name = "accumulate_user")
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@ApiModel("累计用户类")
public class AccumulateUser extends BaseEntity{
    private String date;

    private Integer num;
}
