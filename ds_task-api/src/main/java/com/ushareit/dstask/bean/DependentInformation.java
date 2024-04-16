package com.ushareit.dstask.bean;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author wuyan
 * @date 2022/6/13
 */
@Data
@Entity
@Builder
@Table(name = "dependent_information")
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@ApiModel("依赖信息表")
public class DependentInformation extends DeleteEntity {

    private String region;

    private String groupId;

    private String artifactId;

    private String version;

    private String storageLocation;

}
