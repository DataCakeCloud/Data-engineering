package com.ushareit.dstask.service.impl;

import com.ushareit.dstask.bean.HiveTable;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.mapper.HiveTablesMapper;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import com.ushareit.dstask.service.HiveTablesService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.util.Preconditions;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author wuyan
 * @date 2021/8/26
 */
@Slf4j
@Service
public class HiveTablesServiceImpl extends AbstractBaseServiceImpl<HiveTable> implements HiveTablesService {
    @Resource
    private HiveTablesMapper hiveTablesMapper;

    @Override
    public CrudMapper<HiveTable> getBaseMapper() {
        return hiveTablesMapper;
    }

    @Override
    public Object save(HiveTable hiveTable) {
        //1.参数预校验
        preCheckCommon(hiveTable);
        super.save(hiveTable);
        return hiveTable;
    }

    @Override
    public void update(HiveTable hiveTable) {
        //1.ID不为空校验
        if (hiveTable == null || hiveTable.getId() == null) {
            throw new ServiceException(BaseResponseCodeEnum.CLI_PARAM_REQUIRED);
        }
        //2.参数预校验
        preCheckCommon(hiveTable);
        super.update(hiveTable);
    }

    @Override
    public HiveTable getById(Object id) {
        HiveTable hiveTable = hiveTablesMapper.selectByTaskId(Integer.parseInt(id.toString()));
        return hiveTable;
    }

    private void preCheckCommon(HiveTable hiveTable) {
        Preconditions.checkArgument(StringUtils.isNotEmpty(hiveTable.getPath()), "路径不能为空");
        Preconditions.checkArgument(StringUtils.isNotEmpty(hiveTable.getCatalog()), "catalog不能为空");
        Preconditions.checkArgument(StringUtils.isNotEmpty(hiveTable.getDatabase()), "database不能为空");
        Preconditions.checkArgument(StringUtils.isNotEmpty(hiveTable.getMode()), "mode不能为空");
        Preconditions.checkArgument(StringUtils.isNotEmpty(hiveTable.getName()), "表名不能为空");
        Preconditions.checkArgument(StringUtils.isNotEmpty(hiveTable.getProvider()), "provider不能为空");
        Preconditions.checkArgument(StringUtils.isNotEmpty(hiveTable.getTaskId().toString()), "任务id不能为空");
        Preconditions.checkArgument(StringUtils.isNotEmpty(hiveTable.getRegion()), "region不能为空");
    }
}
