package com.ushareit.dstask.web.utils;


import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.ushareit.dstask.bean.ObsS3Object;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;

import java.io.*;
import java.net.URI;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 操作hdfs文件的工具类
 */

@Slf4j
public class HDFSUtils {


    public static List<ObsS3Object> getHdfsStatus(String hdfsAddress, String hdfsPath) {
        log.info("hdfsAddress is : " + hdfsAddress);
        if (StringUtils.isNotEmpty(hdfsPath) && hdfsPath.contains("hdfs")) {
            hdfsPath = hdfsPath.split("hdfs://")[1];
        }
        log.info("hdfsPath is : " + hdfsPath);
        List<ObsS3Object> collect = new ArrayList<>();
        try {
            Path path = new Path(hdfsAddress);
            Configuration configuration = new Configuration();
            FileSystem fileSystem = path.getFileSystem(configuration);
            Path directory = new Path(hdfsPath);
            FileStatus[] files = fileSystem.listStatus(directory);
            for (FileStatus file : files) {
                if (!(file.getPath().getName().contains("shared") || file.getPath().getName().contains("taskowned"))) {
                    ObsS3Object object = new ObsS3Object(file.getPath().toString(), new Date(file.getModificationTime()), file.getPath().getName());
                    collect.add(object);
                }
            }
            // 关闭文件系统
            fileSystem.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return collect.stream().sorted(Comparator.comparing(ObsS3Object::getLastModified).reversed()).collect(Collectors.toList());
    }


}
