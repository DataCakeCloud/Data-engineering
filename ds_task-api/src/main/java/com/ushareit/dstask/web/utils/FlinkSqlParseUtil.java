package com.ushareit.dstask.web.utils;

import org.apache.calcite.avatica.util.Casing;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.sql.parser.ddl.SqlCreateView;
import org.apache.flink.sql.parser.dml.RichSqlInsert;
import org.apache.flink.sql.parser.impl.FlinkSqlParserImpl;
import org.apache.flink.sql.parser.validate.FlinkSqlConformance;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.table.api.internal.TableEnvironmentImpl;
import org.apache.flink.table.catalog.CatalogManager;
import org.apache.flink.table.planner.calcite.FlinkPlannerImpl;
import org.apache.flink.table.planner.delegation.StreamPlanner;
import org.apache.flink.table.planner.operations.SqlToOperationConverter;

import java.util.ArrayList;
import java.util.List;

import static org.apache.calcite.avatica.util.Quoting.BACK_TICK;

/**
 * flink sql parse
 *
 * @author xuebotao
 * @date 2022-01-27
 */
public class FlinkSqlParseUtil {


    public static void parse(String sql) throws Exception {
        parseSql(beforeExec(sql));
    }

    public static String beforeExec(String sql) {
        String regex = "\n";
        String head = sql.substring(0, sql.indexOf(regex) + 1).trim();
        String body = sql.substring(sql.indexOf(regex) + 1).trim();
        if (head.startsWith("SET") && head.contains("flink.execution.packages")) {
            sql = head.replace("=", "='").replace(";", "';") + "\n" + body;
        }
        return sql;
    }

    public static void parseSql(String sql) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        EnvironmentSettings settings = EnvironmentSettings.newInstance()
                .useBlinkPlanner()
                .inStreamingMode()
                .build();
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env, settings);
        CatalogManager catalogManager = ((TableEnvironmentImpl) tableEnv).getCatalogManager();
        StreamPlanner planner = (StreamPlanner) ((TableEnvironmentImpl) tableEnv).getPlanner();
        //创建实例
        FlinkPlannerImpl flinkPlanner = planner.createFlinkPlanner();
        List<SqlNode> sqlNodes = chekGrammar(sql);
        for (SqlNode sqlNode : sqlNodes) {
            final SqlNode validated = flinkPlanner.validate(sqlNode);
            if (validated instanceof SqlCreateView || validated instanceof RichSqlInsert) {
                continue;
            }
            SqlToOperationConverter.convert(flinkPlanner, catalogManager, sqlNode);
        }
    }

    public static List<SqlNode> chekGrammar(String sql) throws Exception {
        List<SqlNode> sqlList = new ArrayList<>();
        if (StringUtils.isNotEmpty(sql)) {
            SqlParser parser = SqlParser.create(sql, SqlParser.configBuilder()
                    .setParserFactory(FlinkSqlParserImpl.FACTORY)
                    .setQuoting(BACK_TICK)
                    .setUnquotedCasing(Casing.TO_LOWER)
                    .setQuotedCasing(Casing.UNCHANGED)
                    .setConformance(FlinkSqlConformance.DEFAULT)
                    .build());
            List<SqlNode> sqlNodeList = parser.parseStmtList().getList();
            if (sqlNodeList != null && !sqlNodeList.isEmpty()) {
                for (SqlNode sqlNode : sqlNodeList) {
                    sqlList.add(sqlNode);
                }
            }
        }
        return sqlList;
    }
}
