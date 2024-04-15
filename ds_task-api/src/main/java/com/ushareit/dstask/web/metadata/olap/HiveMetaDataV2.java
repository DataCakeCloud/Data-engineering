package com.ushareit.dstask.web.metadata.olap;


import com.ushareit.dstask.bean.CloudResouce;
import com.ushareit.dstask.common.param.MetaDataParam;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.constant.SourceTypeEnum;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.third.cloudresource.CloudResourcesService;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import com.ushareit.dstask.web.ddl.metadata.Table;
import com.ushareit.dstask.web.metadata.airbyte.AirByteMetaData;
import com.ushareit.dstask.web.metadata.lakecat.Lakecatutil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author: xuebotao
 * @create: 2023-03-20
 */
@Slf4j
@Component
public class HiveMetaDataV2 extends AirByteMetaData {

    @Resource
    public Lakecatutil lakecatutil;

    @Resource
    public CloudResourcesService cloudResourcesService;

    public HiveMetaDataV2(MetaDataParam metaDataParam) {
        super(metaDataParam);
    }

    public List<Table> search(MetaDataParam metaDataParam) {
        String tenantName = InfTraceContextHolder.get().getTenantName();
        log.info("search metaDataParam is :" + metaDataParam.toString());
        CloudResouce cloudResource = cloudResourcesService.getCloudResource();
        List<String> collect = cloudResource.getList().stream().map(CloudResouce.DataResource::getRegionAlias).collect(Collectors.toList());
        if (StringUtils.isEmpty(metaDataParam.getRegion()) || !collect.contains(metaDataParam.getRegion())) {
            throw new ServiceException(BaseResponseCodeEnum.CLI_PARAM_ILLEGAL);
        }
        String groupUuid = InfTraceContextHolder.get().getUuid();
        String region = metaDataParam.getRegion();
        if (StringUtils.isNotEmpty(metaDataParam.getDb()) && StringUtils.isNotEmpty(metaDataParam.getTable())) {
            return lakecatutil.getMetaColumn(tenantName, region, metaDataParam.getDb(), metaDataParam.getTable(), groupUuid, metaDataParam.getJudgeTable());
        }
        if (StringUtils.isNotEmpty(metaDataParam.getDb())) {
            return lakecatutil.getMetaTable(tenantName, region, metaDataParam.getDb());
        }
        if (StringUtils.isNotEmpty(metaDataParam.getRegion()) && StringUtils.isNotEmpty(metaDataParam.getType())) {
            return lakecatutil.getMetaDatabase(tenantName, region);
        }
        return Collections.EMPTY_LIST;
    }


    @Override
    public Table getDdl(MetaDataParam metaDataParam) {
        String userName = InfTraceContextHolder.get().getUserName();
        InfTraceContextHolder.get().setUserName("admin");
        List<Table> search = search(metaDataParam);
        if (search.isEmpty()) {
            throw new ServiceException(BaseResponseCodeEnum.DATA_NOT_FOUND, "metadata not exist");
        }
        Table table = search.stream().findFirst().get();
        table.setName(metaDataParam.getTable());
        table.setTypeName(SourceTypeEnum.hive_table.name());
        InfTraceContextHolder.get().setUserName(userName);
        return table;
    }

}
