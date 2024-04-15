package com.ushareit.dstask.constant;

import java.text.MessageFormat;

/**
 * 公共域 服务响应码
 *
 * @author wuyan
 * @date 2020/05/14
 */
public enum BaseResponseCodeEnum {
    /**
     * 系统类响应
     */
    SYS_ERR("系统错误"),
    INFO("提醒"),
    WARN("告警"),
    SYS_DB_CONN("数据库连接失败"),
    SYS_UNA("服务不可用"),
    HTTP_ERR("http请求异常"),
    HTTP_CLOSE_ERR("http关闭异常"),
    SYS_DEGRADE("服务降级"),

    /**
     * 客户端类common响应
     */
    CLI_PARAM_ILLEGAL("参数非法"),
    CLI_ID_NOTNULL("ID不能为空"),
    CLI_DELETE_ILLEGAL("无效的删除对象，请继承DeleteEntity"),
    NOT_SUPPORT_REGION("不支持此区域"),

    CLI_SAVE_DB_SUCCESS("数据库保存成功"),
    CLI_UPDATE_DB_SUCCESS("数据库更新成功"),
    CLI_INIT_DB_FAIL("数据库初始化失败"),
    CLI_SAVE_DB_FAIL("数据库保存失败"),
    CLI_UPDATE_DB_FAIL("数据库更新失败"),
    CLI_PARAM_REQUIRED("参数不能为空"),
    USER_GROUP_DEATIL_IS_NULL("用户组信息为空,请添加用户组信息"),

    /**
     * 登录类型
     */
    FAILED_UPDATE("任务更新失败"),
    TASK_DELETE("任务已被删除，请刷新列表"),
    INVALID_REGION("region与运行集群不匹配"),

    USER_NOT_FOUND("用户不存在"),
    DATA_NOT_FOUND("数据不存在"),
    ROLE_EXSITTS("账号名称已存在"),
    ROLE_NOT_FOUND("用户角色表未初始化，请联系管理员"),
    USER_OR_PASSWORD_ERROR("用户名或密码错误，请重新输入"),
    SQL_DECODE_FAIL("获取表信息失败，请检查表是否有可读权限"),
    NOT_ADMIN("非管理员角色，不允许操作"),
    NOT_ROOT("非ROOT角色，不允许操作"),
    DELETE_FAIL("删除失败"),
    CLI_IDENTIFY_CODE_ERROR("验证码错误"),
    ENUM_TYPE_NOT_EXIST_ERROR("枚举类型不存在"),


    SUCCESS(0,"成功"),
    TEMPLATE_DEP_NOT_FOUND("模板依赖未初始化"),
    LOAP_GATE_WAY_NOT_FOUND("loapGetWay信息未配置"),
    CLUSTER_NS_NOT_FOUND("集群未配置namespace"),

    REQUEST_ILLEGAL("非法请求"),
    NAME_IS_NOT_UNIQUE("名字重复，请重新更换"),
    EMAIL_IS_NOT_UNIQUE("邮箱重复，请重新更换"),
    TASK_IS_DELETED("保存提示：不能将已删除的任务作为前置依赖"),
    TASK_NAME_IS_NOT_UNIQUE("任务重复，请重新更换"),
    WORKFLOW_NAME_IS_NOT_UNIQUE("工作流重复，请重新更换"),
    FIlE_UPLOAD_FAIL("文件上传失败"),
    FILE_IS_NOT_NULL("上传文件不能为空"),
    NAME_NOT_MATCH("名字不匹配【字母开头，只包含a-z,A-Z,0-9或-】，长度2-64"),
    STREAM_TASK_NAME_NOT_MATCH("名字不匹配【字母开头，只包含a-z,A-Z,0-9或-】，长度2-45"),
    WORKFLOW_NAME_NOT_MATCH("名字不匹配【只支持英文字符、-、_、数字，必须以字母开头】，长度不超过100个字符"),
    OFFLINE_TASK_NAME_NOT_MATCH("名字不匹配【只包含a-z,A-Z,0-9或_或-】，长度至少1个"),
    LABEL_NAME_NOT_MATCH("名字不匹配【字母开头，只包含a-z,A-Z,0-9或_或中文】，长度不超过128"),
    NAME_BLANK("名字不能为空"),
    SQL_PARSE_EXCEPTION("sql解析异常"),
    COLUMN_BLANK("列内容不能为空"),
    STATE_PATH_NOT_MATCH("state path不匹配，请参考示例"),
    NO_GROUP("未获取到您的所属部门，请通过“钉钉工作台--->OA审批--->SHAREit--->DataStudio权限申请”进行申请"),
    ONLINE_JOB_CANNOT_UPDATE_NAME("上线跑批任务，无法修改任务名，需先下线。"),
    ONLINE_WORKFLOW_CANNOT_DELETE("上线工作流无法删除，需先下线。"),
    RENAME_WORKFLOW_MUST_ONLINED("曾经上线过的工作流才能修改名字"),
    RENAME_TASK_MUST_ONLINED("曾经上线过的任务才能修改名字"),
    DATASET_TYPE_FAULT("错误的type类型"),
    TASK_NOT_EXISTS("任务不存在"),
    NOT_SUPPORT_STORAGE_TYPE("不支持当前数据源类型"),
    /**
     * 登录类型
     */
    NO_AUTH("无法更新数据库，租户校验失败"),
    NO_LOGIN("未登录或登录过期，请先登录"),
    NO_RIGHT("无权限，请检查用户所在组"),
    NO_STARTED_JOB("数据库中不存在started的job"),

