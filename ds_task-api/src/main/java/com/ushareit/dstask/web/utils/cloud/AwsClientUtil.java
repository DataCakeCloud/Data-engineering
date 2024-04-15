package com.ushareit.dstask.web.utils.cloud;


import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.ushareit.dstask.DsTaskApplication;
import com.ushareit.dstask.bean.CloudResouce;
import com.ushareit.dstask.bean.ObsS3Object;
import com.ushareit.dstask.configuration.AwsConfig;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.constant.DsTaskConstant;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import com.ushareit.dstask.web.utils.UrlUtil;
import com.ushareit.dstask.web.utils.UuidUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.stream.Collectors;


/**
 * @author xuebotao
 * @date 2022-03-11
 */
@Slf4j
public class AwsClientUtil extends CloudBaseClientUtil {
    private static   AmazonS3 s3 ;

    /**
     * 上传
     */
    public String upload(MultipartFile multipartFile, String subPath, String region) {
        try {
            CloudResouce.DataResource cloudResource = cloudResourcesService.getCloudResource(region);
            String realRegion = cloudResource.getRegion();
            AwsConfig awsConfig = DsTaskApplication.getBean(AwsConfig.class);
            AmazonS3 client = getClient(awsConfig);
            //不需要检测路径 桶存在即可
            String keySuffix = cloudResource.getPath() + DsTaskConstant.UPLOAD_OBS_PREFFIX + InfTraceContextHolder.get().getEnv() + "/" + subPath + "/";
            String bucketName = cloudResource.getBucket();
            String oldFileName = multipartFile.getOriginalFilename();
            String key = keySuffix + UuidUtil.getUuid32() + File.separator + oldFileName;
            log.info("原始文件名:" + oldFileName + "  新文件名:" + key);
            // 上传文件到aws
            client.putObject(new PutObjectRequest(bucketName, key, multipartFileToFile(multipartFile)));
//                    .withCannedAcl(CannedAccessControlList.PublicRead));
            GeneratePresignedUrlRequest urlRequest = new GeneratePresignedUrlRequest(
                    bucketName, key);
            URL url = client.generatePresignedUrl(urlRequest);
            String resurl = url.toString().split("\\?")[0];
            if (realRegion.equals("us-east-1")) {
                Matcher matcher = UrlUtil.getMatcher(resurl, DsTaskConstant.AWS_ADDRESS_PATTERN_NEW);
                resurl = matcher.group(1) + "." + realRegion + matcher.group(3) + matcher.group(4);
            }
            return resurl;
        } catch (Exception e) {
            log.error("upload jar to aws", e);
            throw new ServiceException(BaseResponseCodeEnum.AWS_UPLOAD_FAIL);
        }
    }

    @Override
    public String upload(File file, String subPath, String region) {
        try {
            CloudResouce.DataResource cloudResource = cloudResourcesService.getCloudResource(region);
            String realRegion = cloudResource.getRegion();
            AwsConfig awsConfig = DsTaskApplication.getBean(AwsConfig.class);
            AmazonS3 client = getClient(awsConfig);
            //不需要检测路径 桶存在即可
            String keySuffix = DsTaskConstant.UPLOAD_OBS_PREFFIX + InfTraceContextHolder.get().getEnv() + "/" + subPath + "/";
            String bucketName = cloudResource.getBucket();
            String oldFileName = file.getName();
            String key = keySuffix + UuidUtil.getUuid32() + File.separator + oldFileName;
            log.info("region:" + region + "  bucketName:" + bucketName + "  原始文件名:" + oldFileName + "  新文件名:" + key);
            // 上传文件到aws
            client.putObject(new PutObjectRequest(bucketName, key, file));
//                    .withCannedAcl(CannedAccessControlList.PublicRead));
            GeneratePresignedUrlRequest urlRequest = new GeneratePresignedUrlRequest(
                    bucketName, key);
            URL url = client.generatePresignedUrl(urlRequest);
            String resurl = url.toString().split("\\?")[0];
            if (realRegion.equals("us-east-1")) {
                Matcher matcher = UrlUtil.getMatcher(resurl, DsTaskConstant.AWS_ADDRESS_PATTERN_NEW);
                resurl = matcher.group(1) + "." + realRegion + matcher.group(3) + matcher.group(4);
            }
            log.info("resurl:" + resurl);
            return resurl;
        } catch (Exception e) {
            log.error("upload jar to aws", e);
            throw new ServiceException(BaseResponseCodeEnum.AWS_UPLOAD_FAIL);
        }
    }


    @Override
    public String upload(MultipartFile file, String region) {
        return upload(file, "jars", region);
    }


    /**
     * s3下载
     */
    public String download(String content) {
        log.info("aws地址:" + content);
        String dest;
        try {
            Matcher matcher = UrlUtil.getMatcher(content, DsTaskConstant.AWS_ADDRESS_PATTERN);
            String bucketName = matcher.group(2);
            String key = matcher.group(5) + "/" + matcher.group(6);
            log.info("aws地址,bucketName{},key:{}", bucketName,key);
            String jarName = matcher.group(6);
            dest = DsTaskConstant.LOCAL_DOWNLOAD_TMP + jarName;
            if (new File(dest).exists()) {
                log.warn("dest file has already standby:" + dest);
                org.apache.flink.util.FileUtils.deleteFileOrDirectory(new File(dest));
            }
            AwsConfig awsConfig = DsTaskApplication.getBean(AwsConfig.class);
            AmazonS3 client = getClient(awsConfig);
            S3Object object = client.getObject(bucketName, key);
            S3ObjectInputStream s3is;
            FileOutputStream fos;
            s3is = object.getObjectContent();
            fos = new FileOutputStream(dest);
            byte[] read_buf = new byte[1024];
            int read_len = 0;
            while ((read_len = s3is.read(read_buf)) > 0) {
                fos.write(read_buf, 0, read_len);
            }
            s3is.close();
            fos.close();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new ServiceException(BaseResponseCodeEnum.AWS_DOWNLOAD_FAIL);
        }
        log.info("dest file download success：" + dest);
        return dest;
    }


