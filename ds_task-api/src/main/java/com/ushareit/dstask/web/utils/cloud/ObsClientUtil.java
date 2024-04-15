package com.ushareit.dstask.web.utils.cloud;

import com.obs.services.EnvironmentVariableObsCredentialsProvider;
import com.obs.services.ObsClient;
import com.obs.services.exception.ObsException;
import com.obs.services.model.*;
import com.ushareit.dstask.bean.CloudResouce;
import com.ushareit.dstask.bean.ObsS3Object;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.constant.DsTaskConstant;
import com.ushareit.dstask.constant.HuaweiConstant;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import com.ushareit.dstask.web.utils.UrlUtil;
import com.ushareit.dstask.web.utils.UuidUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.util.FileUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.URLDecoder;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author: wuyan
 * @create: 2020-04-30
 **/
@Slf4j
public class ObsClientUtil extends CloudBaseClientUtil {
    /**
     * 创建云服务器客户端对象
     */
    private static ObsClient obsClient = new ObsClient(new EnvironmentVariableObsCredentialsProvider(), HuaweiConstant.END_POINT);
    /**
     * 上传文件到OBS
     *
     * @param file 上传的文件
     * @return 返回上传的url
     * @region region 区域
     */
    public String upload(MultipartFile file, String region) {
        CloudResouce.DataResource defaultRegionConfig = cloudResourcesService.getDefaultRegionConfig();
        if (StringUtils.isEmpty(region)) {
            return upload(file, "jars", defaultRegionConfig.getRegionAlias());
        }
        return upload(file, "jars", region);
    }

    public String upload(MultipartFile file, String subPath, String region) {
        CloudResouce.DataResource cloudResource = cloudResourcesService.getCloudResource(region);
        try {
            // 检测路径
            String keySuffix = DsTaskConstant.UPLOAD_OBS_PREFFIX + InfTraceContextHolder.get().getEnv() + "/" + subPath + "/";
            checkDir(keySuffix,cloudResource.getBucket());
            // 得到文件原名
            String oldFileName = file.getOriginalFilename();
            String newFileName = keySuffix + UuidUtil.getUuid32() + File.separator + oldFileName;
            log.info("原始文件名" + oldFileName + "  新文件名" + newFileName);
            // 上传文件到OBS
            PutObjectResult putObjectResult = obsClient.putObject(cloudResource.getBucket(), newFileName, file.getInputStream());
            // 获取上传成功后的文件存储地址
            return getObsUrl(URLDecoder.decode(putObjectResult.getObjectUrl(), "UTF-8"));
        } catch (Exception e) {
            log.error("upload jar to obs", e);
            throw new ServiceException(BaseResponseCodeEnum.OBS_UPLOAD_FAIL);
        }
    }

    @Override
    public String upload(File file, String subPath, String region) {
        CloudResouce.DataResource cloudResource = cloudResourcesService.getCloudResource(region);
        try {
            // 检测路径
            String keySuffix = DsTaskConstant.UPLOAD_OBS_PREFFIX + InfTraceContextHolder.get().getEnv() + "/" + subPath + "/";
            checkDir(keySuffix, cloudResource.getBucket());
            // 得到文件原名
            String oldFileName = file.getName();
            String newFileName = keySuffix + UuidUtil.getUuid32() + File.separator + oldFileName;
            log.info("原始文件名" + oldFileName + "  新文件名" + newFileName);
            // 上传文件到OBS
            FileInputStream fileInputStream = new FileInputStream(file);
            PutObjectResult putObjectResult = obsClient.putObject(cloudResource.getBucket(), newFileName, fileInputStream);
            // 获取上传成功后的文件存储地址
            return getObsUrl(URLDecoder.decode(putObjectResult.getObjectUrl(), "UTF-8"));
        } catch (Exception e) {
            log.error("upload jar to obs", e);
            throw new ServiceException(BaseResponseCodeEnum.OBS_UPLOAD_FAIL);
        }
    }

    @Override
    public String download(String content) {
        log.info("obs地址:" + content);
        Matcher matcher = UrlUtil.getMatcher(content, DsTaskConstant.OBS_ADDRESS_PATTERN);
        String bucketName = matcher.group(2);
        String objName = matcher.group(4) + "/" + matcher.group(5);
        String jarName = matcher.group(5);
        String dest;
        try {
            dest = DsTaskConstant.LOCAL_UPLOAD_DIR + jarName;
            if (new File(dest).exists()) {
                log.warn("dest file has already standby:" + dest);
                FileUtils.deleteFileOrDirectory(new File(dest));
            }
            log.info("objName=" + objName + "  dest=" + dest);
            obsClient.downloadFile(new DownloadFileRequest(bucketName, objName, dest));
        }catch (ObsException e){
            log.error(e.getMessage(), e);
            throw new ServiceException(BaseResponseCodeEnum.OBS_DOWNLOAD_FILE_ERR);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new ServiceException(BaseResponseCodeEnum.OBS_DOWNLOAD_FAIL);
        }
        log.info("dest file download success：" + dest);

        return dest;
    }

