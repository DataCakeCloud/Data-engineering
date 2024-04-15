package com.ushareit.dstask.web.factory;


import com.ushareit.dstask.bean.CloudResouce;
import com.ushareit.dstask.configuration.DataCakeRegionProperties;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.third.cloudresource.CloudResourcesService;
import com.ushareit.dstask.web.utils.cloud.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;


/**
 * 云工厂
 *
 * @author: xuebotao
 * @create: 2022-03-10
 */
@Slf4j
@Component
public class CloudFactory {

    @Resource
    public CloudResourcesService cloudResourcesService;

    @Resource
    public DataCakeRegionProperties dataCakeRegionProperties;


    public CloudBaseClientUtil getCloudClientUtil(String region) {
        CloudResouce.DataResource cloudResource = cloudResourcesService.getCloudResource(region);
        return getCloudClientUtilByProvider(cloudResource.getProviderAlias());
    }

    public CloudBaseClientUtil getCloudClientUtilByProvider(String Provider) {

        switch (Provider) {
            case "huaweicloud":
                ObsClientUtil obsClientUtil = new ObsClientUtil();
                obsClientUtil.cloudResourcesService = cloudResourcesService;
                obsClientUtil.dataCakeRegionProperties = dataCakeRegionProperties;
                return obsClientUtil;
            case "aws":
                AwsClientUtil awsClientUtil = new AwsClientUtil();
                awsClientUtil.cloudResourcesService = cloudResourcesService;
                awsClientUtil.dataCakeRegionProperties = dataCakeRegionProperties;
                return awsClientUtil;
            case "googlecloud":
                GoogleCloudUtil googleCloudUtil = new GoogleCloudUtil();
                googleCloudUtil.cloudResourcesService = cloudResourcesService;
                googleCloudUtil.dataCakeRegionProperties = dataCakeRegionProperties;
                return googleCloudUtil;
            case "ks3cloud":
                Ks3ClientUtil ks3ClientUtil = new Ks3ClientUtil();
                ks3ClientUtil.cloudResourcesService = cloudResourcesService;
                ks3ClientUtil.dataCakeRegionProperties = dataCakeRegionProperties;
                return ks3ClientUtil;
            default:
                throw new ServiceException(BaseResponseCodeEnum.CLI_PARAM_ILLEGAL, "can't match region  :" + Provider);
        }
    }

    public CloudBaseClientUtil getCloudClientUtilByUrl(String url) {
        String provider = "";
        if (url.contains("amazonaws.com") || url.startsWith("s3://")) {
            provider = "aws";
        } else if (url.contains("myhuaweicloud.com") || url.startsWith("obs://")) {
            provider = "huaweicloud";
        } else if (url.contains("www.googleapis.com") || url.startsWith("gs://")) {
            provider = "googlecloud";
        } else if (url.contains("ksyuncs.com") || url.startsWith("ks3://")) {
            provider = "ks3cloud";
        } else {
            throw new ServiceException(BaseResponseCodeEnum.CLI_PARAM_ILLEGAL, "can't match region  :" + url);
        }
        return getCloudClientUtilByProvider(provider);

//        Pattern r;
//        Matcher matcher = null;
//        List<String> patternList = new ArrayList<>();
//        patternList.add(DsTaskConstant.AWS_ADDRESS_PATTERN);
//        patternList.add(DsTaskConstant.OBS_ADDRESS_PATTERN_NEW);
//        patternList.add(DsTaskConstant.GCS_ADDRESS_PATTERN);
//        for (String pattern : patternList) {
//            r = Pattern.compile(pattern);
//            matcher = r.matcher(url);
//            if (matcher.find()) {
//                if (pattern.equals(DsTaskConstant.GCS_ADDRESS_PATTERN)) {
//                    return getCloudClientUtil("sg3");
//                }
//                break;
//            }
//            if (!matcher.find() && pattern.equals(patternList.size() - 1)) {
//                throw new ServiceException(BaseResponseCodeEnum.UNKOWN);
//            }
//        }
//        return getCloudClientUtil(matcher.group(3));
    }


}
