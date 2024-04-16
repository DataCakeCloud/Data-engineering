/*
 * Copyright (c) 2022 Airbyte, Inc., all rights reserved.
 */

package com.ushareit.dstask.third.airbyte.common.resources;

import com.google.common.base.Preconditions;
import com.google.common.io.Resources;
import com.ushareit.dstask.third.airbyte.common.lang.Exceptions;
import com.ushareit.dstask.third.airbyte.utils.PathUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.stream.Stream;

@Slf4j
public class MoreResources {

    @SuppressWarnings("UnstableApiUsage")
    public static String readResource(final String name) throws IOException {
        final URL resource = Resources.getResource(name);
        return Resources.toString(resource, StandardCharsets.UTF_8);
    }

    @SuppressWarnings("UnstableApiUsage")
    public static String readResource(final Class<?> klass, final String name) throws IOException {
        final String rootedName = !name.startsWith("/") ? String.format("/%s", name) : name;
        final URL url = Resources.getResource(klass, rootedName);
        return Resources.toString(url, StandardCharsets.UTF_8);
    }

    @SuppressWarnings("UnstableApiUsage")
    public static File readResourceAsFile(final String name) throws URISyntaxException {
        return new File(Resources.getResource(name).toURI());
    }

    @SuppressWarnings("UnstableApiUsage")
    public static byte[] readBytes(final String name) throws IOException {
        final URL resource = Resources.getResource(name);
        return Resources.toByteArray(resource);
    }

    /**
     * This class is a bit of a hack. Might have unexpected behavior.
     *
     * @param klass class whose resources will be access
     * @param name  path to directory in resources list
     * @return stream of paths to each resource file. THIS STREAM MUST BE CLOSED.
     * @throws IOException you never know when you IO.
     */
    public static Stream<Path> listResources(final Class<?> klass, final String name) throws IOException {
        Preconditions.checkNotNull(klass);
        Preconditions.checkNotNull(name);
        Preconditions.checkArgument(StringUtils.isNotBlank(name));

        try {
            final String rootedResourceDir = !name.startsWith("/") ? String.format("/%s", name) : name;
            final URL url = klass.getResource(rootedResourceDir);
            // noinspection ConstantConditions
            Preconditions.checkNotNull(url, "Could not find resource.");

            log.info("url is {}, uri is {}", url.toString(), url.toURI());

            final Path searchPath;
            if (url.toString().startsWith("jar")) {
                final FileSystem fileSystem = FileSystems.newFileSystem(url.toURI(), Collections.emptyMap());
                searchPath = fileSystem.getPath(rootedResourceDir);

                log.info("search path is {}", searchPath.toFile());
                return Files.walk(searchPath, 1).onClose(() -> Exceptions.toRuntime(fileSystem::close));
            } else {
                searchPath = PathUtil.of(url.toURI());
                return Files.walk(searchPath, 1);
            }

        } catch (final URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

}
