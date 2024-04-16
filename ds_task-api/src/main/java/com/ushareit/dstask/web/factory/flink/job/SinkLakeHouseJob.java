package com.ushareit.dstask.web.factory.flink.job;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.amazonaws.services.dynamodbv2.xspec.S;
import com.google.common.base.Joiner;
import com.ushareit.dstask.bean.Actor;
import com.ushareit.dstask.bean.CloudResouce;
import com.ushareit.dstask.bean.HiveTable;
import com.ushareit.dstask.bean.Task;
import com.ushareit.dstask.configuration.DataCakeRegionProperties;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.constant.DsTaskConstant;
import com.ushareit.dstask.constant.SymbolEnum;
import com.ushareit.dstask.constant.TemplateEnum;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.service.impl.TaskServiceImpl;
import com.ushareit.dstask.web.ddl.SqlDdl;
import com.ushareit.dstask.web.ddl.metadata.Column;
import com.ushareit.dstask.web.factory.scheduled.param.Dataset;
import com.ushareit.dstask.web.utils.DataCakeConfigUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.PostConstruct;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xuebotao
 * @date 2021/8/30
 */
@Slf4j
public class SinkLakeHouseJob extends DataLakeJob {
    private static final String DATEPART_SQL = "DATE_FORMAT(proc_time, 'yyyyMMdd') as datapart,";

    private static final String HOUR_SQL = "DATE_FORMAT(proc_time, 'HH') as `hour` ";

    private static final String ICEBEG_CATALOG_DEF = "CREATE CATALOG %s WITH (\n" +
            "  'type'='iceberg',\n" +
            "  'catalog-type'='hive',\n" +
            "  'uri'='%s',\n" +
            "  'clients'='5',\n" +
            "  'property-version'='1',\n" +
            "  'warehouse'='%s'\n" +
            ");\n";

    private static final String PAIMO_CATALOG_DEF = "CREATE CATALOG %s WITH (\n" +
            "  'type'='paimon',\n" +
            "  'metastore'='filesystem',\n" +
            "  'warehouse'='%s'\n" +
            ");\n";

    private static final String PAIMO_WAREHOUSE = "oss://{0}.cn-beijing.oss-dls.aliyuncs.com/";

    public static SinkLakeHouseJob sinkLakeHouseJob;

    @PostConstruct
    public void init() {
        sinkLakeHouseJob = this;
    }


    private final String INSERT_SQL_DEMO =
            "INSERT INTO %s \n" +
                    "SELECT \n" +
                    "%s \n" +
                    "FROM %s;\n";


    public SinkLakeHouseJob(Task task, Integer tagId, Integer savepointId, TaskServiceImpl taskServiceImp) {
        super(task, tagId, savepointId, taskServiceImp);
    }

    protected String getIcebergFormatVersion() {
        return null;
    }

    @Override
    public void beforeExec() throws Exception {
        List<Column> columns = runtimeConfig.getColumns();
        String content = runtimeConfig.getContent();

        if (!StringUtils.isBlank(content)) {
            columns = string2Columns(content);
        }
        //兼容多表情况
        if (columns == null) {
            columns = runtimeConfig.getTables().get(0).getColumns();
        }
        // 将用户增加的Column设置进来
        runtimeConfig.setColumns(columns);

        // 组装SQL
        String sql = assembleFinalSql();
        String encodedSql = new String(Base64.getEncoder().encode(URLEncoder.encode(sql).getBytes()));
        runTimeTaskBase.setContent(encodedSql);

        super.beforeExec();
    }

    private List<Column> string2Columns(String content) throws UnsupportedEncodingException {
        String decode = URLDecoder.decode(new String(Base64.getDecoder().decode(content.getBytes())), "UTF-8");

        if (!decode.endsWith(",")) {
            decode = decode + ",";
        }

        List<Column> collect = Arrays.asList(decode.split("\n")).stream().map(str -> {
            String[] arr = str.trim().split(" ");
            if (arr.length < 2) {
                return null;
            }
            Column column = new Column();
            column.setName(arr[0]);
            String type = arr[1].toUpperCase();
            if (type.contains("ROW") || type.contains("MAP") || type.contains("ARRAY")) {
                type = "STRING";
            }

            if (type.endsWith(",")) {
                type = type.substring(0, type.lastIndexOf(","));
            }

            column.setType(type);

            return column;
        }).filter(column -> column != null).collect(Collectors.toList());

        return collect;
    }


