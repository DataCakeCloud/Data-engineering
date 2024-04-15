/*
 * Copyright (c) 2022 Airbyte, Inc., all rights reserved.
 */

package com.ushareit.dstask.third.airbyte.model;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

import java.io.File;
import java.nio.file.Files;

@Slf4j
public enum AirbyteProtocolSchema {

    PROTOCOL("classpath:airbyte_protocol.yaml");

    private final String schemaFilename;

    AirbyteProtocolSchema(final String schemaFilename) {
        this.schemaFilename = schemaFilename;
    }

    public File getFile() {
        try {
            String tmpPath = System.getProperty("java.io.tmpdir") + "/tomcat_" + System.currentTimeMillis();
            String tmpFile = tmpPath + File.separator + schemaFilename.replaceFirst("classpath:", StringUtils.EMPTY);

            Resource resource = new DefaultResourceLoader().getResource(schemaFilename);
            File file = new File(tmpFile);

            log.info("tmp dir is {}", tmpFile);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }

            if (!file.exists()) {
                boolean result = file.createNewFile();
                log.info("create file {} path {}", result, tmpFile);
            }

            IOUtils.copy(resource.getInputStream(), Files.newOutputStream(new File(tmpFile).toPath()));
            return new File(tmpFile);
        } catch (Exception e) {
            throw new RuntimeException(String.format("获取协议文件失败 %s", schemaFilename), e);
        }
    }

}
