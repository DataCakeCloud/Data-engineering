package com.ushareit.dstask.web.interceptor;

import com.baomidou.mybatisplus.core.plugins.InterceptorIgnoreHelper;
import com.baomidou.mybatisplus.core.toolkit.ExceptionUtils;
import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import com.baomidou.mybatisplus.core.toolkit.TableNameParser;
import com.baomidou.mybatisplus.extension.plugins.handler.TableNameHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 动态表名
 *
 * @author jobob
 * @since 3.4.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DatacakeDynamicTableNameInterceptor implements InnerInterceptor {

    /**
     * 该方法废弃，避免多表使用统一策略多次注入，切换使用 TableNameHandler 自行判断表面处理逻辑
     * 该注入方法后续版本会移除
     */
    @Deprecated
    private Map<String, DataCakeTableNameHandler> tableNameHandlerMap;
    private DataCakeTableNameHandler tableNameHandler;

    @Override
    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
        PluginUtils.MPBoundSql mpBs = PluginUtils.mpBoundSql(boundSql);
        if (InterceptorIgnoreHelper.willIgnoreDynamicTableName(ms.getId())) return;
        String database = executor.getTransaction().getConnection().getCatalog();
        mpBs.sql(this.changeTable(database,mpBs.sql()));
    }

    @Override
    public void beforePrepare(StatementHandler sh, Connection connection, Integer transactionTimeout) {
        PluginUtils.MPStatementHandler mpSh = PluginUtils.mpStatementHandler(sh);
        MappedStatement ms = mpSh.mappedStatement();
        SqlCommandType sct = ms.getSqlCommandType();
        if (sct == SqlCommandType.INSERT || sct == SqlCommandType.UPDATE || sct == SqlCommandType.DELETE) {
            if (InterceptorIgnoreHelper.willIgnoreDynamicTableName(ms.getId())) return;
            PluginUtils.MPBoundSql mpBs = mpSh.mPBoundSql();

            String database = null;
            try {
                database = connection.getCatalog();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            mpBs.sql(this.changeTable(database,mpBs.sql()));
        }
    }

    protected String changeTable(String database, String sql) {
        TableNameParser parser = new TableNameParser(sql);
        List<TableNameParser.SqlToken> names = new ArrayList<>();
        parser.accept(names::add);
        StringBuilder builder = new StringBuilder();
        int last = 0;
        for (TableNameParser.SqlToken name : names) {
            int start = name.getStart();
            if (start != last) {
                builder.append(sql, last, start);
                String value = name.getValue();
                if (null == tableNameHandler) {
                    tableNameHandler = tableNameHandlerMap.get(value);
                }
                ExceptionUtils.throwMpe(null == tableNameHandler,"Please implement TableNameHandler processing logic");
                if (null != tableNameHandler) {
                    builder.append(tableNameHandler.dynamicTableName(database,sql, value));
                } else {
                    builder.append(value);
                }
            }
            last = name.getEnd();
        }
        if (last != sql.length()) {
            builder.append(sql.substring(last));
        }
        return builder.toString();
    }
}