    private String assembleFinalSql() throws Exception {
        if (inputDatasets == null || outputDatasets == null) {
            throw new ServiceException(BaseResponseCodeEnum.CLI_PARAM_ILLEGAL, "可视化模式下输入/输出数据集不能为空！");
        }
        //
        Integer sourceActorId = runtimeConfig.getActorId();
        if (sourceActorId == null || sourceActorId <= 0) {
            throw new ServiceException(BaseResponseCodeEnum.KAFKA_CONFIG_ERROR);
        }
        Actor byId = taskServiceImp.actorService.getById(sourceActorId);
        if (byId == null) {
            throw new ServiceException(BaseResponseCodeEnum.KAFKA_CONFIG_ERROR);
        }
        String configuration = byId.getConfiguration();
        JSONObject con = JSON.parseObject(configuration);
        String bootstrapServers = con.getString("bootstrap_servers");
        log.info("connection bootstrapServers is :" + con.getString("bootstrap_servers"));
        runtimeConfig.setBootstrap(bootstrapServers);
        runtimeConfig.setSourceActor(byId);
        //1、创建source ddl
        SqlDdl sourceDdl = getSourceSqlDdl();
        String sourceSql = sourceDdl.getDdl(runtimeConfig);

        //2、创建sink ddl
        SqlDdl targetDdl = getSinkSqlDdl(sourceDdl.getName(), outputDatasets.get(0).getMetadata().getRegion());

        //3、组装insert sql
        String insertSql = String.format(INSERT_SQL_DEMO, targetDdl.getName(), getSelectColumns(runtimeConfig.getColumns()), sourceDdl.getName());
        log.info("insert sql:" + insertSql);

        //4、组装完整sql
        String catalog = outputDatasets.get(0).getMetadata().getRegion();
//        String finalSql = getSet() + getCatalogStr(catalog) + sourceSql + targetDdl.getDdl(runtimeConfig) + insertSql;
        String finalSql = getSet() + getCatalogStr(catalog) + sourceSql + insertSql;

        log.info("SinkIcebergJob组装后最终SQL:" + finalSql);
        return finalSql;
    }


    public String getCatalogStr(String region) {
        String lakeHouseType = runtimeConfig.getLakeHouseType();
        DataCakeRegionProperties.RegionConfig regionConfig = DataCakeConfigUtil.getDataCakeRegionProperties().getRegionConfig(region);
        String hmsUri = regionConfig.getHmsUri();
        CloudResouce.DataResource cloudResource = DataCakeConfigUtil.getCloudResourcesService().getCloudResource(region);
        String bucket = cloudResource.getBucket();
        //warehouse=bdp-poc-test/datastudio/
        String catlogNmae = region.replace("-", "_");

        if (lakeHouseType.equals("ICEBERG")) {
            return String.format(ICEBEG_CATALOG_DEF, catlogNmae, hmsUri, cloudResource.getStorage() + "flink/datacake/");
        }
        return String.format(PAIMO_CATALOG_DEF, catlogNmae, MessageFormat.format(PAIMO_WAREHOUSE, bucket) + bucket + "/warehouse/");

    }

    public Object getSelectColumns(List<Column> columns) {
        StringBuffer sb = getBaseSelectColumns(columns);
        return sb.toString();
    }

    protected StringBuffer getBaseSelectColumns(List<Column> columns) {
        StringBuffer sb = new StringBuffer();
        int lastIndex = columns.size() - 1;
        for (int i = 0; i <= lastIndex; i++) {
            sb.append(" `" + columns.get(i).getName());
            if (i == lastIndex) {
                sb.append("` \n");
            } else {
                sb.append("`, \n");
            }
        }
        return sb;
    }

    public String getTargetTableName(String sourceTableName, String format) {
        Dataset out = outputDatasets.get(0);
        String catalog = out.getMetadata().getRegion();
        String database = out.getMetadata().getDb();
        // 按用户自定义的sink table name来返回
        if (!StringUtils.isEmpty(out.getMetadata().getTable())) {
            return Joiner.on(SymbolEnum.PERIOD.getSymbol())
                    .skipNulls()
                    .join(catalog, database, out.getMetadata().getTable()).toLowerCase();
        }

        String tableName = String.format(format, catalog, database, sourceTableName);
        return tableName;
    }

    @Override
    public void afterExec() {
        super.afterExec();
        saveHiveTable(getTask());
    }


    public void saveHiveTable(Task task) {
        log.info("starting save hive table");
        log.info("task id is " + task.getId());
        log.info("task output dataset is " + task.getOutputDataset());

        HiveTable hiveTable = taskServiceImp.hiveTablesService.getById(task.getId());
        log.info("hiveTable is " + hiveTable);

        if (StringUtils.isEmpty(task.getOutputDataset())) {
            log.info("the output dataset of task is null");
            return;
        }

        log.info("组装hive table 表信息");
        List<Dataset> outputDatasets = JSON.parseArray(task.getOutputDataset(), Dataset.class);
        Dataset.Metadata metadata = outputDatasets.get(0).getMetadata();

        String db = metadata.getDb();
        String catalog = metadata.getRegion();

        CloudResouce.DataResource cloudResource = DataCakeConfigUtil.getCloudResourcesService().getCloudResource(catalog);
        String table = metadata.getTable();
        String region = cloudResource.getRegion();
        String provider = cloudResource.getProvider();
        String path = String.format(cloudResource.getBucket() + "/" + cloudResource.getPath() + "datastudio/" + "%s/%s", db + DsTaskConstant.DB_SUFFIX, table);

        // 新建
        log.info("create hive table");
        if (hiveTable == null) {
            hiveTable = new HiveTable()
                    .setCatalog(catalog)
                    .setDatabase(db)
                    .setDeleteStatus(0)
                    .setMode("k8s")
                    .setPath(path)
                    .setName(table)
                    .setProvider(provider)
                    .setRegion(region)
                    .setTaskId(task.getId());
            hiveTable
                    .setCreateBy(task.getCreateBy())
                    .setUpdateBy(task.getUpdateBy());
            taskServiceImp.hiveTablesService.save(hiveTable);
            return;
        }

        // 更新
        log.info("update hive table");
        hiveTable.setCatalog(catalog)
                .setDeleteStatus(0)
                .setPath(path)
                .setDatabase(db)
                .setName(table)
                .setProvider(provider)
                .setRegion(region)
                .setUpdateBy(task.getUpdateBy());
        taskServiceImp.hiveTablesService.update(hiveTable);
    }
}
