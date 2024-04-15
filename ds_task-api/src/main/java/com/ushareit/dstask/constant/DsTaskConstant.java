package com.ushareit.dstask.constant;

/**
 * @author wuyan
 * @date 2020/05/11
 */
public class DsTaskConstant extends CommonConstant {
    public static final String LOG_TRACE_ID = "traceId";
    public static final String LOG_TENANT_NAME = "tenantName";
    /**
     * 命名规范
     */
    public static final String CLUSTER_NAME_PATTERN = "[A-Za-z0-9_\\-]{2,64}";
    public static final String APPLICATION_NAME_PATTERN = "[a-zA-Z]([-a-zA-Z0-9]*[a-zA-Z0-9])?(\\.[a-zA-Z0-9]([-a-zA-Z0-9]*[a-zA-Z0-9])?)*";
    public static final String ARTIFACT_NAME_PATTERN = "^[a-z0-9.A-Z_-]{1,200}$";
    public static final String STREAM_TASK_NAME_PATTERN = "[a-zA-Z]([-a-zA-Z0-9]*[a-zA-Z0-9])?(\\.[a-zA-Z0-9]([-a-zA-Z0-9]*[a-zA-Z0-9])?)*{2,45}";
    public static final String TENANT_NAME_PATTERN = "[A-Za-z0-9\\_]{2,64}";

    public static final String WORKFLOW_NAME_PATTERN = "[A-Za-z0-9_\\-]{2,100}";
    public static final String CLUSTER_ADDRESS_PATTERN = "(https?|ftp|file)://[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]";

    public static final String LABEL_NAME_PATTERN = "[A-Za-z0-9_\\u4e00-\\u9fa5]{2,128}";
    public static final String SCHEDULED_APPLICATION_NAME_PATTERN = "^[0-9a-zA-Z_-]{1,}$";
    public static final String GCS_ADDRESS_PATTERN = "(https://www.googleapis.com/storage/v1/b/([-A-Za-z0-9_]+)/o/)((.*)/(.+))";
    public static final String OBS_ADDRESS_PATTERN = "(https://([-A-Za-z0-9_]+).obs.ap-southeast-3.myhuaweicloud.com)(:[a-zA-Z0-9]*)?/(.*)/(.+)";
    public static final String GPS_PATH_PATTERN = "(gs)://([^\\/]*)/(.*)";

    public static final String APPLICATION_MAIN_CLASS = "^(([a-zA-Z]|[a-zA-Z][a-zA-Z0-9_\\-]*[a-zA-Z0-9])\\.)*([A-Za-z]|[A-Za-z][A-Za-z0-9_\\-]*[A-Za-z0-9])$";
    public static final String OBS_AWS_PATH_PATTERN = "(obs|s3a|s3|gs|ks3)://([^\\/]*)/(.*)";
    public static final String FLINK_SQL_SET_PATTERN = "SET((\\s+flink.execution.packages\\s*)=(.*))?;";
    public static final String CLASS_NAME_PATTERN = "(.*)class ([-A-Za-z0-9_]+) extends(.*)";
    public static final String FLINK_SQL_DEPENDENCE_PATTERN = "[.A-Za-z0-9_\\-]*:[.A-Za-z0-9_\\-]*:[.A-Za-z0-9_\\-]*";
    public static final String JDBC_URL_PATTERN = "jdbc:mysql://([.A-Za-z0-9_\\-]*):([0-9]*)/([A-Za-z0-9_\\-]*)?.*";
    public static final String EMAIL_PATTERN = "\\w+@\\w+(\\.\\w+)+";
    public static final String TEMPLATE_PARAM_PATTERN = "\\{\\{[^\\{\\}]+\\}\\}";

    public static final String AWS_ADDRESS_PATTERN_NEW = "(https://([-A-Za-z0-9_.]+).s3)(.amazonaws.com)((:[a-zA-Z0-9]*)?/(.*)/(.+))";
    public static final String AWS_ADDRESS_PATTERN = "(https://([-A-Za-z0-9_.]+).s3.([-A-Za-z0-9_.]+).amazonaws.com)(:[a-zA-Z0-9]*)?/(.*)/(.+)";
    public static final String KS3_ADDRESS_PATTERN = "(http://([-A-Za-z0-9_.]+).ks3-([-A-Za-z0-9_.]+).ksyuncs.com)(:[a-zA-Z0-9]*)?/(.*)/(.+)";
    public static final String OBS_ADDRESS_PATTERN_NEW = "(https://([-A-Za-z0-9_.]+).obs.([-A-Za-z0-9_]+).myhuaweicloud.com)(:[a-zA-Z0-9]*)?/(.*)/(.+)";

