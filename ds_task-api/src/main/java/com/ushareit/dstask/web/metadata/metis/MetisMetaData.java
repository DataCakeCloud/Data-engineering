package com.ushareit.dstask.web.metadata.metis;

import com.alibaba.fastjson.JSON;
import com.ushareit.dstask.common.param.MetaDataParam;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.third.oidc.OidcService;
import com.ushareit.dstask.utils.HttpUtil;
import com.ushareit.dstask.web.ddl.metadata.Table;
import com.ushareit.dstask.web.metadata.airbyte.AirByteMetaData;
import com.ushareit.dstask.web.metadata.metis.vo.RouteForAtlasVO;
import com.ushareit.dstask.web.vo.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author fengxiao
 * @date 2022/9/19
 */
@Slf4j
@Component
public class MetisMetaData extends AirByteMetaData {

    private static final Pattern TABLE_NAME_PATTERN = Pattern.compile("^([^@]+)@([^@]+)@([^@]+).*");

    @Value("${metis.url}")
    private String metisUrl;

    @Autowired
    private OidcService oidcService;

    public MetisMetaData(MetaDataParam metaDataParam) {
        super(metaDataParam);
    }

    @Override
    public List<Table> search(MetaDataParam metaDataParam) {
        String token = oidcService.getToken("metisApi");
        Map<String, String> params = new HashMap<>();
        params.put("region", metaDataParam.getRegion());

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + token);
        BaseResponse response = HttpUtil.get(metisUrl, params, headers);
        if (response == null || !BaseResponseCodeEnum.SUCCESS.name().equals(response.getCodeStr())) {
            log.error("response is {}", response);
            throw new ServiceException(BaseResponseCodeEnum.SYS_UNA, "获取 metis 数据源失败");
        }

        List<RouteForAtlasVO> metisList = JSON.parseArray(response.getData().toString(), RouteForAtlasVO.class);
        if (CollectionUtils.isEmpty(metisList)) {
            return Collections.emptyList();
        }

        return metisList.stream().map(RouteForAtlasVO::toTable).collect(Collectors.toList());
    }

    @Override
    public Table getDdl(MetaDataParam metaDataParam) {
        Matcher matcher = TABLE_NAME_PATTERN.matcher(metaDataParam.getTable());
        if (!matcher.find()) {
            throw new ServiceException(BaseResponseCodeEnum.SYS_UNA, "Table 信息错误");
        }
        String logStore = matcher.group(1);
        String groupName = matcher.group(2);
        String appName = matcher.group(3);

        String token = oidcService.getToken("metisApi");
        Map<String, String> params = new HashMap<>();
        params.put("region", metaDataParam.getRegion());
        params.put("logStore", logStore);

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + token);
        BaseResponse response = HttpUtil.get(metisUrl, params, headers);
        if (response == null || !BaseResponseCodeEnum.SUCCESS.name().equals(response.getCodeStr())) {
            log.error("response is {}", response);
            throw new ServiceException(BaseResponseCodeEnum.SYS_UNA, "获取 metis 数据源失败");
        }

        List<RouteForAtlasVO> metisList = JSON.parseArray(response.getData().toString(), RouteForAtlasVO.class);
        Optional<RouteForAtlasVO> metisOptional = CollectionUtils.emptyIfNull(metisList).stream()
                .filter(item -> StringUtils.equalsIgnoreCase(item.getLogStore(), logStore) &&
                        StringUtils.equalsIgnoreCase(item.getAppName(), appName) &&
                        StringUtils.equalsIgnoreCase(item.getGroupName(), groupName))
                .findFirst();

        if (!metisOptional.isPresent()) {
            throw new ServiceException(BaseResponseCodeEnum.SYS_UNA, String.format("不存在日志库 %s", metaDataParam.getTable()));
        }

        return metisOptional.get().toTable();
    }
}
