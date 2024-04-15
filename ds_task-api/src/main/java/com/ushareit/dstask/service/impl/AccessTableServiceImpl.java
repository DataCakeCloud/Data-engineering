package com.ushareit.dstask.service.impl;

import com.ushareit.dstask.bean.AccessTable;
import com.ushareit.dstask.bean.UserGroup;
import com.ushareit.dstask.constant.CommonConstant;
import com.ushareit.dstask.mapper.AccessTableMapper;
import com.ushareit.dstask.mapper.UserGroupMapper;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import com.ushareit.dstask.service.AccessTableService;
import com.ushareit.dstask.web.metadata.lakecat.Lakecatutil;
import com.ushareit.dstask.web.utils.CommonUtil;
import com.ushareit.dstask.web.utils.TimestampUtil;
import io.lakecat.catalog.client.LakeCatClient;
import io.lakecat.catalog.common.ObjectType;
import io.lakecat.catalog.common.model.PagedList;
import io.lakecat.catalog.common.model.TableUsageProfile;
import io.lakecat.catalog.common.plugin.request.ListCatalogUsageProfilesRequest;
import io.lakecat.catalog.common.plugin.request.SearchDiscoveryNamesRequest;
//import io.lakecat.catalog.common.plugin.request.input.FilterJsonInput;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author tianxu
 * @date 2023/12/14
 **/
@Slf4j
@Service
public class AccessTableServiceImpl extends AbstractBaseServiceImpl<AccessTable> implements AccessTableService{

    @Value("${catalogName}")
    private String catalogName;

    @Autowired
    private Lakecatutil lakecatutil;

    @Resource
    private AccessTableMapper accessTableMapper;

    @Resource
    private UserGroupMapper userGroupMapper;

    @Resource
    private AccessTableService accessTableService;

    @Resource
    private TimestampUtil timestampUtil;

    @Override
    public CrudMapper<AccessTable> getBaseMapper() {
        return accessTableMapper;
    }

//    @Scheduled(cron = "00 02 * * * ?")
//    @Scheduled(cron = "00 */2 * * * ?")
//    public void scheduledTableStatics() {
//        accessTableService.tableStatistics();
//    }

    private HashMap<String, AccessTable> handleCatalogTableUsage(TableUsageProfile[] objects, String group) {
        Map<String, AccessTable> result = Arrays.stream(objects)
                .collect(Collectors.toMap(
                        data -> String.format("%s.%s.%s",
                                data.getTable().getCatalogName(),
                                data.getTable().getDatabaseName(),
                                data.getTable().getTableName()),
                        data -> {
                            AccessTable accessTable = new AccessTable();
                            accessTable.setTableName(data.getTable().getTableName());
                            accessTable.setDatabaseName(data.getTable().getDatabaseName());
                            accessTable.setCount(data.getSumCount().intValue());
//                            accessTable.setCatalogName(data.getTable().getCatalogName());
                            accessTable.setUserGroup(group);
                            return accessTable;
                        }
                ));
        return (HashMap<String, AccessTable>) result;
    }

    private ArrayList<AccessTable> handleTableInfo(String[] tables, HashMap<String, AccessTable> catalogTableUsage) {
        ArrayList<AccessTable> info = new ArrayList<>();
        for (String table : tables) {
            if (catalogTableUsage.containsKey(table)) {
                info.add(catalogTableUsage.get(table));
            }
        }
        return info;
    }

    public void tableStatistics() {
        ArrayList<Long> yesterday = timestampUtil.getYesterday();
        String stat_date = timestampUtil.formatYesterday(yesterday.get(0));
        try {
            // 获取所有用户组
            UserGroup userGroup = userGroupMapper.selectUserGroupByExists();
            String group = "wangyiren";

//            String catalogName = "ksyun_cn-beijing-6";  # todo 写入配置文件
            String catalogName = "shareit_ue1";
            // 获取指定catalog在指定日期所有表的访问次数
            LakeCatClient lakeCatClient = lakecatutil.getClient();
            String projectId = lakeCatClient.getProjectId();
            ListCatalogUsageProfilesRequest listCatalogUsageProfilesRequest = new ListCatalogUsageProfilesRequest();
            listCatalogUsageProfilesRequest.setProjectId(lakeCatClient.getProjectId());
            listCatalogUsageProfilesRequest.setCatalogName(catalogName);
            listCatalogUsageProfilesRequest.setOpType(CommonConstant.OP_TYPE);
            listCatalogUsageProfilesRequest.setUsageProfileType(0);  // 对应topType: 0倒序、1正序
            listCatalogUsageProfilesRequest.setLimit(CommonConstant.TABLE_LIMIT);  // 默认-1  对应topNum
            listCatalogUsageProfilesRequest.setStartTimestamp(yesterday.get(0));
            listCatalogUsageProfilesRequest.setEndTimestamp(yesterday.get(1));
            PagedList<TableUsageProfile> tableUsageProfilePagedList = lakeCatClient.listCatalogUsageProfiles(listCatalogUsageProfilesRequest);
            HashMap<String, AccessTable> catalogTableUsage = handleCatalogTableUsage(tableUsageProfilePagedList.getObjects(), group);

            // 获取指定用户组下所有的表
            HashMap<String, Object> owner = new HashMap<>();
            owner.put("owner", group);
//            FilterJsonInput filterJsonInput = new FilterJsonInput();
//            filterJsonInput.setFilterJson(owner);
//            SearchDiscoveryNamesRequest searchDiscoveryNamesRequest = new SearchDiscoveryNamesRequest(projectId, "", null, ObjectType.TABLE, filterJsonInput);
            SearchDiscoveryNamesRequest searchDiscoveryNamesRequest = new SearchDiscoveryNamesRequest(projectId, "", null, ObjectType.TABLE, null);
            searchDiscoveryNamesRequest.setLimit(CommonConstant.TABLE_LIMIT);
            PagedList<String> stringPagedList = lakeCatClient.searchDiscoveryNames(searchDiscoveryNamesRequest);
            String[] tables = stringPagedList.getObjects();
            ArrayList<AccessTable> info = handleTableInfo(tables, catalogTableUsage);

            accessTableMapper.insertAccessTable(info, stat_date);
        } catch (Exception e) {
            log.error(String.format("[%s]failed to handle table statistics:%s", stat_date, CommonUtil.printStackTraceToString(e)));
        }
    }
}