    /**
     * 集群类响应
     */
    CLUSTER_NOT_AVAIL("集群不可用"),
    CLUSTER_AVAIL_SLOT_NOT_ENOUGH("集群可用资源量不足"),
    ARTIFACT_TYPE_NOT_MATCH("工件类型不匹配"),
    ARTIFACT_MODE_NOT_MATCH("工件模式不匹配"),
    CLUSTER_TYPE_NOT_MATCH("集群类型不匹配"),
    CLUSTER_VERSION_NOT_MATCH("自动伸缩模式不支持Flink1.13.0以下版本"),
    CLUSTER_ADDRESS_NOT_MATCH("集群地址不匹配"),
    ARTIFACT_REGION_IS_NULL("工件region为空"),
    ARTIFACT_NOT_ACCESS("没有权限操作当前工件"),

    /**
     * 应用类响应
     */
    APP_START_SUCCESS("启动应用成功"),
    APP_START_FAIL("启动任务失败"),
    HIVE_TABLE_FAIL("目标表创建失败"),
    TASK_CHECK_FAIL("校验任务失败"),
    APP_IS_CANCELING_OR_SUSPENDING("当前应用已触发取消或停止请求"),
    APP_IS_DELETE("当前应用已被删除"),
    APP_IS_TERMINAL("当前应用处于最终状态"),
    APP_IS_RUNNING("当前应用处于RUNNING状态"),
    APP_HAS_ALIVE_JOB("数据库中存在非最终状态的job"),
    APP_SAVE_SUCCESS("应用创建成功"),
    APP_SAVE_FAIL("应用创建失败"),
    APP_DELETE_SUCCESS("应用删除成功"),
    APP_CANCEL_SUCCESS("应用终止成功"),
    APP_UPDATE_SUCCESS("应用更新成功"),
    APP_TAG_SUCCESS("应用打标签成功"),
    APP_RELEASE_SUCCESS("启动发布成功"),
    MAIN_CLASS_NOT_MATCH("主类不匹配"),
    APP_TYPE_NOT_MATCH("应用类型不匹配"),
    APP_IAM_NO_RIGHT("应用IAM账号权限不足，请联系运维确认"),
    APP_CHECK_NO_RIGHT("应用内容校验未通过，请检查"),
    APP_DEBUG_NO_RIGHT("应用内容调试失败，请检查"),
    APP_DEBUG_NO_RESULT("应用内容调试失败，无返回值"),
    NO_DEBUG("无debug任务"),
    APP_STOPDEBUG_SUCCESS("应用终止调试成功"),
    APP_CLUSTER_VERSION_NO_MATCH("Session独享模式1.11.0版本以上，才支持Sql任务！"),
    APP_ARTIFACTID_ARTIVERSIONID_NO_MATCH("工件ID和工件版本ID未一一对应"),
    GET_TABLES_FAIL("获取sql中source和sink表失败"),
    SINK_NOT_EXIST("sink不存在"),
    TASK_IS_NOT_STREAMING_MODE("非流模式不支持取消"),
    TASK_MUTUAL_INCLUDE("两个任务相互依赖"),
    BATCH_UPDATE_ROLE_FAILED("批量更新任务角色失败"),
    TASK_NOT_SUPPORT_AUROSCALE("当前任务不支持自动伸缩taskmanager"),
    TASKMANAGER_NUM_GREATER_THAN_ZERO("tm要扩容或缩容的数量必须大于0"),
    OUTPUT_DATASET_EXIST("已有任务产出相同的数据集，请及时更换当前任务产出数据集"),
    TASK_VERSION_FOUND("切换指定版本不存在"),
    CHECK_FAIL("校验失败"),
    GET_STORAGE_DETAIL_FAILED("获取存储文件schema信息失败"),

