package com.ushareit.dstask.web.metadata.lakecat;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@ApiModel("数据权限判断")
public class AuthenticationReq {

    @NotNull
    @ApiModelProperty(value = "项目ID， 默认shareit", required = true)
    private String projectId;
    @NotNull
    @ApiModelProperty(value = "catalogName", required = true)
    private String catalogName;
    @NotNull
    @ApiModelProperty(value = "operation", required = true, allowableValues = "[DESC_TABLE, CREATE_DATABASE, DROP_DATABASE, INSERT_TABLE, DROP_TABLE, ALTER_DATABASE, SELECT_TABLE, DESC_DATABASE, ALTER_TABLE, CREATE_TABLE]")
    private String operation;
    @NotNull
    @ApiModelProperty(value = "qualifiedName, 全限定名称", required = true, example = "${catalogName}.${databaseName}.${tableName}")
    private String qualifiedName;
    @NotNull
    @ApiModelProperty(value = "userId, 你的shareid", required = true)
    private String userId;
    @NotNull
    @ApiModelProperty(value = "region, 数据区域", required = true)
    private String region;
}
