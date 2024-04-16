package com.ushareit.dstask.web.utils;


import com.ushareit.dstask.configuration.DataCakeConfig;
import com.ushareit.dstask.configuration.DataCakeRegionProperties;
import com.ushareit.dstask.configuration.DataCakeServiceConfig;
import com.ushareit.dstask.configuration.DataCakeSourceConfig;
import com.ushareit.dstask.third.cloudresource.CloudResourcesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * @author: xuebotao
 * @create: 2023-06-29 15:24
 **/
@Slf4j
@Component
public class DataCakeConfigUtil {

    private static DataCakeConfig dataCakeConfig;

    private static DataCakeRegionProperties dataCakeRegionProperties;

    private static DataCakeServiceConfig dataCakeServiceConfig;

    private static DataCakeSourceConfig dataCakeSourceConfig;

    private static CloudResourcesService cloudResourcesService;

    public static DataCakeConfig getDataCakeConfig() {
        return dataCakeConfig;
    }

    @Autowired
    public void setDataCakeConfig(DataCakeConfig dataCakeConfig) {
        DataCakeConfigUtil.dataCakeConfig = dataCakeConfig;
    }

    public static DataCakeRegionProperties getDataCakeRegionProperties() {
        return dataCakeRegionProperties;
    }

    @Autowired
    public void setDataCakeRegionProperties(DataCakeRegionProperties dataCakeRegionProperties) {
        DataCakeConfigUtil.dataCakeRegionProperties = dataCakeRegionProperties;
    }

    public static DataCakeServiceConfig getDataCakeServiceConfig() {
        return dataCakeServiceConfig;
    }

    @Autowired
    public void setDataCakeServiceConfig(DataCakeServiceConfig dataCakeServiceConfig) {
        DataCakeConfigUtil.dataCakeServiceConfig = dataCakeServiceConfig;
    }

    public static DataCakeSourceConfig getDataCakeSourceConfig() {
        return dataCakeSourceConfig;
    }

    @Autowired
    public void setDataCakeSourceConfig(DataCakeSourceConfig dataCakeSourceConfig) {
        DataCakeConfigUtil.dataCakeSourceConfig = dataCakeSourceConfig;
    }


    public static CloudResourcesService getCloudResourcesService() {
        return cloudResourcesService;
    }

    @Autowired
    public void setCloudResourcesService(CloudResourcesService cloudResourcesService) {
        DataCakeConfigUtil.cloudResourcesService = cloudResourcesService;
    }

}