    FILE_FORMAT_ERROR("文件格式有误，请您确认："),
    TASK_OUTPUT_ERROR("有任务生成相同的SUCCESS！任务id"),
    TASK_OUTPUT_DATASET_ERROR("与任务【%s】产出了相同的数据集，请及时更换当前任务产出的数据集"),
    GET_SEATUNNEL_JOB_SHELL_FAIL("生成任务执行命令失败"),
    UPDATE_FAIL("更新失败，数据不在当前组，请先切换用户组"),
    SELECT_FAIL(303, "数据不在当前组，请先切换用户组"),

    WECHAT_REPEAT("该用户组下的微信报警名重复，请修改"),
    WECHAT_NO_EXIST("该微信报警信息不存在"),
    DATABASE_NO_EXIST("数据审计时需要数据库名字"),

    /**
     * sharestore的校验
     */
    SHARESTORE_RUN_PARAM_NOT_NULL("运行时参数不能为空"),
    SHARESTORE_ADDRESS_NOT_NULL("ShareStore 连接地址不能为空"),
    SHARESTORE_CLUSTER_NOT_NULL("ShareStore集群名称不能为空"),
    SHARESTORE_SEGMENT_NOT_NULL("ShareStore 目标表名不能为空"),
    SHARESTORE_SEGMENT_NOT_FOUND("表 %s 在集群 %s 中不存在，请联系 ShareStore 管理员创建"),


    SHARESTORE_CONNECTION_FAIL("访问 ShareStore 服务失败"),
    SHARESTORE_NOT_GET_SEGMENT("无法获取 %s.%s 的分片信息"),
    SHARESTORE_NETWORK_FAIL("访问 %s 网络不通"),
    SHARESTORE_VISIT_FAIL("访问 %s 报错"),


    /**
     * 对hive表的校验
     */
    HIVE_READ_NO_PRIVILEGE("用户没有该lakehouse表的读权限"),
    HIVE_WRITE_NO_PRIVILEGE("用户没有该lakehouse表的写权限"),
    HIVE_CREATE_NO_PRIVILEGE("用户没有在该库下创建lakehouse表的权限"),
    PARTITION_NO_FOUND_SOURCE("目标表分区字段名称不能在源表schema中存在"),
    NO_SPARKJAR_ARGS_CHECK("非sparkJar模版校验args参数"),
    ARGS_LINE_BREAK_ERROR("Args参数中换行符请使用 \\ "),
    JINJA_ERROR("分区Jinja表达式错误"),
    IRREGULAR_SCHEDULER_PARAM_ERROR("不定时调度参数{{%s}}错误,请修改"),
    PARTITION_NEED_EQUAL_SIGN("分区信息的字段后面需要="),
    PARTITION_NO_EMPTY("分区信息的字段与值均不能为空"),
    NOTE_ERROR("注释符错误"),
    PARTITION_FIELD_NOT_IN_TABLE_FIELD("目标表mysql的分区字段不能与表中已存在的字段重复"),

