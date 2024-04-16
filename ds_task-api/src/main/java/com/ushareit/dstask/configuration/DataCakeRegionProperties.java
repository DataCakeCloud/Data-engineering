package com.ushareit.dstask.configuration;

import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.exception.ServiceException;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author xuebotao
 * @date 2023/04/20
 */
@Slf4j
@Data
@Component
@ConfigurationProperties(prefix = "region")
public class DataCakeRegionProperties {

    private List<RegionConfig> configurations;

    @Data
    public static class RegionConfig {
        private String regionAlias;
        private String sparkHistoryServerUrl;
        private String engine;
        private String hmsUri;
        private String shareStoreEndPoint;
        private String testShareStoreEndPoint;
        private String name;
        private String serviceAccount;
        private String provider;
        private String region;
        private String roleName;
        private String storage;
        private String type;
        private String tenantName;
        private String description;
        private String providerAlias;
        private String catalogName;
    }

    public RegionConfig getRegionConfig(String regionAlias) {
        Map<String, List<RegionConfig>> collect = configurations.stream()
                .collect(Collectors.groupingBy(RegionConfig::getRegionAlias));
        List<RegionConfig> regionConfigs = collect.get(regionAlias);
        if (!regionConfigs.isEmpty()) {
            return regionConfigs.stream().findFirst().get();
        }
        throw new ServiceException(BaseResponseCodeEnum.NOT_SUPPORT_REGION);
    }

    public List<RegionConfig> getRegionConfig() {
        return configurations;
    }

}
