package com.ushareit.dstask.web.interceptor;

/**
* 动态表名处理器
*
* @author licg
* @since 3.4.0
*/
public interface DataCakeTableNameHandler {
    /**
     * 生成动态表名
     *
     * @param sql       当前执行 SQL
     * @param tableName 表名
     * @return String
     */
    String dynamicTableName(String database, String sql, String tableName);
}
