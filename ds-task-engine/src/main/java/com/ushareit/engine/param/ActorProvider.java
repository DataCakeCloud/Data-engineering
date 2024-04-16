package com.ushareit.engine.param;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

@Data
public class ActorProvider {
    String bucket;
    String endpoint;
    @JSONField(name = "aws_access_key_id")
    String awsAccessKeyId;
    @JSONField(name = "aws_secret_access_key")
    String awsSecretAccessKey;
}