    /**
     * 删除
     *
     * @param content
     */
    public void delete(String content) {
        Matcher matcher = UrlUtil.getMatcher(content, DsTaskConstant.OBS_ADDRESS_PATTERN);
        String bucketName = matcher.group(2);
        String objName = matcher.group(3);
        try {
            obsClient.deleteObject(bucketName, objName);
            log.info(String.format("删除obs对象%s成功", content));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new ServiceException(BaseResponseCodeEnum.OBS_DELETE_FAIL);
        }
    }

    /**
     * 创建目录
     *
     * @param keySuffixWithSlash
     */
    public static void checkDir(String keySuffixWithSlash, String bucket) {
        if (dirIsExist(keySuffixWithSlash, bucket)) {
            return;
        }

        obsClient.putObject(bucket, keySuffixWithSlash, new ByteArrayInputStream(new byte[0]));
    }

    /**
     * 判断环境对应的文件夹路径是否存在
     *
     * @param objectKey
     * @return
     */
    private static Boolean dirIsExist(String objectKey, String buceket) {
        try {
            obsClient.getObjectMetadata(buceket, objectKey);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * 返回指定flinkjobid下的最新的checkpoint名字
     *
     * @param appId
     * @param flinkJobId
     * @param address
     * @param env
     * @return
     */
    public ObsS3Object listObject(Integer appId, String flinkJobId, String address, String env,String region) {
        List<ObsS3Object> objects = listObjects(appId, flinkJobId, address, env,region);
        if (objects == null || objects.size() == 0) {
            return null;
        }
        return objects.get(0);
    }

    /**
     * @author wuyan
     * @param appId
     * @param flinkJobId
     * @param address
     * @param env
     * @return
     */
    public List<ObsS3Object> listObjects(Integer appId, String flinkJobId, String address, String env,String region) {
        log.info("正在列出华为云上的对象");
        Matcher m = UrlUtil.getMatcher(address, DsTaskConstant.OBS_AWS_PATH_PATTERN);
        String type = m.group(1);
        String bucketName = m.group(2);
        String firstPath = m.group(3);
        List<ObsObject> obsObjects = listObsObjects(appId, flinkJobId, bucketName, firstPath, env);
        if (obsObjects.isEmpty()) {
            log.info(String.format("应用ID:%s下的flinkJobId:%s没有checkpoint", appId, flinkJobId));
            return new ArrayList<>();
        }

        List<ObsS3Object> collect = obsObjects.stream().map(obsObject -> {
            String objectKey = obsObject.getObjectKey();
            Date lastModified = obsObject.getMetadata().getLastModified();
            log.info(String.format("应用ID:%s下的flinkJobId:%s有checkpoint,路径:%s", appId, flinkJobId, objectKey));
            String name = objectKey.substring(0, objectKey.lastIndexOf(File.separator));
            return new ObsS3Object(MessageFormat.format("{0}://{1}/{2}", type, bucketName, name), lastModified, name.substring(name.lastIndexOf("/") + 1));
        }).sorted(Comparator.comparing(ObsS3Object::getLastModified).reversed()).collect(Collectors.toList());
        return collect;
    }

    private List<ObsObject> listObsObjects(Integer appId, String flinkJobId, String bucketName, String firstPath, String env) {
        if (firstPath.endsWith(File.separator)) {
            firstPath = firstPath.substring(0, firstPath.length() - 1);
            log.info("集群firstPath:" + firstPath);
        }
        ListObjectsRequest request = new ListObjectsRequest(bucketName);
        String path = firstPath + File.separator + env + File.separator + appId + DsTaskConstant.CHECKPOINT_PATH + File.separator + flinkJobId + File.separator;

        log.info("path:" + path);
        request.setPrefix(path);
        ObjectListing result = obsClient.listObjects(request);
        log.info(String.format("列出对象结果:%s", result));

        List<ObsObject> obsObjects = result.getObjects().stream().filter(obsObject ->
                !(obsObject.getObjectKey().contains("shared") || obsObject.getObjectKey().contains("taskowned")) && obsObject.getObjectKey().endsWith("_metadata")
        ).sorted(Comparator.comparing(h -> h.getMetadata().getLastModified())).collect(Collectors.toList());

        return obsObjects;
    }


    public static String getObsUrl(String url) {
        Pattern r = Pattern.compile(DsTaskConstant.OBS_ADDRESS_PATTERN);
        Matcher matcher = r.matcher(url);
        if (!matcher.find()) {
            throw new RuntimeException(String.format("对象存储url:%s 格式有误", url));
        }
        return matcher.group(1) + "/" + matcher.group(4) + "/" + matcher.group(5);
    }
}