    /**
     * Job类响应
     */
    JOB_CANCEL_FAIL("取消flink的job失败"),
    JOB_CHECK_FAIL("flink任务check失败"),
    JOB_CANCEL_SUCCESS("取消flink的job成功"),
    JOB_UPLOAD_FlINK_JAR_NULL("上传flink JAR为空"),
    JOB_UPLOAD_FlINK_JAR_FAIL("上传flink JAR失败"),
    JOB_DELETE_FLINK_JAR_FAIL("删除flink集群上的jar失败"),
    JOB_TRIGGER_SAVEPOINT_SUCCESS("触发flink集群上job的保存点成功"),
    JOB_TRIGGER_SAVEPOINT_FAIL("触发flink集群上job的保存点失败"),
    ONLINE_AND_OFFLINE_FAIL("上下线离线任务失败"),
    ANNOUNCEMENT_IS_ONLINE("公告已上线"),
    ANNOUNCEMENT_IS_OFFLINE("公告已下线"),
    TASK_INSTANCE_STOP_FAIL("离线任务停止失败"),
    TASK_INSTANCE_BATCH_STOP_FAIL("离线任务批量停止失败"),
    GET_BACKFILL_STATUS_FAIL("获取深度补数任务状态失败"),
    JOB_SUBMIT_TO_FLINK_SUCCESS("提交job到flink集群成功"),
    JOB_SUBMIT_TO_FLINK_FAIL("提交job到flink集群失败"),
    JOB_SUBMIT_TO_LOCAL_FAIL("提交job到本地失败"),
    JOB_RESULT_PARSE_FAIL("任务失败"),
    JOB_STOP_WITH_SAVEPOINT_FAIL("停止并触发保存点失败"),
    TASK_ILLEGEL_PARAMS("离线任务存在非法参数"),
    DB_TO_HIVE_PARAM_ILLEGAL("db2lakehouse模板中参数异常"),
    CANT_GET_STREAMING_CMD("实时任务不支持获取command"),
    /**
     * Template类响应
     */
    TEMPLATE_DELETE_SUCCESS("模板删除成功"),
    TEMPLATE_GROUP_NOTNULL("group不能为空"),


    DOWNLOAD_FAIL("下载失败"),

    USER_HAD_ADDED("用户已添加，请勿重复添加"),

    /**
     * OBS类响应
     */
    OBS_COPY_FAIL("从obs上复制jar包失败"),
    OBS_DELETE_FAIL("OBS上删除对象失败"),
    OBS_DOWNLOAD_FAIL("从OBS下载失败"),
    OBS_UPLOAD_FAIL("更新jar包到OBS失败"),
    OBS_CREATED_DIR_FAIL("OBS上创建目录失败"),
    OBS_URL_NOT_EXIST("OBS路径不存在"),
    OBS_DOWNLOAD_FILE_ERR("填入路径有误，请检查路径"),
    OBS_DOWNLOAD_PARAM("获取git项目或者文件不能为空"),
    OBS_DOWNLOAD_PARAM_FILE("获取的必须是一个文件"),

    /**
     * git下载
     */
    GIT_FILE_CONTECT_ERR("获取的GIT内容不规范"),

    EXIST_CHILD_MENU("该菜单下存在子菜单，请先删除所有子菜单"),



    /**
     * GCS类响应
     */
    GCS_COPY_FAIL("从GCS上复制jar包失败"),
    GCS_DELETE_FAIL("GCS上删除对象失败"),
    GCS_DOWNLOAD_FAIL("从GCS下载失败"),
    GCS_UPLOAD_FAIL("更新jar包到GCS失败"),
    GCS_LIST_FAIL("列出GCS对象失败"),
    GCS_CREATED_DIR_FAIL("GCS上创建目录失败"),
    GCS_URL_NOT_EXIST("GCS路径不存在"),

    /**
     * 工单反馈
     */
    FEEDBACK_MESSAGE_ERR("反馈或者回复信息不能为空"),
    FEEDBACK_URL_ERR("附件的URL为空或者URL不规范"),
    FEEDBACK_ID_ERR("查询工单ID不能为空"),
    FEEDBACK_ASSIGN_REASON_ERR("转让原因不能为空"),
    FEEDBACK_EXPORT_MESSAGE_ERR("工单导出信息为空"),
    FEEDBACK_EXPORT_MESSAGE_FAILURE("工单导出失败"),
    FEEDBACK_USER_MESSAGE_FAILURE("只有提交人、处理人、值班人才能发送消息"),
    FEEDBACK_USER_UPDATE_FAILURE("只有提交人、处理人、值班人或管理员才能修改，请核实"),
    FEEDBACK_USER_REOPEN_FAILURE("只有创建人及受理人才能重开工单"),


    /**
     * 数据源信息获取
     */
    META_DATA_SCHEMA_GET_FAILURE(30010001,"元数据表信息获取失败"),
    META_DATA_PASSWORD_ENCRYPT_FAILURE(30010002,"元数据密码加密失败"),
    ENCRYPT_FAILURE("密码加密失败"),
    NOT_OPERATION_TABLE_PERMISSION("没有操作当前表权限，请先申请"),
    LAKECAT_METADATA_GET_FAILURE("lakecat元数据获取失败"),
    CANNOT_DELETE_DATA_SCHEMA("有任务依赖该数据源，不能删除"),


