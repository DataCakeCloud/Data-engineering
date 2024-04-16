package com.ushareit.dstask.bean;

import lombok.Data;
import java.util.Date;

@Data
public class ObsS3Object {
    protected String path;

    protected Date lastModified;

    String name;

    public ObsS3Object(String path, Date lastModified, String name) {
        this.path = path;
        this.lastModified = lastModified;
        this.name = name;
    }
}
