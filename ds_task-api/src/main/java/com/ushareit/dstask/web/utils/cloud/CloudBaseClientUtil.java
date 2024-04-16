package com.ushareit.dstask.web.utils.cloud;


import com.ushareit.dstask.bean.ObsS3Object;
import com.ushareit.dstask.configuration.DataCakeRegionProperties;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.constant.DsTaskConstant;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.third.cloudresource.CloudResourcesService;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class CloudBaseClientUtil {

    public CloudResourcesService cloudResourcesService;

    public DataCakeRegionProperties dataCakeRegionProperties;

    /**
     * 列出云对象
     *
     * @param appId
     * @param flinkJobId
     * @param address
     * @param env
     * @return
     */
    public abstract ObsS3Object listObject(Integer appId, String flinkJobId, String address, String env,String region);

    /**
     * @author wuyan
     * @param appId
     * @param flinkJobId
     * @param address
     * @param env
     * @return
     * @author wuyan
     */
    public abstract List<ObsS3Object> listObjects(Integer appId, String flinkJobId, String address, String env,String region);

    /**
     * 上传
     *
     * @param file
     * @param region
     * @return
     */
    public abstract String upload(MultipartFile file, String region);

    public abstract String upload(MultipartFile file, String subPath, String region);

    public abstract String upload(File file, String subPath, String region);

    /**
     * 下载
     *
     * @param content
     * @return
     */
    public abstract String download(String content) throws IOException;

    /**
     * 删除
     *
     * @param url
     */
    public abstract void delete(String url);


    public String getCloudRegionByUrl(String url) {
        Pattern r;
        Matcher matcher = null;
        List<String> patternList = new ArrayList<>();
        patternList.add(DsTaskConstant.AWS_ADDRESS_PATTERN);
        patternList.add(DsTaskConstant.OBS_ADDRESS_PATTERN_NEW);
        patternList.add(DsTaskConstant.GCS_ADDRESS_PATTERN);
        for (String pattern : patternList) {
            r = Pattern.compile(pattern);
            matcher = r.matcher(url);
            if (matcher.find()) {
                if (pattern.equals(DsTaskConstant.GCS_ADDRESS_PATTERN)) {
                    return "sg3";
                }
                break;
            }
            if (!matcher.find() && pattern.equals(patternList.size() - 1)) {
                throw new ServiceException(BaseResponseCodeEnum.UNKOWN);
            }
        }
        return matcher.group(3);
    }
}