    /**
     * AWS类响应
     */
    AWS_COPY_FAIL("从AWS上复制jar包失败"),
    AWS_DELETE_FAIL("AWS上删除对象失败"),
    AWS_DOWNLOAD_FAIL("从AWS下载失败"),
    AWS_UPLOAD_FAIL("更新jar包到AWS失败"),
    AWS_CREATED_DIR_FAIL("AWS上创建目录失败"),
    AWS_URL_NOT_EXIST("AWS路径不存在"),
    AWS_FILE_CONVERSION_FAIL("AWS文件转化失败"),
    KS_CONNECTION_FAIL("KS3连接失败"),
    KS_FILE_CONVERSION_FAIL("KS文件转化失败"),
    KS3_DOWNLOAD_FAIL("从KS3下载失败"),
    KS3_DELETE_FAIL("从KS3删除对象失败"),
    KS3_NO_ACCESS_SECRET("获取不到KS3的key或秘钥"),

    /**
     * statistic响应类
     */
    TIMESTAMP_TO_DATE_FAIl("时间戳转换成date失败"),

    /**
     * 标签收藏响应
     */
    LABEL_COLLECTED("标签已经收藏"),
    LABEL_CAN_NOT_CANCEL_COLLECTED("标签无法取消收藏，因为未收藏过"),
    NEVEER_COLLECTED("此用户从未收藏过，无法取消收藏"),

    /**
     * rest api 相应码
     */
    CREATED("已创建"),
    DELETED ("已删除"),
    UPDATED_ALL("已更新-ALL"),
    UPDATED("已更新"),
    QUERY("查询已返回"),
    UNKOWN("未识别访问类型"),

    /**
     * scmp
     */
    SCMP_GET_IAM_FAIL("获取IAM账户失败"),

    /**
     * IT
     */
    IT_GET_TOKEN_FAIL("获取iTtoken失败"),
    IT_GET_USER_FAIL("获取iTusers失败"),
    IT_BATCH_GET_USER_INFO_FAIL("批量获取iTusers失败"),
    IT_GET_DEPARTMENT_LIST_FAIL("获取部门列表失败"),
    IT_GET_DEPT_INFO_FAIL("获取部门基本信息失败"),
    IT_GET_SUBORDINATE_INFO_FAIL("获取下属基本信息失败"),
    /**
     * BI
     */
    BI_NO_PERMISSION("没有看板权限"),
    BI_GET_DASHBOARD_FAIL("获取看板失败"),
    BI_ADD_PERMISSION_FAIL("申请ds看板权限失败"),
    /**
     * compile
     */
    FILE_COMPILE_FAIL("文件编译失败"),

    /**
     * scheduler
     */
    SCHEDULER_RESQUEST_FAIL("请求调度模块失败"),
    SCHEDULER_RESPONSE_WARN("调度模块提示信息"),
    SCHEDULER_RESPONSE_ERR("调度模块响应错误"),
    SCHEDULER_START_ERR("启动任务失败, 调度模块响应错误"),

    TABLE_PARAM_IS_NULL("获取DDL失败，表参数不能为空"),

    /**
     * sparksql解析响应
     */
    SPARKSQL_JINJA_DATA_ERR("存在错误的日期参数"),
    SPARLSQL_SQL_PARSE_ERR("sql存在语法错误"),
    SPARKSQL_MISSING_PARAM("缺少用户组、地区参数"),

    /**
     * ui
     */
    UI_GET_FAIL("ui服务尚未启动，请稍后再试"),

