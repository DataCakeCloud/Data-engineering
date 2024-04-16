package com.ushareit.dstask.web.metadata.olap;

import com.ushareit.dstask.bean.Account;
import com.ushareit.dstask.bean.CloudResouce;
import com.ushareit.dstask.common.param.MetaDataParam;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.constant.SourceTypeEnum;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.mapper.AccountMapper;
import com.ushareit.dstask.third.cloudresource.CloudResourcesService;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import com.ushareit.dstask.web.ddl.metadata.Table;
import com.ushareit.dstask.web.metadata.airbyte.AirByteMetaData;
import com.ushareit.dstask.web.utils.DataCakeConfigUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: xuebotao
 * @create: 2022-07-28
 */
@Slf4j
@Component

public class HiveMetaData extends AirByteMetaData {

    @Resource
    public AccountMapper accountMapper;

    @Resource
    public CloudResourcesService cloudResourcesService;

    @Resource
    public HiveOlapUtil hiveOlapUtil;

    @Value("${olap.aws.url}")
    public String awsUrl;

    @Value("${olap.awsSG.url}")
    public String awsSGUrl;

    @Value("${olap.huawei.url}")
    public String huaweiUrl;

    @Value("${ranger.adminUsername}")
    public String adminUsername;

    @Value("${ranger.adminPassword}")
    public String adminPassword;

    @Value("${genie.client.username}")
    public String genieUsername;

    @Value("${genie.client.password}")
    public String geniePassword;

    public HiveMetaData(MetaDataParam metaDataParam) {
        super(metaDataParam);
    }

    public  static List<Account> account;

    public List<Table> search(MetaDataParam metaDataParam) {
        String tenantName = InfTraceContextHolder.get().getTenantName();

        log.info("search metaDataParam is :" + metaDataParam.toString());
        account = accountMapper.selectAll();
        CloudResouce cloudResource = cloudResourcesService.getCloudResource();
        List<String> collect = cloudResource.getList().stream().map(CloudResouce.DataResource::getRegionAlias).collect(Collectors.toList());
        if (StringUtils.isEmpty(metaDataParam.getRegion()) || !collect.contains(metaDataParam.getRegion())) {
            throw new ServiceException(BaseResponseCodeEnum.CLI_PARAM_ILLEGAL);
        }
        //使用presto引擎
        String userName = InfTraceContextHolder.get().getUserName();
        String region = metaDataParam.getRegion();
        String engine = DataCakeConfigUtil.getDataCakeRegionProperties().getRegionConfig(region).getEngine();
        if (StringUtils.isNotEmpty(metaDataParam.getDb()) && StringUtils.isNotEmpty(metaDataParam.getTable())) {
            return hiveOlapUtil.getMetaColumn(this, engine, userName, metaDataParam.getDb(), metaDataParam.getTable(), tenantName);
        }
        if (StringUtils.isNotEmpty(metaDataParam.getDb())) {
            return hiveOlapUtil.getMetaTable(this, engine, userName, metaDataParam.getDb(), tenantName);
        }
        if (StringUtils.isNotEmpty(metaDataParam.getRegion()) && StringUtils.isNotEmpty(metaDataParam.getType())) {
            return hiveOlapUtil.getMetaDatabase(this, engine, userName, tenantName);
        }
        return Collections.EMPTY_LIST;
    }


    @Override
    public Table getDdl(MetaDataParam metaDataParam) {
        List<Table> search = search(metaDataParam);
        if (search.isEmpty()) {
            throw new ServiceException(BaseResponseCodeEnum.DATA_NOT_FOUND, "metadata not exist");
        }
        Table table = search.stream().findFirst().get();
        table.setName(metaDataParam.getTable());
        table.setTypeName(SourceTypeEnum.hive_table.name());
        return table;
    }

}
