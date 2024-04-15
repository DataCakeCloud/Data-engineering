package com.ushareit.dstask.web.utils.cloud;


import com.ksyun.ks3.AutoAbortInputStream;
import com.ksyun.ks3.dto.GetObjectResult;
import com.ksyun.ks3.dto.Ks3Object;
import com.ksyun.ks3.dto.Ks3ObjectSummary;
import com.ksyun.ks3.dto.PutObjectResult;
import com.ksyun.ks3.http.HttpClientConfig;
import com.ksyun.ks3.service.Ks3;
import com.ksyun.ks3.service.Ks3Client;
import com.ksyun.ks3.service.Ks3ClientConfig;
import com.ksyun.ks3.service.request.GetObjectRequest;
import com.ushareit.dstask.bean.CloudResouce;
import com.ushareit.dstask.bean.ObsS3Object;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.constant.DsTaskConstant;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import com.ushareit.dstask.web.utils.CommonUtil;
import com.ushareit.dstask.web.utils.UrlUtil;
import com.ushareit.dstask.web.utils.UuidUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

/**
 * @author xuebotao
 * @date 2023/7/25 22:09
 **/

@Slf4j
public class Ks3ClientUtil extends CloudBaseClientUtil {


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
        Ks3 client = getClient();
        Matcher m = UrlUtil.getMatcher(clusterAddress, DsTaskConstant.OBS_AWS_PATH_PATTERN);
        String type = m.group(1);
        String bucketName = m.group(2);
        String firstPath = m.group(3);

        if (firstPath.endsWith(File.separator)) {
            firstPath = firstPath.substring(0, firstPath.length() - 1);
            log.info("集群firstPath:" + firstPath);
        }

        String path = firstPath + File.separator + env + File.separator + appId + DsTaskConstant.CHECKPOINT_PATH + File.separator + flinkJobId + File.separator;
        log.info("path:" + path);
        com.ksyun.ks3.dto.ObjectListing objectListing = client.listObjects(bucketName, path);
        List<Ks3ObjectSummary> objectSummaries = objectListing.getObjectSummaries();

        List<Ks3ObjectSummary> result = objectSummaries.stream().filter(ks3ObjectSummary ->
                !(ks3ObjectSummary.getKey().contains("shared") || ks3ObjectSummary.getKey().contains("taskowned")) && ks3ObjectSummary.getKey().endsWith("_metadata")
        ).sorted(Comparator.comparing(Ks3ObjectSummary::getLastModified).reversed()).collect(Collectors.toList());

        if (result.isEmpty()) {
            log.info(String.format("应用ID:%s下的flinkJobId:%s没有checkpoint", appId, flinkJobId));
            return new ArrayList<>();
        }

        List<ObsS3Object> collect = result.stream().map(ks3ObjectSummary -> {
            String objectKey = ks3ObjectSummary.getKey();
            Date lastModified = ks3ObjectSummary.getLastModified();
            log.info(String.format("应用ID:%s下的flinkJobId:%s有checkpoint,路径:%s", appId, flinkJobId, objectKey));
            String name = objectKey.substring(0, objectKey.lastIndexOf(File.separator));
            return new ObsS3Object(MessageFormat.format("{0}://{1}/{2}", type, bucketName, name), lastModified, name.substring(name.lastIndexOf("/") + 1));
        }).collect(Collectors.toList());
        return collect;
    }

    @Override
    public String upload(MultipartFile file, String region) {
        return upload(file, "jars", region);
    }

    @Override
    public String upload(MultipartFile multipartFile, String subPath, String region) {

        CloudResouce.DataResource cloudResource = cloudResourcesService.getCloudResource(region);
        String realRegion = cloudResource.getRegion();
        //不需要检测路径 桶存在即可
        String keySuffix = cloudResource.getPath() + DsTaskConstant.UPLOAD_OBS_PREFFIX + InfTraceContextHolder.get().getEnv() + "/" + subPath + "/";
        String bucketName = cloudResource.getBucket();
        String oldFileName = multipartFile.getOriginalFilename();
        String key = keySuffix + UuidUtil.getUuid32() + File.separator + oldFileName;
        log.info(String.format("original file[%s], new file[%s]",oldFileName, key));

        Ks3 client = getClient();
        PutObjectResult putObjectResult = client.putObject(bucketName, key, multipartFileToFile(multipartFile));
        return client.generatePresignedUrl(bucketName, key, 600).split("\\?")[0];
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
        log.info("ks3 download:" + content);
        String dest;
        FileOutputStream fos = null;
        AutoAbortInputStream s3is = null;
        try {
            Matcher matcher = UrlUtil.getMatcher(content, DsTaskConstant.KS3_ADDRESS_PATTERN);
            String bucketName = matcher.group(2);
            String key = matcher.group(5) + "/" + matcher.group(6);
            dest = DsTaskConstant.LOCAL_DOWNLOAD_TMP + matcher.group(6);
            if (new File(dest).exists()) {
                log.warn("ks3 dest file has been already exists:" + dest);
                org.apache.flink.util.FileUtils.deleteFileOrDirectory(new File(dest));
            }
            Ks3 client = getClient();
            GetObjectRequest request = new GetObjectRequest(bucketName, key);
            GetObjectResult result = client.getObject(request);
            Ks3Object object = result.getObject();
            s3is = object.getObjectContent();
            fos = new FileOutputStream(dest);
            byte[] read_buf = new byte[1024];
            int read_len = 0;
            while ((read_len = s3is.read(read_buf)) > 0) {
                fos.write(read_buf, 0, read_len);
            }
            log.info("k3s dest file download success：" + dest);
            return dest;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new ServiceException(BaseResponseCodeEnum.KS3_DOWNLOAD_FAIL);
        } finally {
            assert s3is != null;
            s3is.close();
            assert fos != null;
            fos.close();
        }
    }

    @Override
    public void delete(String url) {
        Matcher matcher = UrlUtil.getMatcher(url, DsTaskConstant.KS3_ADDRESS_PATTERN);
        String bucketName = matcher.group(2);
        String objName = matcher.group(5) + "/" + matcher.group(6);
        try {
            Ks3 client = getClient();
            client.deleteObject(bucketName, objName);
            log.info(String.format("ks3删除对象[%s]成功", url));
        } catch (Exception e) {
            log.error(String.format("ks3 failed to delete: %s", url));
            throw new ServiceException(BaseResponseCodeEnum.KS3_DELETE_FAIL);
        }
    }

    public static Ks3 getClient() {
        String accessKeyId = System.getenv("KS3_ACCESS_KEY_ID");
        String accessKeySecret = System.getenv("KS3_ACCESS_KEY_SECRET");
        if (StringUtils.isEmpty(accessKeyId) || StringUtils.isEmpty(accessKeySecret)) {
            throw new ServiceException(BaseResponseCodeEnum.KS3_NO_ACCESS_SECRET);
        }
        Ks3ClientConfig config = new Ks3ClientConfig();
        config.setEndpoint("ks3-cn-beijing.ksyuncs.com");
        HttpClientConfig hconfig = new HttpClientConfig();
        config.setHttpClientConfig(hconfig);


        try {
            return new Ks3Client(accessKeyId, accessKeySecret, config);
        } catch (Exception e) {
            log.error(String.format("keyId[%s], keySecret[%s], failed to connect ks3: %s", accessKeyId, accessKeySecret, CommonUtil.printStackTraceToString(e)));
            throw new ServiceException(BaseResponseCodeEnum.KS_CONNECTION_FAIL);
        }
    }
}
