package com.ushareit.dstask.third.airbyte.utils;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author fengxiao
 * @date 2022/6/21
 */
public class ProcessFactoryUtil {

    public static String VERSION_DELIMITER = ":";
    public static String DOCKER_DELIMITER = "/";
    public static Pattern ALPHABETIC = Pattern.compile("[a-zA-Z]+");

    /**
     * Docker image names are by convention separated by slashes. The last portion is the image's name.
     * This is followed by a colon and a version number. e.g. airbyte/scheduler:v1 or
     * gcr.io/my-project/image-name:v2.
     * <p>
     * With these two facts, attempt to construct a unique process name with the image name present that
     * can be used by the factories implementing this interface for easier operations.
     */
    public static String createProcessName(final String fullImagePath, final String jobType, final String jobId,
                                           final int attempt, final int lenLimit) {
        final String noVersion = fullImagePath.split(VERSION_DELIMITER)[0];

        final String[] nameParts = noVersion.split(DOCKER_DELIMITER);
        String imageName = nameParts[nameParts.length - 1];

        final String randSuffix = RandomStringUtils.randomAlphabetic(5).toLowerCase();
        final String suffix = jobType + "-" + jobId + "-" + attempt + "-" + randSuffix;

        String processName = imageName + "-" + suffix;
        if (processName.length() > lenLimit) {
            final int extra = processName.length() - lenLimit;
            imageName = imageName.substring(extra);
            processName = imageName + "-" + suffix;
        }

        // Kubernetes pod names must start with an alphabetic character while Docker names accept
        // alphanumeric.
        // Use the stricter convention for simplicity.
        final Matcher m = ALPHABETIC.matcher(processName);
        // Since we add sync-UUID as a suffix a couple of lines up, there will always be a substring
        // starting with an alphabetic character.
        // If the image name is a no-op, this function should always return `sync-UUID` at the minimum.
        m.find();
        return processName.substring(m.start());
    }

}