    public static final String AWS_OR_OBS_ADDRESS_PATTERN = "(https://([-A-Za-z0-9_.]+).(s3|obs).([-A-Za-z0-9_.]+).(amazonaws.com|myhuaweicloud.com))(:[a-zA-Z0-9]*)?/(.*)/(.+)";

    /**
     * 登录鉴权方式
     */
    public static final String LOGIN_MODE = "LDAP";

    public static final String EMAIL_SUFFIX = "@ninebot.com";
    public static final String SHAREIT_EMAIL = "@ushareit.com";
    /**
     * 字典表根字段
     */
    public static final String TEMPLATE_TYPE = "TEMPLATE_TYPE";

    public static final Boolean SWITCH_TO_NEW_ACCESS = true;

    public static final String TEMPLATE_DEP = "TEMPLATE_DEP";

    /**
     * 集群常量
     */
    public static final Integer MAX_JOB_CREATION_ATTEMPTS = 1;
    public static final Boolean ALLOW_NON_RESTORED_STATE = true;
    /**
     * event相关
     */
    public static final String APPLICATION_ID = "applicationId";
    public static final String ARTIFACT_ID = "artifactId";
    public static final String OBJECT_ID = "id";
    public static final String OBJECT_NAME = "name";
    public static final String CDC = "cdc";
    public static final Integer EVENT_RESPONCE_LENGTH = 1000;
    public static final Integer LOG_PERCENT = 100;

    /**
     * obs相关
     */
    public static final String UPLOAD_OBS_ADS_GIT_PATH = "https://xxx.com/main-sg2-prod/git/";
    public static final String GIT_FLIE_SEGMENTATION = "--shareit--shareit--";
    public static final String UPLOAD_OBS_PREFFIX = "hebe/";
    public static final String COPY_PATH = "path";
    public static final String OBS_FLAG_TRUE = "true";
    public static final String OBS_FLAG_FALSE = "false";
    public static final String OBS_RESULT = "result";
    public static final String OBS_MESSAGE = "message";
    public static final String OBS_HEAD = "obs://";



    /**
     * ==================================ARTIFACT_Type==========================================
     */
    public static final String ARTIFACT_MODE_UPLOAD= "UPLOAD";
    public static final String ARTIFACT_TYPE_ONLINE = "ONLINE";
    public static final String ARTIFACT_TYPE_TXT = "TXT";
    public static final String ARTIFACT_TYPE_JAR = "JAR";

    /**
     * ==================================APPLICATION_Type==========================================
     */
    public static final String APPLICATION_TYPE_JAR = "JAR";
    public static final String APPLICATIONT_TYPE_SQL = "SQL";
    public static final String APPLICATION_TYPE_JAVA = "JAVA";
    public static final String APPLICATION_TYPE_SCALA = "SCALA";
    public static final String APPLICATION_TYPE_PYTHON = "PYTHON";

    /**
     * ==================================restoreStrategy==========================================
     */
    public static final String TASK_RESTORE_STRATEGY_NONE = "NONE";
    public static final String TASK_RESTORE_STRATEGY_LATEST_STATE = "LATEST_STATE";


    /**
     * 将MultipartFile存到本地的临时存放目录
     */
    public static final String LOCAL_DOWNLOAD_TMP = "/tmp/";
    public static final String LOCAL_UPLOAD_DIR = "static" + System.getProperty("file.separator") + "tmpupload" + System.getProperty("file.separator");
    public static final String WINDOW_PREFFIX = "file:";
    public static final String DEFAULT_UDF_JAR_NAME = "data_development_udf.jar";

    /**
     * event_code
     */
    /**
     * ===========================修改资源============================
     */
    public static final String PUT_METHOD = "PUT";
    /**
     * ===========================新增资源============================
     */
    public static final String POST_METHOD = "POST";
    /**
     * ===========================删除资源===========================
     */
    public static final String DELETE_METHOD = "DELETE";
    /**
     * ===========================修改资源============================
     */
    public static final String PATCH_METHOD = "PATCH";
    /**
     * ===========================获取资源============================
     */
    public static final String GET_METHOD = "GET";
    public static final String CANCEL = "CANCEL";
    public static final String UNKNOWN = "UNKNOWN";

    /**
     * time
     */
    public static final Long FIXED_DELAY = 2000L;


    /**
     * savepoint相关
     */
    public static final String SAVEPOINT_STATUS_COMPLETED = "COMPLETED";
    public static final String SAVEPOINT_STATUS_IN_PROGRESS = "IN_PROGRESS";
    public static final String SAVEPOINT = "SAVEPOINT";
    public static final String CHECKPOINT = "CHECKPOINT";

    /**
     * 运行环境
     */
    public static final String DEV = "dev";
    public static final String PROD = "prod";
    public static final String TEST = "test";
    public static final String CLOUD_TEST = "cloud-test";
    public static final String CLOUD_PROD = "cloud-prod";
    public static final String CLOUD = "CLOUD";

