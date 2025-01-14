/*
 * Copyright (c) 2022 Airbyte, Inc., all rights reserved.
 */

package com.ushareit.dstask.third.airbyte.common.io;

import cn.hutool.core.io.FileUtil;
import com.google.common.base.Charsets;
import org.apache.commons.io.input.ReversedLinesFileReader;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class IOs {

    public static Path writeFile(final Path path, final String fileName, final String contents) {
        final Path filePath = path.resolve(fileName);
        return writeFile(filePath, contents);
    }

    public static Path writeFile(final Path filePath, final byte[] contents) {
        try {
            Files.write(filePath, contents);
            return filePath;
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Path writeFile(final Path filePath, final String contents) {
        try {
            Files.write(filePath, Collections.singleton(contents), StandardCharsets.UTF_8);
            return filePath;
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Writes a file to a random directory in the /tmp folder. Useful as a staging group for test
     * resources.
     */
    public static String writeFileToRandomTmpDir(final String filename, final String contents) {
        final Path source = Paths.get("/tmp", UUID.randomUUID().toString());
        try {
            final Path tmpFile = source.resolve(filename);
            Files.deleteIfExists(tmpFile);
            Files.createDirectory(source);
            writeFile(tmpFile, contents);
            return tmpFile.toString();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String readFile(final Path path, final String fileName) {
        return readFile(path.resolve(fileName));
    }

    public static String readFile(final Path fullpath) {
        try {
            return FileUtil.readString(fullpath.toFile(), StandardCharsets.UTF_8);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static List<String> getTail(final int numLines, final Path path) throws IOException {
        if (path == null) {
            return Collections.emptyList();
        }

        final File file = path.toFile();
        if (!file.exists()) {
            return Collections.emptyList();
        }

        try (final ReversedLinesFileReader fileReader = new ReversedLinesFileReader(file, Charsets.UTF_8)) {
            final List<String> lines = new ArrayList<>();

            String line;
            while ((line = fileReader.readLine()) != null && lines.size() < numLines) {
                lines.add(line);
            }

            Collections.reverse(lines);

            return lines;
        }
    }

    public static InputStream inputStream(final Path path) {
        try {
            return Files.newInputStream(path);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void silentClose(final Closeable closeable) {
        try {
            closeable.close();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static BufferedReader newBufferedReader(final InputStream inputStream) {
        return new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
    }

}
