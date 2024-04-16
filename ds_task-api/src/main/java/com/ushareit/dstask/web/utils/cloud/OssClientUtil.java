package com.ushareit.dstask.web.utils.cloud;


import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.auth.EnvironmentVariableCredentialsProvider;
import com.aliyun.oss.model.*;
import com.ushareit.dstask.bean.CloudResouce;
import com.ushareit.dstask.bean.ObsS3Object;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.constant.DsTaskConstant;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import com.ushareit.dstask.web.utils.UrlUtil;
import com.ushareit.dstask.web.utils.UuidUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

/**
 * @author xuebotao
 * @date 2024/1/26 22:09
 **/

@Slf4j
public class OssClientUtil extends CloudBaseClientUtil {

    //https://oss-cn-hangzhou.aliyuncs.com
    public static final String OSS_END_POINT = "https://oss-{0}.aliyuncs.com";
    //datacake1.cn-beijing.oss-dls.aliyuncs.com
    //cn-beijing.oss-dls.aliyuncs.com
    public static final String OSS_END_POINT_HDFS = "https://{0}.oss-dls.aliyuncs.com";

    @Override
    public ObsS3Object listObject(Integer appId, String flinkJobId, String address, String env, String region) {
        List<ObsS3Object> objects = listObjects(appId, flinkJobId, address, env, region);
        if (objects != null || objects.size() == 0) {
            return null;
        }
        return objects.get(0);
    }