    /**
     * 用户组织架构
     */
    DELETE_USER_GROUP_FAIL_EXIST_GROUP("删除用户组失败，组中存在用户"),
    DELETE_GROUP_FAIL_EXIST_GROUP("删除用户组失败，组中存在子组"),
    DELETE_USER_GROUP_FAIL_EXIST_TASK("删除用户组失败，组中存在关联任务"),
    USER_EMAIL_ERROR("用户邮箱无效，请检查"),
    USER_EMAIL_FORMAT_CHECK_FAIL("邮箱格式校验失败"),
    USER_LOGIN_INFO_NOT_NULL("用户登录信息不能为空"),
    USER_NOT_EXISTS("当前用户已删除或已冻结，请联系管理员"),
    USER_NOT_EXIST("当前用户不存在，请检查"),
    USER_PASSWORD_ERROR("密码不正确，请重新输入"),
    USER_TOKEN_CREATE_FAIL("用户token生成失败"),
    USER_TOKEN_PARSE_FAIL("用户token解析失败"),
    USER_TOKEN_EXPIRED("用户token已过期"),
    TENANT_NOT_EXISTS("当前租户不存在"),
    USER_MFA_SECRET_NULL("用户MFA没有绑定，请您先绑定"),
    USER_MFA_ERROR("MFA CODE不正确，请重新输入"),
    USER_NO_GROUP("该用户没有分配用户组，请联系管理员"),
    LDAP_EXCEPTION("登录异常，请联系管理员"),
    LDAP_USER_ERROR("用户名异常"),
    DATASOURCE_CONFIG_IS_EXIST("数据源配置信息已存在"),

    /**
     * 值班
     */
    DUTY_NOT_INIT("值班信息未初始化"),


    /**
     * 参数校验
     */
    SPARK_PARAM_CHECK_FAIL("spark高级设置参数校验失败"),
    SPARK_SQL_CHECK_FAIL("校验不通过"),

    /**
     * 离线任务实例响应
     */
    NOT_SQL_TYPE_TAS("不是sql类型任务"),
    NOT_HAVE_SPARK_UI("SparkUI不存在"),
    /**
     * 离线任务校验
     */
    OUTPUT_GRA_MORE_THAN_DATA_DEPEND("在数据依赖上游中,要求依赖上游的最小周期与产出数据周期相同"),
    GRA_DIFF("工作流中任务的调度时间粒度不一致"),
    OUTPUT_GRA_MORE_THAN_EVENT_DEPEND("在事件依赖上游中,要求依赖上游的最小周期与产出数据周期相同"),
    BAD_SHORTURL("无效的短链地址"),

    SEATUNNEL_CONFIG_ANALYZE_FAIL("SEATUNNEL解析命令失败"),
    SEATUNNEL_DATASOURCE_NULL("数据源ID不能为空"),


    /**
     * ak sk
     */
    AK_SK_INVALID("无效的akSk"),
    TOKEN_INVALID("无效的token"),
    /**
     * lakeclient
     */
    LAKECLIENT_ADD_SOURCE("lake增加数据源失败"),
    LAKECLIENT_NO_SOURCE_UPDATE("您没有编辑数据源的权限"),
    LAKECLIENT_NO_SOURCE_DELETE("您没有删除数据源的权限"),

    /**
     * Seatunnel
     */
    SEATUNNEL_WRITE_FILE_FAIL("seatunnel写文件异常"),

    /**
     * 文件管理
     */
    FM_CANT_DEL_ROOT_FOLDER("不能删除根文件夹"),
    FM_CANT_DEL_CONTAINS_SUB_FOLDER("无法直接删除包含子文件的文件夹"),
    FM_MODULE_ERROR("模块【%s】不支持添加文件或文件夹"),
    FM_MOVE_CYCLE_ERROR("任何文件夹都无法移动到其子文件夹中"),

    USERGROUP_EXIST("用户组名称已存在"),
    TASKFOLDER_EXIST("文件夹已经存在"),
    QIWEI_MESSAGE_SEND_FAIL("企微消息发送失败"),
    USERGROUP_API_FAIL("调用创建用户组接口创建失败"),
    ;


    private Integer code;
    private String message;
    private String suggest;
    private Boolean msgIgnore = false;

    BaseResponseCodeEnum(String message) {
        this.code = 500;
        this.message = message;
    }

    BaseResponseCodeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
        this.suggest = "";
        this.msgIgnore = true;
    }

    BaseResponseCodeEnum(Integer code, String message,String suggest) {
        this.code = code;
        this.message = message;
        this.suggest = suggest;
        this.msgIgnore = true;
    }

    BaseResponseCodeEnum(Integer code, String message,String suggest,Boolean msgIgnore) {
        this.code = code;
        this.message = message;
        this.suggest = suggest;
        this.msgIgnore = msgIgnore;
    }

    public Integer getCode() {
        return code;
    }
    public String getMessage() {
        return message;
    }
    public String getSuggest() {
        return suggest;
    }

    public Boolean getMsgIgnore() {
        return msgIgnore;
    }

    @Override
    public String toString() {
        return MessageFormat.format("ResponseCode:{0},{1}.", this.name(), this.message);
    }

}
