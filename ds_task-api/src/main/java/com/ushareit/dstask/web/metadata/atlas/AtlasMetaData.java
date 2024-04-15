package com.ushareit.dstask.web.metadata.atlas;

import com.alibaba.fastjson.JSON;
import com.ushareit.dstask.bean.MetaData;
import com.ushareit.dstask.common.param.MetaDataParam;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.constant.CommonConstant;
import com.ushareit.dstask.constant.DsTaskConstant;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import com.ushareit.dstask.utils.HttpUtil;
import com.ushareit.dstask.web.ddl.metadata.Table;
import com.ushareit.dstask.web.metadata.AbstractMetaData;
import com.ushareit.dstask.web.vo.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
public class AtlasMetaData extends AbstractMetaData {


    public AtlasMetaData(MetaDataParam metaDataParam) {
        super(metaDataParam);
    }

    @Override
    public List<Table> search(MetaDataParam metaDataParam) {
        if (!metaDataParam.isLegal()) {
            throw new ServiceException(BaseResponseCodeEnum.CLI_PARAM_ILLEGAL);
        }

        StringBuilder url = new StringBuilder();
        url.append(getMetadataUrl(metaDataParam));
        if (StringUtils.isNotEmpty(metaDataParam.getGuid())) {
            url.append("guid=").append(metaDataParam.getGuid()).append("&");
            return getMetadataTable(url.substring(0, url.length() - 1));
        }
        if (StringUtils.isNotEmpty(metaDataParam.getQualifiedName())) {
            url.append("qualifiedName=").append(metaDataParam.getQualifiedName()).append("&");
            return getMetadataTable(url.substring(0, url.length() - 1));
        }
        if (StringUtils.isNotEmpty(metaDataParam.getRegion())) {
            url.append("region=").append(metaDataParam.getRegion()).append("&");
        }
        if (StringUtils.isNotEmpty(metaDataParam.getType())) {
            url.append("type=").append(metaDataParam.getType()).append("&");
        }
        if (StringUtils.isNotEmpty(metaDataParam.getDatasource())) {
            url.append("datasource=").append(metaDataParam.getDatasource()).append("&");
        }
        if (StringUtils.isNotEmpty(metaDataParam.getDb())) {
            url.append("db=").append(metaDataParam.getDb()).append("&");
        }
        if (StringUtils.isNotEmpty(metaDataParam.getTable())) {
            url.append("table=").append(metaDataParam.getTable()).append("&");
        }
        return getMetadataTable(url.substring(0, url.length() - 1));
    }


    private static String getMetadataUrl(MetaDataParam metaDataParam) {
        String metaUrlPrefix;
        if (DsTaskConstant.PROD.equals(InfTraceContextHolder.get().getEnv())) {
            metaUrlPrefix ="";
        } else if (DsTaskConstant.TEST.equals(InfTraceContextHolder.get().getEnv())) {
            metaUrlPrefix = "";
        } else {
            metaUrlPrefix = "";
        }
        if (StringUtils.isNotEmpty(metaDataParam.getQualifiedName()) || StringUtils.isNotEmpty(metaDataParam.getGuid())) {
            return metaUrlPrefix + "dataset/get?";
        } else {
            return metaUrlPrefix + "dataset/searchDataset?";
        }

    }

    public static List<Table> getMetadataTable(String getTableUrl) {
        List<Table> tables = new ArrayList<>();
        log.info("查询元数据表信息url：" + getTableUrl);

        Map<String, String> headers = new HashMap<>(1);
        headers.put(CommonConstant.AUTHENTICATION_HEADER, InfTraceContextHolder.get().getAuthentication());

        BaseResponse response = HttpUtil.get(getTableUrl, null, headers);
        if (response == null || !BaseResponseCodeEnum.SUCCESS.name().equals(response.getCodeStr())) {
            throw new ServiceException(BaseResponseCodeEnum.SYS_UNA);
        }
        if (getTableUrl.contains("dataset/get?")) {
            Table table = JSON.parseObject(response.getData().toString(), Table.class);
            tables.add(table);
        } else {
            tables = JSON.parseObject(response.getData().toString(), MetaData.class).getData();
        }

        if (tables == null) {
            tables = new ArrayList<>();
        }

        return tables;
    }


    @Override
    public Table getDdl(MetaDataParam metaDataParam) {
        List<Table> search = search(metaDataParam);
        if (search.isEmpty()) {
            throw new ServiceException(BaseResponseCodeEnum.DATA_NOT_FOUND, "metadata not exist");
        }
        return search.stream().findFirst().get();
    }


}
