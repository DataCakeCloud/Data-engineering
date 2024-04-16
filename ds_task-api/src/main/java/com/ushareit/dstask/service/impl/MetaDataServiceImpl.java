package com.ushareit.dstask.service.impl;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ushareit.dstask.bean.Actor;
import com.ushareit.dstask.common.param.MetaDataParam;
import com.ushareit.dstask.constant.*;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import com.ushareit.dstask.service.MetaDataService;
import com.ushareit.dstask.web.ddl.DdlFactory;
import com.ushareit.dstask.web.ddl.SqlDdl;
import com.ushareit.dstask.web.ddl.metadata.Table;
import com.ushareit.dstask.web.ddl.model.KafkaDdl;
import com.ushareit.dstask.web.ddl.model.MetisDdl;
import com.ushareit.dstask.web.metadata.MetaDataManager;
import com.ushareit.dstask.web.utils.FileSystemUtil;
import com.ushareit.engine.param.ActorProvider;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class MetaDataServiceImpl extends AbstractBaseServiceImpl<Table> implements MetaDataService {

    @Override
    public CrudMapper<Table> getBaseMapper() {
        return null;
    }

    /**
     * 兼容所有的元数据获取
     *
     * @param metaDataParam
     */
    //@Cacheable(cacheNames = {"seatunnelSearch"}, key = "#type+'-'+#actorId+'-'+#db+'-'+#table+'-'+#sourceParam")
    @Override
    public List<Table> search(MetaDataParam metaDataParam, String type, String actorId, String db, String table, String sourceParam) {
        log.info("get new data");
        return MetaDataManager.getMetaDataDiscover(metaDataParam);
    }

    /**
     * 兼容所有的元数据获取
     */
    //@CacheEvict(cacheNames = {"seatunnelSearch"}, key = "#type+'-'+#actorId+'-'+#db+'-'+#table+'-'+#sourceParam")
    @Override
    public Boolean clearCache(String type, String actorId, String db, String table, String sourceParam) {
        log.info("success cache key :seatunnelSearch-" + type + "-" + actorId + "-" + db + "-" + table + "-" + sourceParam);
        return true;
    }

    @Override
    public List<Table> search(MetaDataParam metaDataParam) {
        return MetaDataManager.getMetaDataDiscover(metaDataParam);
    }
    @Override
    public List<Map<String, String>> getStorageSchemaDetail(Actor actor,String type,String path,String fileType,String fieldDelimiter) {
        String configuration = actor.getConfiguration();
        JSONObject configurationObject = JSON.parseObject(configuration);
        ActorProvider actorProvider = JSON.parseObject(configurationObject.getString("provider"), ActorProvider.class);
        String bucket = actorProvider.getBucket();
        String awsAccessKeyId = actorProvider.getAwsAccessKeyId();
        String awsSecretAccessKey = actorProvider.getAwsSecretAccessKey();
        String implClass = "";
        String defaultFS;
        if ("s3".equalsIgnoreCase(type)){
            defaultFS = String.format("%s%s","s3://",bucket);
            implClass = "org.apache.hadoop.fs.s3a.S3AFileSystem";
        } else if ("ks3".equalsIgnoreCase(type)) {
            defaultFS = String.format("%s%s","ks3://",bucket);
            implClass = "com.ksyun.kmr.hadoop.fs.ks3.Ks3FileSystem";
        }else{
            defaultFS = "hdfs://ip-172-17-53-122.ec2.internal:8020";
        }
        try {
            Configuration conf = FileSystemUtil.getFileSystemConf(awsAccessKeyId, awsSecretAccessKey, implClass, defaultFS, type);
            Path childPath = FileSystemUtil.getPath(conf,new Path(path));
            if("orc".equalsIgnoreCase(fileType)){
                List<Map<String, String>> orcSchema = FileSystemUtil.getOrcSchema(conf, childPath);
                return orcSchema;
            }else if("parquet".equalsIgnoreCase(fileType)){
                List<Map<String, String>> parquetSchema = FileSystemUtil.getParquetSchema(conf, childPath);
                return parquetSchema;
            }else if("csv".equalsIgnoreCase(fileType)){
                List<Map<String, String>> csvSchema = FileSystemUtil.getTextFileSchema(conf, childPath, ",");
                return csvSchema;
            }else{
                List<Map<String, String>> textFileSchema = FileSystemUtil.getTextFileSchema(conf, childPath, fieldDelimiter);
                return textFileSchema;
            }
        }catch (Exception e){
            throw new ServiceException(BaseResponseCodeEnum.GET_STORAGE_DETAIL_FAILED,e.getMessage());
        }
    }

    @Override
    public List<Map<String, String>> getTableSample(MetaDataParam metaDataParam) {
        return MetaDataManager.getTableSample(metaDataParam);
    }

    @Override
    public Boolean checkConnection(MetaDataParam metaDataParam) {
        return MetaDataManager.checkConnection(metaDataParam);
    }

    @Override
    public Boolean createTable(MetaDataParam metaDataParam) {
        return MetaDataManager.createTable(metaDataParam);
    }

    @Override
    public String getDdl(MetaDataParam metaDataParam) {
        if(StringUtils.isEmpty(metaDataParam.getTable())){
            throw new ServiceException(BaseResponseCodeEnum.TABLE_PARAM_IS_NULL);
        }
        try {
            return DdlFactory.getDdl(metaDataParam).getDdl(null);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new ServiceException(BaseResponseCodeEnum.SQL_DECODE_FAIL);
        }
    }

    @Override
    public Map<String, Object> getDisplayDdl(MetaDataParam metaDataParam) {
        try {
            SqlDdl ddl = DdlFactory.getDdl(metaDataParam);
            HashMap<String, Object> result = new HashMap<>(2);
            if (ddl instanceof MetisDdl || ddl instanceof KafkaDdl) {
                Object displaySchema = ddl.getDisplaySchema(metaDataParam.getIsSql());
                result.put("columns", displaySchema);
            } else {
                result.put("columns", ddl.getColumns());
            }

            String displayTableName = ddl.getDisplayTableName();
            result.put("table", displayTableName);
            return result;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new ServiceException(BaseResponseCodeEnum.SQL_DECODE_FAIL);
        }
    }

}