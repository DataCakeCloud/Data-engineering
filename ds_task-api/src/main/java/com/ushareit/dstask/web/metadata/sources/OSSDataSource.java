package com.ushareit.dstask.web.metadata.sources;

import com.alibaba.fastjson.JSONObject;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.ushareit.dstask.common.param.MetaDataParam;
import com.ushareit.dstask.web.metadata.AbstractDataSource;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OSSDataSource extends AbstractDataSource {
    @Override
    public Boolean checkConnection(MetaDataParam metaDataParam) {
        try{
            JSONObject connectConfig = metaDataParam.getConnectionConfiguration();
            JSONObject provider = connectConfig.getJSONObject("provider");
            String awsAccessKeyId = provider.getString("aws_access_key_id");
            String awsSecretAccessKey = provider.getString("aws_secret_access_key");
            String bucket = provider.getString("bucket");
            String endpoint = provider.getString("endpoint");
            OSS build = new OSSClientBuilder().build(endpoint, awsAccessKeyId, awsSecretAccessKey);
            // 必须执行这一步，上一步不会进行连接测试
            build.getBucketLocation(bucket);
            log.info("测试OSS链接成功: {}",provider);
        }catch (Exception e){
            e.printStackTrace();
            log.error("测试OSS链接失败: ",e);
            throw new RuntimeException(e.getMessage());
        }
        return true;
    }

}
