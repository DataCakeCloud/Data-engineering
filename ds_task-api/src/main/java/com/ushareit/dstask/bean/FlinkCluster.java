package com.ushareit.dstask.bean;

import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.exception.ServiceException;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;

/**
 * @author: licg
 * @create: 2021-05-12 15:24
 */
@Data
@Entity
@Builder
@Table(name = "flink_cluster")
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@ApiModel("Flink集群类")
@Slf4j
public class FlinkCluster  extends DeleteEntity{
    @NotBlank(message = "应用名字不能为空")
    @ApiModelProperty(value = "应用名字")
    private String name;

    @ApiModelProperty("类型 SESSION/PER-JOB")
    @Column(name = "type_code")
    private String typeCode;

    @ApiModelProperty("集群服务基地址")
    @Column(name = "address")
    private String address;

    @ApiModelProperty("状态存储路径")
    @Column(name = "state_path")
    private String statePath;

    @ApiModelProperty("ZK服务地址")
    @Column(name = "zookeeper_quorum")
    private String zookeeperQuorum;

    @ApiModelProperty("日志对应es数据源")
    @Column(name = "log_es_source")
    private String logEsSource;

    @ApiModelProperty("容器镜像地址")
    @Column(name = "container_image")
    private String containerImage;

    @ApiModelProperty("region")
    @Column(name = "region")
    private String region;

    @ApiModelProperty("env")
    @Column(name = "env")
    private String env;

    @ApiModelProperty("namespace")
    @Column(name = "namespace")
    private String namespace;

    @ApiModelProperty("node-selector")
    @Column(name = "node_selector")
    private String nodeSelector;

    @ApiModelProperty("tolerations")
    @Column(name = "tolerations")
    private String tolerations;

    @ApiModelProperty("version")
    @Column(name = "version")
    private String version;

    public String getNameSpace() {
        if (StringUtils.isEmpty(namespace)) {
            throw new ServiceException(BaseResponseCodeEnum.CLUSTER_NS_NOT_FOUND);
        }
        return namespace;
    }
}
