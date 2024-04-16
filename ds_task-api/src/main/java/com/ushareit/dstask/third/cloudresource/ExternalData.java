package com.ushareit.dstask.third.cloudresource;


import com.ushareit.dstask.bean.DeleteEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author liangyouze
 * @date 2022/11/10
 */
@Data
@Entity
@Builder
@Table(name = "external_data")
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@ApiModel("挂载数据")
public class ExternalData extends DeleteEntity {
    @ApiModelProperty(value = "数据名称", position = 1)
    String name;
    @ApiModelProperty(value = "数据源", position = 2)
    String dataSource;
    @ApiModelProperty(value = "云资源id", position = 3)
    Long cloudResourceId;
    @ApiModelProperty(value = "路径", position = 4)
    String path;
    @ApiModelProperty(value = "租户ID", position = 5)
    String tenantId;
}
