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
import java.util.List;

/**
 * @author liangyouze
 * @date 2022/11/10
 */
@Data
@Entity
@Builder
@Table(name = "cloud_resource")
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@ApiModel("云资源")
public class CloudResource extends DeleteEntity {

    @ApiModelProperty(value = "云资源名称", position = 1)
    String name;
    @ApiModelProperty(value = "云资源名称", position = 1)
    String regionAlias;
    @ApiModelProperty(value = "租户ID", position = 2)
    String tenantId;
    @ApiModelProperty(value = "云供应商", position = 3)
    String provider;
    @ApiModelProperty(value = "区域", position = 4)
    String region;
    @ApiModelProperty(value = "角色名", position = 5)
    String roleName;
    @ApiModelProperty(value = "存储桶", position = 6)
    String storage;
    @ApiModelProperty(value = "类型:(full-managed/semi-managed)", position = 7)
    String type;
    @ApiModelProperty(value = "vpc id(full-managed类型不需要)", position = 8)
    String vpcId;
    @ApiModelProperty(value = "子网id(full-managed类型不需要)", position = 9)
    List<String> subnetIds;
    @ApiModelProperty(value = "安全组id(full-managed类型不需要)", position = 10)
    List<String> securityGroupIds;
    @ApiModelProperty(value = "租户名称", position = 11)
    String tenantName;
    String providerAlias;
    String catalogName;
}