    /**
     * K8S运行参数
     */
    public static final String LOCAL_PREFIX = "local:///opt/flink/usrlib/";

    public static final String DEFAULT_HUAWEI_IAM = "BDP-Developer-Flink";
    public static final String DEFAULT_AWS_IAM = "BDP-Developer";

    public static final String DEFAULT_JOB_ID = "00000000000000000000000000000000";

    public static final String JOBMANAGER_ANNOTATIONS_MODEL = "prometheus.io/port:10106,prometheus.io/scrape:true,iam.shareit.me/huawei:{0},iam.shareit.me/aws:{1},iam.shareit.me/type:env";
    public static final String JOBMANAGER_ANNOTATIONS_MODEL_NOAWS = "prometheus.io/port:10106,prometheus.io/scrape:true,iam.shareit.me/huawei:{0},iam.shareit.me/type:env";
    public static final String JOBMANAGER_LABLES = "owner:{0},template:{1},id:{2},app-ds-name:{3},tenantId:{4},groupId:{5}";
    /**
     * Scmp 接口及参数
     */
    public static final String SCMP_URL = "https://openapi.ushareit.me/user/api/v1/aksk/check";
    public static final String SCMP_URL_IAM = "https://xxxx/user/api/v1/aksk/iam";
    public static final String SCMP_GET_TOKEN_URL = "https://xxxx/dex/token";
    public static final String SCMP_DELETE_DEPLOYMENT_URL = "http://xxxx/hulk/openapi/api/v2/apps/{0}/deployment/{1}";
    public static final String SCMP_USERNAME = "xxxxx";
    public static final String SCMP_PASSWORD = "xxxx";
    public static final String SCMP_GRANT_TYPE = "password";
    public static final String SCMP_SCOPE = "openid groups";
    public static final String SCMP_CLIENT_ID = "xxxx";
    public static final String SCMP_CLIENT_ID_USER = "xxx";

    /**
     * IT接口参数
     */
    public static final String GET = "get";
    public static final String POST = "post";
    public static final String IT_GET_TOKEN_URL = "{0}/v1/get/token";
    public static final String IT_GET_USER_URL = "{0}/api/v1/user/fuzzy/query";
    public static final String IT_BATCH_GET_USER_URL = "{0}/api/v1/user/basicinfo/list";
    public static final String IT_GET_DEPARTMENT_LIST_URL = "{0}/api/v1/department/list";
    public static final String IT_GET_DEPT_INFO_URL = "{0}/api/v1/user/deptinfo/get";
    public static final String IT_GET_SUBORDINATE_INFO_URL = "{0}/api/v1/user/subordinate/list";

    public static final String IT_SYSKEY_TEST = "xxxx";
    public static final String IT_SYSSECRET_TEST = "xxxx";

    public static final String IT_SYSKEY = "xxxx";
    public static final String IT_SYSSECRET = "xxxx";


    /**
     * Bi平台接口参数
     */
    public static final String BI_URL_TEST = "https://dashboard-server-test.ushareit.org";
    public static final String BI_URL = "https://dashboard-server.ushareit.org";
    public static final String BI_DS_DASHBOARD_PERMISSION_TEST="319,310,601";
    public static final String BI_DS_DASHBOARD_PERMISSION="319,310,756";
    /**
     * checkpoint
     */
    public static final String CHECKPOINT_PATH = "/checkpoint";

    /**
     * savepoint
     */
    public static final String SAVEPOINT_PATH = "/savepoint";

    /**
     * ha
     */
    public static final String HA_PATH = "/ha";

    /**
     * 云商
     */
    public static final String HUAWEI = "huawei";
    public static final String AWS = "aws";

    public static final String FLINK_DIR = "/data/flink";
    public static final String K8S_DIR = "/data/code/k8s";
    public static final String FLINK_CONF_DIR = FLINK_DIR + "/conf";
    public static final String FLINK_LIB_DIR = FLINK_DIR + "/lib";
    public static final String FLINK_OPT_DIR = FLINK_DIR + "/opt";


    public static final String METRICS_URL = "https://xxxx/d/flink-operator-hebe/flink-operator-hebe?orgId=1&kiosk=tv&var-job_id={0}";
    public static final String PERJOB_METRICS_URL = "https://xxxx/d/flink-operator-hebe-perjob/flink-operator-hebe-perjob?orgId=1&kiosk=tv&var-app={0}&var-job_id={1}";
    public static final String LOG_URL = "https://xxxx/explore?orgId=1&kiosk=tv&left=[\"now-5m\",\"now\",\"{0}\",'{'\"metrics\":['{'\"type\":\"logs\"'}'],\"query\":\"labels.app:{1}\"'}']";

