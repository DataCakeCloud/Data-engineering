package com.ushareit.dstask.validator;

import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.constant.TemplateEnum;
import com.ushareit.dstask.exception.ServiceException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author fengxiao
 * @date 2023/1/31
 */
@Getter
@AllArgsConstructor
public enum ValidType {

    BASE_PARAM_CHECK("基础参数校验", true, 2),
    HIVE_READ_PRI("校验lakehouse表的读权限", false, 2),
    HIVE_WRITE_PRI("校验lakehouse表的写权限", false, 2),
    HIVE_CREATE_PRI("校验创建lakehouse临时表的权限", false, 2),
    SHARE_STORE_PRI("校验目标sharestore的访问权限", false, 2),
    HIVE_PARTITION_FIELD("校验lakehouse表分区字段合法性", true, 2),
    SPARK_ADVANCED_PARAMS("校验spark高级参数设置的合法性", true, 2),
    SPARK_PARAMS("校验spark Args设置的合法性", true, 2),
    SPARK_SQL_SYNTAX("校验SQL语法合法性", true, 2),
//    OUTPUT_DATASET("校验生成数据集", false, 3),
    OUTPUT_DATASET("校验成功标识", false, 3),
    SUCCESS_FILE("校验成功标识", true, 3),
    PARTITION_FIELD_NOT_IN_TABLE_FIELD("目标表mysql的分区字段不能与表中已存在的字段重复", false, 2),
    PYTHON_SHELL("校验Python工件", true, 2),
    IMAGE_AND_CLUSTER_IN_SAME_CLOUD("校验镜像和集群在相同云", true, 2),
    FLINK_SQL_SYNTAX("校验 Flink SQL 语法", true, 2),
    DATABASE_CREATE_TABLE("校验lakehouse库创建表权限",true,2);

    public static final Map<TemplateEnum, List<ValidType>> TEMPLATE_VALIDATORS_MAP = new HashMap<TemplateEnum, List<ValidType>>() {
        private static final long serialVersionUID = -8153415229816067975L;

        {
            put(TemplateEnum.Mysql2Hive, Arrays.asList(BASE_PARAM_CHECK, HIVE_PARTITION_FIELD,
                     OUTPUT_DATASET));

            put(TemplateEnum.Hive2Hive, Arrays.asList(BASE_PARAM_CHECK, SPARK_SQL_SYNTAX,
                    SPARK_ADVANCED_PARAMS, OUTPUT_DATASET));

            put(TemplateEnum.DataMigration, Arrays.asList(BASE_PARAM_CHECK, HIVE_READ_PRI, HIVE_WRITE_PRI,
                    SPARK_ADVANCED_PARAMS, OUTPUT_DATASET));

            put(TemplateEnum.SPARKJAR, Arrays.asList(BASE_PARAM_CHECK, IMAGE_AND_CLUSTER_IN_SAME_CLOUD, SPARK_PARAMS,
                    SPARK_ADVANCED_PARAMS, OUTPUT_DATASET));

            put(TemplateEnum.Hive2Clickhouse, Arrays.asList(BASE_PARAM_CHECK, HIVE_READ_PRI, SPARK_SQL_SYNTAX,
                    SPARK_ADVANCED_PARAMS, OUTPUT_DATASET));

            put(TemplateEnum.Hive2Mysql, Arrays.asList(BASE_PARAM_CHECK, HIVE_READ_PRI, SPARK_SQL_SYNTAX,
                    SPARK_ADVANCED_PARAMS, OUTPUT_DATASET));

            put(TemplateEnum.Hive2Doris, Arrays.asList(BASE_PARAM_CHECK, SPARK_SQL_SYNTAX,
                    SPARK_ADVANCED_PARAMS, OUTPUT_DATASET));

            put(TemplateEnum.Hive2Redis, Arrays.asList(BASE_PARAM_CHECK, HIVE_READ_PRI, SPARK_SQL_SYNTAX,
                    SPARK_ADVANCED_PARAMS, OUTPUT_DATASET));

            put(TemplateEnum.Hive2Redshift, Arrays.asList(BASE_PARAM_CHECK, HIVE_CREATE_PRI,
                    SPARK_ADVANCED_PARAMS, OUTPUT_DATASET));

            put(TemplateEnum.Hive2Sharestore, Arrays.asList(BASE_PARAM_CHECK, HIVE_READ_PRI, SHARE_STORE_PRI,
                    SPARK_ADVANCED_PARAMS, OUTPUT_DATASET));

            put(TemplateEnum.PythonShell, Arrays.asList(BASE_PARAM_CHECK, IMAGE_AND_CLUSTER_IN_SAME_CLOUD,
                    OUTPUT_DATASET));
        }
    };

    /**
     * 校验项描述
     */
    private final String desc;

    /**
     * 是否为强校验（阻塞）
     */
    private final boolean mandatory;

    /**
     * 模板的第几步
     */
    private final int step;

    public static ValidType of(String type) {
        if (StringUtils.isBlank(type)) {
            throw new ServiceException(BaseResponseCodeEnum.SYS_ERR.name(), "校验类别不能为空");
        }

        for (ValidType validType : ValidType.values()) {
            if (StringUtils.equalsIgnoreCase(validType.name(), type)) {
                return validType;
            }
        }

        throw new ServiceException(type, type + ": 校验类别不存在");
    }

}
