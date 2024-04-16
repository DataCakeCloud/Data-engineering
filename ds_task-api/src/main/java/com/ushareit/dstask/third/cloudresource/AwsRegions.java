package com.ushareit.dstask.third.cloudresource;

import java.util.Arrays;
import java.util.List;

/**
 * @author renjianxu
 * @date 2022/11/17
 */
public class AwsRegions {
    public static List<AwsRegions> AWS_REGIONS = Arrays.asList(
            new AwsRegions("us-gov-west-1", "AWS GovCloud(美国) us-gov-west-1"),
            new AwsRegions("us-gov-east-1", "AWS GovCloud (美东) us-gov-east-1"),
            new AwsRegions("us-east-1", "美国东部(弗吉尼亚北部) us-east-1"),
            new AwsRegions("us-east-2", "美国东部(俄亥俄州) us-east-2"),
            new AwsRegions("us-west-1", "美国西部(加利福尼亚北部) us-west-1"),
            new AwsRegions("us-west-2", "美国西部(俄勒冈州) us-west-2"),
            new AwsRegions("eu-west-1", "欧洲(爱尔兰) eu-west-1"),
            new AwsRegions("eu-west-2", "欧洲(伦敦) eu-west-2"),
            new AwsRegions("eu-west-3", "欧洲(巴黎) eu-west-3"),
            new AwsRegions("eu-central-1", "欧洲(法兰克福) eu-central-1"),
            new AwsRegions("eu-north-1", "欧洲(斯德哥尔摩) eu-north-1"),
            new AwsRegions("eu-south-1", "欧洲(米兰) eu-south-1"),
            new AwsRegions("ap-east-1", "亚太地区(香港) ap-east-1"),
            new AwsRegions("ap-south-1", "亚太地区(孟买) ap-south-1"),
            new AwsRegions("ap-southeast-1", "亚太地区(新加坡) ap-southeast-1"),
            new AwsRegions("ap-southeast-2", "亚太地区(悉尼) ap-southeast-2"),
            new AwsRegions("ap-southeast-3", "亚太地区(雅加达) ap-southeast-3"),
            new AwsRegions("ap-northeast-1", "亚太地区(东京) ap-northeast-1"),
            new AwsRegions("ap-northeast-2", "亚太地区(首尔) ap-northeast-2"),
            new AwsRegions("ap-northeast-3", "亚太地区(大阪) ap-northeast-3"),
            new AwsRegions("sa-east-1", "南美洲(圣保罗) sa-east-1"),
            new AwsRegions("cn-north-1", "中国(北京) cn-north-1"),
            new AwsRegions("cn-northwest-1", "中国(宁夏) cn-northwest-1"),
            new AwsRegions("ca-central-1", "加拿大(中部) ca-central-1"),
            new AwsRegions("me-south-1", "中东(巴林) me-south-1"),
            new AwsRegions("af-south-1", "非洲(开普敦) af-south-1"),
            new AwsRegions("us-iso-east-1", "美国ISO东部 us-iso-east-1"),
            new AwsRegions("us-isob-east-1", "美国ISOB东部（俄亥俄州）us-isob-east-1"),
            new AwsRegions("us-iso-west-1", "美国ISO西部 us-iso-west-1")
    );

    private final String name;
    private final String description;

    private AwsRegions(String name, String description) {
        this.name = name;
        this.description = description;
    }

    /**
     * The name of this region, used in the regions.xml file to identify it.
     */
    public String getName() {
        return name;
    }

    /**
     * Descriptive readable name for this region.
     */
    public String getDescription() {
        return description;
    }
}
