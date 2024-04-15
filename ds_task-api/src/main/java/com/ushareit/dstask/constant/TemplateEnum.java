package com.ushareit.dstask.constant;

import com.ushareit.dstask.exception.ServiceException;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.annotation.PublicEvolving;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@PublicEvolving
public enum TemplateEnum {
    Hive2Hive(TerminalCategory.ETL),
    DataMigration(TerminalCategory.ETL),
    SPARKJAR(TerminalCategory.ETL),
    Hive2Mysql(TerminalCategory.ExportWarehouse),
    Hive2Clickhouse(TerminalCategory.ExportWarehouse),
    Hive2Sharestore(TerminalCategory.ExportWarehouse),
    Hive2Doris(TerminalCategory.ExportWarehouse),
    Mysql2Hive(TerminalCategory.ImportWarehouse),
    Oracle2Hive(TerminalCategory.ImportWarehouse),
    SqlServerHive(TerminalCategory.ImportWarehouse),
    Doris2Hive(TerminalCategory.ImportWarehouse),
    StreamingSQL(TerminalCategory.STREAMING),
    Metis2Hive(TerminalCategory.STREAMING),
    MysqlCDC2Hive(TerminalCategory.STREAMING),
    Kafka2Clickhouse(TerminalCategory.STREAMING),
    Kafka2Hive(TerminalCategory.STREAMING),
    Db2Hive(TerminalCategory.STREAMING),
    StreamingJAR(TerminalCategory.STREAMING),
    PythonShell(TerminalCategory.Script),
    Hive2Redis(TerminalCategory.ExportWarehouse),
    Hive2Redshift(TerminalCategory.ExportWarehouse),
    MergeSmallFiles(TerminalCategory.MergeSmallFiles),
    TfJob(TerminalCategory.AI),
    Hive2File(TerminalCategory.ExportWarehouse),
    TrinoJob(TerminalCategory.ImportWarehouse),
    QueryEdit(TerminalCategory.ETL),
    File2Lakehouse(TerminalCategory.ExportWarehouse);
    public static List<TemplateEnum> templateEnumList = new ArrayList<>();
    public static Map<TerminalCategory, List<TemplateEnum>> templateEnumMap = new HashMap<>();

    static {
        templateEnumList.add(TemplateEnum.Db2Hive);
        templateEnumList.add(TemplateEnum.DataMigration);
        templateEnumList.add(TemplateEnum.SPARKJAR);
        templateEnumList.add(TemplateEnum.Hive2Mysql);
        templateEnumList.add(TemplateEnum.Hive2Doris);
        templateEnumList.add(TemplateEnum.Hive2Clickhouse);
        templateEnumList.add(TemplateEnum.Hive2Sharestore);
        templateEnumList.add(TemplateEnum.Mysql2Hive);
        templateEnumList.add(TemplateEnum.StreamingSQL);
        templateEnumList.add(TemplateEnum.Metis2Hive);
        templateEnumList.add(TemplateEnum.MysqlCDC2Hive);
        templateEnumList.add(TemplateEnum.Kafka2Clickhouse);
        templateEnumList.add(TemplateEnum.Kafka2Hive);
        templateEnumList.add(TemplateEnum.Db2Hive);
        templateEnumList.add(TemplateEnum.StreamingJAR);
        templateEnumList.add(TemplateEnum.PythonShell);
        templateEnumList.add(TemplateEnum.MergeSmallFiles);
        templateEnumList.add(TemplateEnum.TfJob);
        templateEnumList.add(TemplateEnum.Hive2File);
        templateEnumList.add(TemplateEnum.TrinoJob);
        templateEnumList.add(TemplateEnum.QueryEdit);
        templateEnumList.add(TemplateEnum.File2Lakehouse);
        for (TemplateEnum templateEnum : templateEnumList) {
            if (templateEnumMap.get(templateEnum.terminalCategory) != null) {
                templateEnumMap.get(templateEnum.terminalCategory).add(templateEnum);

            } else {
                List<TemplateEnum> templateEnums = new ArrayList<>();
                templateEnumMap.put(templateEnum.terminalCategory, templateEnums);
            }
        }
    }

    private final TerminalCategory terminalCategory;

    TemplateEnum(TerminalCategory terminalCategory) {
        this.terminalCategory = terminalCategory;
    }

    /**
     * 通过模板类型获取 模板code
     *
     * @return
     */
    public static List<TemplateEnum> getStreamingTemplate() {
        TerminalCategory code = TerminalCategory.STREAMING;
        if (templateEnumMap.containsKey(code)) {
            return templateEnumMap.get(code);
        }
        return new ArrayList<>();
    }

    /**
     * 是否可以预览sql的模板
     */
    public static boolean isOfflineSqlTemplate(String template) {
        switch (TemplateEnum.valueOf(template)) {
            case Hive2Hive:
            case Hive2Mysql:
            case Hive2Doris:
            case Hive2Clickhouse:
            case SPARKJAR:
            case PythonShell:
            case Hive2Redis:
            case Hive2Redshift:
            case Hive2File:
            case TfJob:
            case TrinoJob:
                return true;
            default:
                return false;
        }
    }

    /**
     * 是否可以查看kibana日志
     */
    public static boolean isScanKibana(String template) {
        switch (TemplateEnum.valueOf(template)) {
            case Hive2Sharestore:
            case Mysql2Hive:
            case Hive2Mysql:
            case Hive2Doris:
            case Hive2Clickhouse:
            case DataMigration:
            case Hive2Hive:
            case SPARKJAR:
            case MergeSmallFiles:
            case Hive2Redis:
            case Hive2Redshift:
            case TfJob:
            case Hive2File:
            case TrinoJob:
                return true;
            default:
                return false;
        }
    }

    /**
     * 是否可以查看spark ui日志
     */
    public static boolean isScanSparkUI(String template) {
        switch (TemplateEnum.valueOf(template)) {
            case Hive2Sharestore:
            case Mysql2Hive:
            case Hive2Mysql:
            case Hive2Doris:
            case Hive2Clickhouse:
            case DataMigration:
            case Hive2Hive:
            case SPARKJAR:
            case Hive2Redis:
            case Hive2File:
                return true;
            default:
                return false;
        }
    }

    /**
     * 是否流式模板
     *
     * @return
     */
    public boolean isStreamingTemplate() {
        return terminalCategory == TerminalCategory.STREAMING;
    }

    private enum TerminalCategory {
        STREAMING,
        ETL,
        ImportWarehouse,
        ExportWarehouse,
        Script,
        MergeSmallFiles,
        AI
        }

    public static TemplateEnum of(String code) {
        if (StringUtils.isBlank(code)) {
            throw new ServiceException(BaseResponseCodeEnum.ENUM_TYPE_NOT_EXIST_ERROR.name(), "模板类型不能为空");
        }

        for (TemplateEnum templateEnum : TemplateEnum.values()) {
            if (StringUtils.equalsIgnoreCase(templateEnum.name(), code)) {
                return templateEnum;
            }
        }

        throw new ServiceException(BaseResponseCodeEnum.ENUM_TYPE_NOT_EXIST_ERROR.name(), "不支持的模板类型");
    }
}