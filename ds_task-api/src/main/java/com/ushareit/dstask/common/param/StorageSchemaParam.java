package com.ushareit.dstask.common.param;

import lombok.Data;
import org.springframework.web.bind.annotation.RequestParam;

@Data
public class StorageSchemaParam {
    private Integer actorId;
    private String type;
    private String path;
    private String fileType;
    private String fieldDelimiter;
}
