package com.ushareit.dstask.web.metadata.sources;

import com.alibaba.fastjson.JSONObject;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.ushareit.dstask.common.param.MetaDataParam;
import com.ushareit.dstask.web.metadata.AbstractDataSource;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class S3DataSource extends AbstractDataSource {
    @Override
    public Boolean checkConnection(MetaDataParam metaDataParam) {
        try{
            JSONObject connectConfig = metaDataParam.getConnectionConfiguration();
            JSONObject provider = connectConfig.getJSONObject("provider");
            String awsAccessKeyId = provider.getString("aws_access_key_id");
            String awsSecretAccessKey = provider.getString("aws_secret_access_key");
            String bucket = provider.getString("bucket");
            String endpoint = provider.getString("endpoint");

            BasicAWSCredentials credentials = new BasicAWSCredentials(awsAccessKeyId, awsSecretAccessKey);
            AmazonS3Client s3Client = new AmazonS3Client(credentials);
            s3Client.setEndpoint(endpoint);
            String bucketLocation = s3Client.getBucketLocation(bucket);
            System.out.println(bucketLocation);

            log.info("测试S3链接成功: {}",connectConfig);
        }catch (Exception e){
            e.printStackTrace();
            log.error("测试S3链接失败: ",e);
            throw new RuntimeException(e.getMessage());
        }
        return true;
    }

}