    public static final String LOG_URL_FRONT="https://xxxx/explore?orgId=1&kiosk=tv&left=";
    public static final String LOG_URL_BEHIN = "[\"now-1h\",\"now\",\"{0}\",'{'\"query\":\"labels.app:({1})\",\"alias\":\"\",\"metrics\":['{'\"id\":\"1\",\"type\":\"logs\",\"settings\":'{'\"limit\":\"500\"'}'}'],\"bucketAggs\":[],\"timeField\":\"time\"'}']";
    public static final String OFFLINE_TASK_URL = "https://datastudio-{0}.ushareit.org/pipeline/refresh?name={1}";


    public static final String METADATA_PASSWDKEY = "DataStudio-20210628";

    public static final String UE1 = "ue1";
    public static final String SG1 = "sg1";
    public static final String SG2 = "sg2";

    public static final String UE1_HIVE_LOCATION = "s3://xxxx/datacake/";
    public static final String SG1_HIVE_LOCATION = "s3://xxxx/datacake/";
    public static final String SG2_HIVE_LOCATION = "obs://xxxx/datacake/";
    public static final String SG3_HIVE_LOCATION = "gs://xxxx/datacake/";

    public static final String DB_SUFFIX = ".db";

    /**
     * job 状态
     */
    public static final String JOB_STATUS_INITIALIZING = "INITIALIZING";

    /**
     * k8s constants
     */
    public static final String LABEL_TYPE_KEY = "type";
    public static final String LABEL_CONFIGMAP_TYPE_KEY = "configmap-type";
    public static final String LABEL_CONFIGMAP_TYPE_HIGH_AVAILABILITY = "high-availability";
    public static final String LABEL_TYPE_NATIVE_TYPE = "flink-native-kubernetes";
    public static final String LABEL_APP_KEY = "app";

    /**
     * 实例诊断状态
     */
    public static final Integer TASKINSTANCE_STATE_NORMAL = 0;
    public static final Integer TASKINSTANCE_STATE_UPSTREAM_ERROR = 1;
    public static final Integer TASKINSTANCE_STATE_SELF_ERROR = 2;

    public static String DINGDING_SECRET = "xxxx";
    public static String DINGDING_WEBHOOK = "xxxx";

    // TODO test
    public static String DINGDING_WORKORDER_SECRET_TEST = "xxxx";
    public static String DINGDING_WORKORDER_WEBHOOK_TEST = "xxxx";

    /**
     * CoCo
     */
    public static String DINGDING_WORKORDER_SECRET = "xxxx";
    public static String DINGDING_WORKORDER_WEBHOOK = "xxxx";

    /**
     * 离线任务trigger方式
     */
    public static String CRON_TRIGGER = "cron" ;
    public static String DATA_TRIGGER = "data" ;

    /**
     * 离线任务trigger依赖类型
     */
    public static String DATA_DEPEND = "dataset" ;
    public static String EVENT_DEPEND = "event" ;

    /**
     * 功能模块
     */
    public static String TASK = "TASK";
    public static String WORKFLOW = "WORKFLOW";
    public static String TASK_INSTANCE = "TASK_INSTANCE";

    /**
     * 获取集群的部门配置
     */
    public static String BDP = "bdp";
    public static String ADS = "ads";
    public static String DEFAULT_GROUP = BDP;
    public static String ADS_GROUP_KEYWORDS = "广告中心";

    /**
     * ds任务告警方式
     */
    public static String ALERT_DINGTALK = "dingTalk";
    public static String ALERT_PHONE = "phone";

    public static int SAMPLE_SIZE = 5;
    /**
     * Mysql
     */
    public static String MYSQL_URL = "jdbc:mysql://%s:%s/%s";

    /**
     * Clickhouse
     */
    public static String CLICKHOUSE_URL = "jdbc:clickhouse://%s:%s/%s";

    /**
     * SqlServer
     */
    public static String SQLSERVER_DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    public static String SQLSERVER_URL = "jdbc:sqlserver://%s:%s;databaseName=%s;encrypt=true;trustServerCertificate=true";

    /**
     * oracle
     */
    public static String ORACLE_SID = "EE";
    public static String ORACLE_URL = "jdbc:oracle:thin:@%s:%s:%s";
    public static String ORACLE_DRIVER = "oracle.jdbc.driver.OracleDriver";

    /**
     * postgres
     */
    public static String POSTGRES_URL = "jdbc:postgresql://%s:%s/%s?%s";

    public static String NineBotDataApplication="数据接入申请";

    /**
     * shareit 超级管理租户
     */
//    public static String SUPPER_TENANT_NAME = "bdp";
//
//    public static String SHAREIT_TENANT_NAME = "shareit";
}
