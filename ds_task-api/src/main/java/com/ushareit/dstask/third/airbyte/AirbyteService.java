package com.ushareit.dstask.third.airbyte;

import com.ushareit.dstask.third.airbyte.config.AirbyteCatalog;
import com.ushareit.dstask.third.airbyte.config.ConnectorSpecification;

/**
 * @author fengxiao
 * @date 2022/7/15
 */
public interface AirbyteService {

    /**
     * 获取镜像的 spec 信息
     *
     * @param imageName 镜像
     * @return spec 信息
     */
    ConnectorSpecification spec(String imageName) throws Exception;

    /**
     * 测试连接信息
     *
     * @param imageName            镜像
     * @param connectConfiguration 连接配置信息
     * @return true 能连接，false 不能连接
     */
    boolean check(String imageName, String connectConfiguration) throws Exception;

    /**
     * 获取数据源 catalog 信息
     *
     * @param imageName            镜像
     * @param connectConfiguration 连接配置信息
     * @return 数据源 catalog
     * @throws Exception
     */
    AirbyteCatalog discover(String imageName, String connectConfiguration) throws Exception;
}
