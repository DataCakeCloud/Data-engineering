package com.ushareit.dstask.web.ddl;

import com.ushareit.dstask.web.factory.flink.param.RuntimeConfig;

/**
 * @author: licg
 * @create: 2021-05-12 15:24
 */
public interface IDdl {

    boolean verify()  throws Exception;

    String getDdl(RuntimeConfig runtimeConfig)  throws Exception;

    String getName()  throws Exception;

    String getSchema()  throws Exception;

    String getPartition()  throws Exception;

    String getInfo()  throws Exception;
}
