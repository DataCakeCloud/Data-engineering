package com.ushareit.dstask.third.airbyte.connector;

import com.fasterxml.jackson.databind.JsonNode;
import com.ushareit.dstask.api.DataSourceQueryApi;
import com.ushareit.dstask.third.airbyte.connector.vo.ColumnInfo;

import java.util.List;
import java.util.Map;

/**
 * @author fengxiao
 * @date 2022/8/17
 */
public interface MySQLConnector {

    /**
     * 获取表的列表
     *
     * @param config actor 配置信息
     * @return 表列表
     */
    List<String> getTableList(JsonNode config, String type) throws Exception;

    /**
     * 获取字段集合
     *
     * @param actorId 数据实例ID
     * @param table   表
     * @return 字段结合
     */
    Map<String, ColumnInfo> getColumnMap(Integer actorId, String table, String type) throws Exception;

    /**
     * 获取字段集合
     *
     * @param config 配置信息
     * @param table  表
     * @return 字段结合
     */
    Map<String, ColumnInfo> getColumnMap(JsonNode config, String table, String type) throws Exception;

    /**
     * 查询 SQL 的返回结果
     *
     * @param config 配置信息
     * @param type   数据源类别
     * @param sql    查询 SQL
     */
    List<DataSourceQueryApi.Item> query(JsonNode config, String type, String sql, DataSourceQueryApi.QueryContext context) throws Exception;

    List<Map<String, String>> getTableSample(JsonNode config, String table, String metaType) throws Exception;
}
