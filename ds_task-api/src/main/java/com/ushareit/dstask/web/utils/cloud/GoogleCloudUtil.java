package com.ushareit.dstask.web.utils.cloud;

import com.google.api.gax.paging.Page;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.*;
import com.ushareit.dstask.bean.CloudResouce;
import com.ushareit.dstask.bean.ObsS3Object;
import com.ushareit.dstask.constant.*;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import com.ushareit.dstask.web.utils.UrlUtil;
import com.ushareit.dstask.web.utils.UuidUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.util.FileUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.stream.Collectors;


@Slf4j
public class GoogleCloudUtil extends CloudBaseClientUtil {


    public static Storage storage;


    public  Storage getStorage() throws IOException {
        if (storage != null) {
            return storage;
        }
        StorageOptions storageOptions = StorageOptions.newBuilder()
//                .setProjectId(PERJECT_ID)
                .setCredentials(ServiceAccountCredentials.getApplicationDefault()
//                .setCredentials(GoogleCredentials.fromStream(
//                         Objects.requireNonNull(this.getClass().getClassLoader().getResourceAsStream("cloud-authentication.json"))
////                        new FileInputStream("ds_task-api/src/main/resources/cloud-authentication.json")
//                        )
                ).build();

        return storageOptions.getService();
    }

    @Override
    public ObsS3Object listObject(Integer appId, String flinkJobId, String address, String env,String region) {
        List<ObsS3Object> objects = listObjects(appId, flinkJobId, address, env,region);
        if (objects == null || objects.size() == 0) {
            return null;
        }
        return objects.get(0);
    }

    @Override
    public List<ObsS3Object> listObjects(Integer appId, String flinkJobId, String address, String env,String region) {
        log.info("正在列出华为云上的对象");
        Matcher m = UrlUtil.getMatcher(address, DsTaskConstant.GPS_PATH_PATTERN);
        String type = m.group(1);
        String bucketName = m.group(2);
        String firstPath = m.group(3);
        List<ObsS3Object> collect= new ArrayList<>();
        if (firstPath.endsWith(File.separator)) {
            firstPath = firstPath.substring(0, firstPath.length() - 1);
            log.info("集群firstPath:" + firstPath);
        }
        String directoryPrefix = firstPath + File.separator + env + File.separator + appId + DsTaskConstant.CHECKPOINT_PATH + File.separator + flinkJobId + File.separator;

        try {
            Storage storage = getStorage();
            Page<Blob> blobs =
                    storage.list(
                            bucketName,
                            Storage.BlobListOption.prefix(directoryPrefix),
                            Storage.BlobListOption.currentDirectory());
            List<Blob> blobList = new ArrayList<>();
            for (Blob blob : blobs.iterateAll()) {
                blobList.add(blob);
            }
            List<Blob> obsObjects = blobList.stream().filter(obsObject ->
                    !(obsObject.getName().contains("shared") || obsObject.getName().contains("taskowned")) && obsObject.getName().endsWith("_metadata")
            ).sorted(Comparator.comparing(h -> h.getUpdateTime())).collect(Collectors.toList());

            collect = obsObjects.stream().map(obsObject -> {
                String objectKey = obsObject.getName();
                Long updateTime = obsObject.getUpdateTime();
                log.info(String.format("应用ID:%s下的flinkJobId:%s有checkpoint,路径:%s", appId, flinkJobId, objectKey));
                String name = objectKey.substring(0, objectKey.lastIndexOf(File.separator));
                return new ObsS3Object(MessageFormat.format("{0}://{1}/{2}", type, bucketName, name), new Date(updateTime), name.substring(name.lastIndexOf("/") + 1));
            }).sorted(Comparator.comparing(ObsS3Object::getLastModified).reversed()).collect(Collectors.toList());

        } catch (Exception e) {
            log.error("listObjects  gcs object fail", e);
            throw new ServiceException(BaseResponseCodeEnum.GCS_LIST_FAIL);
        }
        return collect;
    }

    @Override
    public String upload(MultipartFile file, String region) {
        return upload(file, "jars", region);
    }

    @Override
    public String upload(MultipartFile file, String subPath, String region) {
        try {
            // 检测路径
            CloudResouce.DataResource cloudResource = cloudResourcesService.getCloudResource(region);
            String keySuffix = cloudResource.getPath() + DsTaskConstant.UPLOAD_OBS_PREFFIX + InfTraceContextHolder.get().getEnv() + "/" + subPath + "/";
            // 得到文件原名
            String oldFileName = file.getOriginalFilename();
            String newFileName = keySuffix + UuidUtil.getUuid32() + File.separator + oldFileName;
            log.info("原始文件名" + oldFileName + "  新文件名" + newFileName);
            Storage storage = getStorage();
            BlobId blobId = BlobId.of(cloudResource.getBucket(), newFileName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
            byte[] bytes = readStream(file.getInputStream());
            // 上传文件到=GCS
            Blob blob = storage.create(blobInfo, bytes);
            String link = blob.getSelfLink();
            link = URLDecoder.decode(link, "UTF-8");
            // 获取上传成功后的文件存储地址
//            return GoogleCloudStorageConstant.END_POINT + RegionEnum.regionEnumMap.get(region).getBucket() + newFileName;
//            mediaLink.split("\\?")[0];
            return link;
        } catch (Exception e) {
            log.error("upload jar to gcs", e);
            throw new ServiceException(BaseResponseCodeEnum.GCS_UPLOAD_FAIL);
        }

    }

    @Override
    public String upload(File file, String subPath, String region) {
        return null;
    }


    @Override
    public String download(String content) {
        log.info("gcs地址:" + content);
        Matcher matcher = UrlUtil.getMatcher(content, DsTaskConstant.GCS_ADDRESS_PATTERN);
        String bucketName = matcher.group(2);
        String objName = matcher.group(3);
        String jarName = matcher.group(5);
        String dest;
        try {
            dest = DsTaskConstant.LOCAL_DOWNLOAD_TMP + jarName;
            if (new File(dest).exists()) {
                log.warn("dest file has already standby:" + dest);
                FileUtils.deleteFileOrDirectory(new File(dest));
            }
            log.info("objName=" + objName + "  dest=" + dest);
            Storage storage = getStorage();
            Blob blob = storage.get(BlobId.of(bucketName, objName));
            blob.downloadTo(Paths.get(dest));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new ServiceException(BaseResponseCodeEnum.GCS_DOWNLOAD_FAIL);
        }
        log.info("dest file download success：" + dest);
        return dest;

    }

    @Override
    public void delete(String url) {
        Matcher matcher = UrlUtil.getMatcher(url, DsTaskConstant.GCS_ADDRESS_PATTERN);
        String bucketName = matcher.group(2);
        String objName = matcher.group(3);
//        String jarName = matcher.group(5);
        try {
            Storage storage = getStorage();
            storage.delete(bucketName, objName);
            log.info(String.format("删除gcs对象%s成功", objName));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new ServiceException(BaseResponseCodeEnum.GCS_DELETE_FAIL);
        }
    }


    /**
     * @param inStream
     * @return 字节数组
     * @throws Exception
     * @功能 读取流
     */
    public static byte[] readStream(InputStream inStream) throws Exception {
        int count = 0;
        while (count == 0) {
            count = inStream.available();
        }
        byte[] by = new byte[count];
        inStream.read(by);
        return by;
    }


}