    @Override
    public List<ObsS3Object> listObjects(Integer appId, String flinkJobId, String clusterAddress, String env, String region) {
        CloudResouce.DataResource cloudResource = cloudResourcesService.getCloudResource(region);
        Matcher m = UrlUtil.getMatcher(clusterAddress, DsTaskConstant.OBS_AWS_PATH_PATTERN);
        String type = m.group(1);
        String bucketName = m.group(2);
        if(bucketName.contains("oss-dls.aliyuncs.com")){
            bucketName= bucketName.split("\\.")[0];
        }
        String firstPath = m.group(3);
        if (firstPath.endsWith(File.separator)) {
            firstPath = firstPath.substring(0, firstPath.length() - 1);
            log.info("集群firstPath:" + firstPath);
        }
        log.info("bucketName is :" + bucketName);
        String path = firstPath + File.separator + env + File.separator + appId + DsTaskConstant.CHECKPOINT_PATH + File.separator + flinkJobId + File.separator;
        log.info("path:" + path);

        // 设置最大个数。
        int maxKeys = 1000;
        // 创建OSSClient实例。
        OSS ossClient = getClient(cloudResource.getRegion(),"HDFS");
        List<ObsS3Object> collect = new ArrayList<>();
        try {
            ListObjectsV2Request listObjectsV2Request = new ListObjectsV2Request(bucketName, path);
            listObjectsV2Request.setMaxKeys(maxKeys);
            ListObjectsV2Result result = ossClient.listObjectsV2(listObjectsV2Request);
            List<OSSObjectSummary> ossObjectSummaries = result.getObjectSummaries();
            List<OSSObjectSummary> resList = ossObjectSummaries.stream().filter(ks3ObjectSummary ->
                    !(ks3ObjectSummary.getKey().contains("shared") || ks3ObjectSummary.getKey().contains("taskowned")) && ks3ObjectSummary.getKey().endsWith("_metadata")
            ).sorted(Comparator.comparing(OSSObjectSummary::getLastModified).reversed()).collect(Collectors.toList());

            if (resList.isEmpty()) {
                log.info(String.format("应用ID:%s下的flinkJobId:%s没有checkpoint", appId, flinkJobId));
                return new ArrayList<>();
            }

            String finalBucketName = bucketName;
            collect = resList.stream().map(ossObjectSummary -> {
                String objectKey = ossObjectSummary.getKey();
                Date lastModified = ossObjectSummary.getLastModified();
                log.info(String.format("应用ID:%s下的flinkJobId:%s有checkpoint,路径:%s", appId, flinkJobId, objectKey));
                String name = objectKey.substring(0, objectKey.lastIndexOf(File.separator));
                return new ObsS3Object(MessageFormat.format("{0}://{1}/{2}", type, finalBucketName, name), lastModified, name.substring(name.lastIndexOf("/") + 1));
            }).collect(Collectors.toList());
        } catch (OSSException oe) {
            log.error("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            log.error("Error Message:" + oe.getErrorMessage());
            log.error("Error Code:" + oe.getErrorCode());
            log.error("Request ID:" + oe.getRequestId());
            log.error("Host ID:" + oe.getHostId());
            throw new ServiceException(BaseResponseCodeEnum.OSS_LIST_FAIL);
        } catch (ClientException ce) {
            log.error("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            log.error("Error Message:" + ce.getMessage());
            throw new ServiceException(BaseResponseCodeEnum.OSS_LIST_FAIL);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
        return collect;
    }

    @Override
    public String upload(MultipartFile file, String region) {
        return upload(file, "jars", region);
    }


    @Override
    public String upload(MultipartFile multipartFile, String subPath, String region) {
        // Endpoint以华东1（杭州）为例，其它Region请按实际情况填写。
        // 从环境变量中获取访问凭证。运行本代码示例之前，请确保已设置环境变量OSS_ACCESS_KEY_ID和OSS_ACCESS_KEY_SECRET。
        CloudResouce.DataResource cloudResource = cloudResourcesService.getCloudResource(region);
        String keySuffix = cloudResource.getPath() + DsTaskConstant.UPLOAD_OBS_PREFFIX + InfTraceContextHolder.get().getEnv() + "/" + subPath + "/";
        String bucketName = cloudResource.getBucket();
        String oldFileName = multipartFile.getOriginalFilename();
        String key = keySuffix + UuidUtil.getUuid32() + File.separator + oldFileName;
        log.info(String.format("original file[%s], new file[%s]", oldFileName, key));

        // 创建OSSClient实例。
        OSS ossClient = getClient(cloudResource.getRegion(),null);
        GeneratePresignedUrlRequest generatePresignedUrlRequest = null;
        PutObjectResult result = null;
        try {

            InputStream inputStream = new FileInputStream(multipartFileToFile(multipartFile));
            // 创建PutObjectRequest对象。
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key, inputStream);
            // 创建PutObject请求。
            result = ossClient.putObject(putObjectRequest);
            generatePresignedUrlRequest = new GeneratePresignedUrlRequest(bucketName, key);
            Date date = new Date(System.currentTimeMillis() + 10 * 1000L);
            generatePresignedUrlRequest.setExpiration(date);
            URL url = ossClient.generatePresignedUrl(generatePresignedUrlRequest);
            if (StringUtils.isNotEmpty(url.toString())) {
                String[] split = url.toString().split("\\?");
                return split[0];
            }
        } catch (OSSException oe) {
            log.info("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            log.info("Error Message:" + oe.getErrorMessage());
            log.info("Error Code:" + oe.getErrorCode());
            log.info("Request ID:" + oe.getRequestId());
            log.info("Host ID:" + oe.getHostId());
            throw new ServiceException(BaseResponseCodeEnum.OSS_UPLOAD_FAIL);
        } catch (Exception ce) {
            log.info("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            log.info("Error Message:" + ce.getMessage());
            throw new ServiceException(BaseResponseCodeEnum.OSS_UPLOAD_FAIL);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
        return result.toString();
    }



    @Override
    public String upload(File file, String subPath, String region) {
        return null;
    }

    public static File multipartFileToFile(MultipartFile multiFile) {
        String fileName = multiFile.getOriginalFilename();
        String name = fileName.substring(fileName.lastIndexOf("."));
        String prefix = fileName.substring(0, fileName.lastIndexOf("."));
        String tmpDir = DsTaskConstant.LOCAL_DOWNLOAD_TMP + fileName;
        try {
            if (new File(tmpDir).exists()) {
                org.apache.flink.util.FileUtils.deleteFileOrDirectory(new File(tmpDir));
            }
            File file = File.createTempFile(name, prefix, new File(DsTaskConstant.LOCAL_DOWNLOAD_TMP));
            FileUtils.copyInputStreamToFile(multiFile.getInputStream(), file);
            return file;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new ServiceException(BaseResponseCodeEnum.KS_FILE_CONVERSION_FAIL);
        }

    }


    @Override
    public String download(String content) throws IOException {
        log.info("oss download:" + content);
        String dest;

        Matcher matcher = UrlUtil.getMatcher(content, DsTaskConstant.OSS_ADDRESS_PATTERN);
        String bucketName = matcher.group(2);
        String reg = matcher.group(3);
        String key = matcher.group(5) + "/" + matcher.group(6);
        dest = DsTaskConstant.LOCAL_DOWNLOAD_TMP + matcher.group(6);
        OSS ossClient = getClient(reg,null);
        try {
            if (new File(dest).exists()) {
                log.warn("dest file has already standby:" + dest);
                org.apache.flink.util.FileUtils.deleteFileOrDirectory(new File(dest));
            }
            // 下载Object到本地文件，并保存到指定的本地路径中。如果指定的本地文件存在会覆盖，不存在则新建。
            // 如果未指定本地路径，则下载后的文件默认保存到示例程序所属项目对应本地路径中。
            ossClient.getObject(new GetObjectRequest(bucketName, key), new File(dest));
        } catch (OSSException oe) {
            log.error("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            log.error("Error Message:" + oe.getErrorMessage());
            log.error("Error Code:" + oe.getErrorCode());
            log.error("Request ID:" + oe.getRequestId());
            log.error("Host ID:" + oe.getHostId());
            throw new ServiceException(BaseResponseCodeEnum.OSS_DOWNLOAD_FAIL);
        } catch (ClientException ce) {
            log.error("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            log.error("Error Message:" + ce.getMessage());
            throw new ServiceException(BaseResponseCodeEnum.OSS_DOWNLOAD_FAIL);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
        log.info("dest file download success：" + dest);
        return dest;
    }


    //获取客户端
    public OSS getClient(String region,String type) {
        String endPoint=OSS_END_POINT;
        if(StringUtils.isNotEmpty(type) && type.equals("HDFS")){
            endPoint=OSS_END_POINT_HDFS;
        }
        EnvironmentVariableCredentialsProvider credentialsProvider = null;
        OSS ossClient = null;
        try {
            credentialsProvider = CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();
            String format = MessageFormat.format(endPoint, region);
            ossClient = new OSSClientBuilder().build(format, credentialsProvider);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new ServiceException(BaseResponseCodeEnum.OSS_CLIENT_GET_FAIL);
        }
        return ossClient;
    }



    @Override
    public void delete(String url) {
        Matcher matcher = UrlUtil.getMatcher(url, DsTaskConstant.OSS_ADDRESS_PATTERN);
        String bucketName = matcher.group(2);
        String reg = matcher.group(3);
        String objName = matcher.group(5) + "/" + matcher.group(6);
        OSS client = getClient(reg,null);
        try {
            client.deleteObject(bucketName, objName);
            log.info(String.format("oss 删除对象[%s]成功", url));
        } catch (Exception e) {
            log.error(String.format("oss failed to delete: %s", url));
            throw new ServiceException(BaseResponseCodeEnum.OSS_DELETE_FAIL);
        }
    }


}