    public File multipartFileToFile(MultipartFile multiFile) {
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
            throw new ServiceException(BaseResponseCodeEnum.AWS_FILE_CONVERSION_FAIL);
        }
    }

    /**
     * 返回指定flinkjobid下的最新的checkpoint名字
     *
     * @param appId
     * @param flinkJobId
     * @param clusterAddress
     * @param env
     * @return
     */
    public ObsS3Object listObject(Integer appId, String flinkJobId, String clusterAddress, String env,String region) {
        List<ObsS3Object> objects = listObjects(appId, flinkJobId, clusterAddress, env,region);
        if (objects != null || objects.size() == 0) {
            return null;
        }
        return objects.get(0);
    }

    /**
     * @author wuyan
     * @param appId
     * @param flinkJobId
     * @param clusterAddress
     * @param env
     * @return
     */
    public List<ObsS3Object> listObjects(Integer appId, String flinkJobId, String clusterAddress, String env,String region) {
        log.info("正在列出亚马逊云上的对象");
        Matcher m = UrlUtil.getMatcher(clusterAddress, DsTaskConstant.OBS_AWS_PATH_PATTERN);
        String type = m.group(1);
        String bucketName = m.group(2);
        String firstPath = m.group(3);

        List<S3ObjectSummary> awsObjects = listS3ObjectSummary(appId, flinkJobId, bucketName, firstPath, env,region);
        if (awsObjects.isEmpty()) {
            log.info(String.format("应用ID:%s下的flinkJobId:%s没有checkpoint", appId, flinkJobId));
            return new ArrayList<>();
        }

        List<ObsS3Object> collect = awsObjects.stream().map(awsObject -> {
            String objectKey = awsObject.getKey();
            Date lastModified = awsObject.getLastModified();
            log.info(String.format("应用ID:%s下的flinkJobId:%s有checkpoint,路径:%s", appId, flinkJobId, objectKey));
            String name = objectKey.substring(0, objectKey.lastIndexOf(File.separator));
            return new ObsS3Object(MessageFormat.format("{0}://{1}/{2}", type, bucketName, name), lastModified, name.substring(name.lastIndexOf("/") + 1));
        }).collect(Collectors.toList());

        return collect;
    }

    private List<S3ObjectSummary> listS3ObjectSummary(Integer appId, String flinkJobId, String bucketName, String firstPath, String env,String region) {
        if (firstPath.endsWith(File.separator)) {
            firstPath = firstPath.substring(0, firstPath.length() - 1);
            log.info("集群firstPath:" + firstPath);
        }
        String path = firstPath + File.separator + env + File.separator + appId + DsTaskConstant.CHECKPOINT_PATH + File.separator + flinkJobId + File.separator;
        log.info("path:" + path);
//        String region = getRegion(bucketName);
        CloudResouce.DataResource cloudResource = cloudResourcesService.getCloudResource(region);
        AwsConfig awsConfig = DsTaskApplication.getBean(AwsConfig.class);
        AmazonS3 client = getClient(awsConfig);
        ListObjectsV2Result result = client.listObjectsV2(bucketName, path);
        log.info(String.format("列出对象结果:%s", result));
        List<S3ObjectSummary> awsObjects = result.getObjectSummaries().stream().filter(awsObject ->
                !(awsObject.getKey().contains("shared") || awsObject.getKey().contains("taskowned")) && awsObject.getKey().endsWith("_metadata")
        ).sorted(Comparator.comparing(S3ObjectSummary::getLastModified).reversed()).collect(Collectors.toList());

        return awsObjects;
    }




    /**
     * 删除
     *
     * @param content 要删除文件的url
     */
    public void delete(String content) {
        Matcher matcher = UrlUtil.getMatcher(content, DsTaskConstant.AWS_ADDRESS_PATTERN);
        String bucketName = matcher.group(2);
        String region = matcher.group(3);
//        String objName = matcher.group(5) + "/" + matcher.group(6);
        String objName = matcher.group(4);
        try {
            AwsConfig awsConfig = DsTaskApplication.getBean(AwsConfig.class);
            AmazonS3 client = getClient(awsConfig);
            client.deleteObject(bucketName, objName);
            log.info(String.format("删除aws对象%s成功", content));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new ServiceException(BaseResponseCodeEnum.AWS_DELETE_FAIL);
        }
    }


    public static AmazonS3 getClient(AwsConfig awsConfig ) {
        if (s3 != null) {
            return s3;
        }
        BasicAWSCredentials credentials = new BasicAWSCredentials(awsConfig.getAccessKey(), awsConfig.getSecretKey());
        s3 = AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(awsConfig.getEndpoint(),""))
                .build();
        return s3;
    }


    private String getRegion(String bucketName) {
        return bucketName.substring(bucketName.lastIndexOf(".") + 1);
    }

    public static void main(String[] args) {

        String content = "https://bd-datacake-fra.s3.eu-central-1.amazonaws.com/BDP/logs/pipeline_log/20240408/91ad6882-e848-4f91-917c-153453b629ab.txt";

        Matcher matcher = UrlUtil.getMatcher(content, DsTaskConstant.AWS_ADDRESS_PATTERN);
        String bucketName = matcher.group(2);
        String reg = matcher.group(3);
        String key = matcher.group(5) + "/" + matcher.group(6);
        String jarName = matcher.group(6);
    }
}
