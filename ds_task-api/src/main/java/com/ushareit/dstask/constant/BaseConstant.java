package com.ushareit.dstask.constant;

import com.google.common.collect.Lists;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @program: hebe
 * @description:
 * @author: wuyan
 * @create: 2020-04-30 20:34
 **/
public class BaseConstant {
    public static final Integer ADMINMENUID = 110000;
    public static final Integer SUPPERADMINMENUID = 110001;
    public static final String SEPERATOR = File.separator;

    /**
     * windows系统下，jar下载存储路径
     */
    public static String JAR_DOWNLOAD_DIR = "C:/develop/obs/";


    /**
     * response
     */
    public static final String RES_CODE = "code";
    public static final String RES_MESSAGE = "message";
    public static final String RES_DATA = "data";

    public static final String NODATA="-";

    public static final Double LITTLE=0.01;

    public static final String EMPTY="EMPTY";

    public static final String AIRFLOW="airflow";

    public static final String EMPTY2="-";

    public static final List<String> PUS=  new ArrayList<>();

    public static final BigDecimal HUNDRED=new BigDecimal("100");

    public static final String MINOR_ROLE = "homepage";
    public static final String COMMON_ROLE = "common";
    public static final String defaultTenantName="ninebot";
}