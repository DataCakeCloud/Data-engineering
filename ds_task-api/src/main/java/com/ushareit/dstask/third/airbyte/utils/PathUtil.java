package com.ushareit.dstask.third.airbyte.utils;

import java.net.URI;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;

/**
 * @author fengxiao
 * @date 2022/6/21
 */
public class PathUtil {

    public static Path of(String first, String... more) {
        return FileSystems.getDefault().getPath(first, more);
    }

    public static Path of(URI uri) {
        String scheme = uri.getScheme();
        if (scheme == null)
            throw new IllegalArgumentException("Missing scheme");

        // check for default provider to avoid loading of installed providers
        if (scheme.equalsIgnoreCase("file"))
            return FileSystems.getDefault().provider().getPath(uri);

        // try to find provider
        for (FileSystemProvider provider : FileSystemProvider.installedProviders()) {
            if (provider.getScheme().equalsIgnoreCase(scheme)) {
                return provider.getPath(uri);
            }
        }

        throw new FileSystemNotFoundException("Provider \"" + scheme + "\" not installed");
    }

}
