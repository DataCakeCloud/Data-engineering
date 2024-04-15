package com.ushareit.dstask.service.impl;

import com.ushareit.dstask.bean.AccessProduct;
import com.ushareit.dstask.bean.AccessTenant;
import com.ushareit.dstask.bean.DeleteEntity;
import com.ushareit.dstask.mapper.AccessProductMapper;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import com.ushareit.dstask.service.AccessProductService;
import com.ushareit.dstask.service.AccessTenantService;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import com.ushareit.dstask.web.utils.DataCakeConfigUtil;
import com.ushareit.dstask.web.vo.BaseResponse;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.List;

/**
 * @author wuyan
 * @date 2022/4/13
 */
@Slf4j
@Service
public class AccessProductServiceImpl extends AbstractBaseServiceImpl<AccessProduct> implements AccessProductService {


    @Resource
    private AccessTenantService accessTenantService;

    @Resource
    private AccessProductMapper accessProductMapper;

    @Override
    public CrudMapper<AccessProduct> getBaseMapper() {
        return accessProductMapper;
    }

    @ApiOperation(value = "租户管理中配置资源接口")
    @ApiResponses({
            @ApiResponse(code = 200, response = BaseResponse.class, message = "成功")
    })
    @Override
    public List<AccessProduct> getConfig(Integer tenantId) {
        // 查找所有资源
        if (tenantId == null) {
            return accessProductMapper.selectAll();
        }

        AccessTenant accessTenant = accessTenantService.checkExist(tenantId);

        InfTraceContextHolder.get().setTenantName(accessTenant.getName());
        InfTraceContextHolder.get().setTenantId(accessTenant.getId());

        // 查找租户选中的资源
        return accessProductMapper.selectByTenantId(tenantId);
    }

    @Override
    public void initProducts(String tenantName) {
        InfTraceContextHolder.get().setTenantName(DataCakeConfigUtil.getDataCakeSourceConfig().getSuperTenant());
        Example example = new Example(AccessProduct.class);
        example.or()
                .andEqualTo("deleteStatus", DeleteEntity.NOT_DELETE);
        List<AccessProduct> accessProductList = accessProductMapper.selectByExample(example);

        InfTraceContextHolder.get().setTenantName(tenantName);
        accessProductList.stream().peek(item -> {
            item.setCreateBy("admin");
            item.setUpdateBy("admin");
            item.setCreateTime(new Timestamp(System.currentTimeMillis()));
            item.setCreateTime(new Timestamp(System.currentTimeMillis()));
        }).forEach(accessProductMapper::insertSelective);
    }
}
