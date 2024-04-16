package com.ushareit.dstask.bean;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * @author: licg
 * @create: 2020-05-12 15:24
 **/
@Slf4j
@Data
public class FlinkVersion {
    private int majorVersion;
    private int minorVersion;
    private int patchVersion;
    private String versionString;
    private int maxLevel = 3;

    public FlinkVersion(String versionString) {
        this.versionString = versionString;

        try {
            int pos = versionString.indexOf('-');

            String numberPart = versionString;
            if (pos > 0) {
                numberPart = versionString.substring(0, pos);
            }

            String[] versions = numberPart.split("\\.");
            this.majorVersion = Integer.parseInt(versions[0]);
            this.minorVersion = Integer.parseInt(versions[1]);
            if (versions.length == maxLevel) {
                this.patchVersion = Integer.parseInt(versions[2]);
            }

        } catch (Exception e) {
            log.error("Can not recognize Flink version " + versionString +
                    ". Assume it's a future release", e);
        }
    }

    public static FlinkVersion fromVersionString(String versionString) {
        return new FlinkVersion(versionString);
    }

    public boolean isGreaterThanFlink113() {
        return this.majorVersion >= 1 && minorVersion >= 13;
    }
}
