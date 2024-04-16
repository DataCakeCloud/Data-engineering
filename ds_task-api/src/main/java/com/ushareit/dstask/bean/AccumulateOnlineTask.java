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
@Table(name = "accumulate_online_task")
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@ApiModel("累计上线任务类")
public class AccumulateOnlineTask extends BaseEntity{
    private String date;

    private Integer num;
}
