package com.ushareit.dstask.service.impl;
import com.ushareit.dstask.bean.AccessTenantProduct;
import com.ushareit.dstask.mapper.AccessTenantProductMapper;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import com.ushareit.dstask.service.AccessTenantProductService;
import com.ushareit.dstask.service.AccessTenantService;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author wuyan
 * @date 2022/4/11
 */
@Slf4j
@Service
public class AccessTenantProductServiceImpl extends AbstractBaseServiceImpl<AccessTenantProduct> implements AccessTenantProductService {
    @Resource
    private AccessTenantProductMapper accessTenantProductMapper;

    @Resource
    private AccessTenantService accessTenantService;


    @Override
    public CrudMapper<AccessTenantProduct> getBaseMapper() {
        return accessTenantProductMapper;
    }


    @Override
    public Object save(AccessTenantProduct accessTenantProduct) {
        // 1. 检查租户是否冻结或删除
        accessTenantService.checkExist(accessTenantProduct.getTenantId());
        accessTenantProduct.setCreateBy(InfTraceContextHolder.get().getUserName());
        accessTenantProduct.setUpdateBy(InfTraceContextHolder.get().getUserName());

        // TODO 2. 检查产品是否删除


        // 3. 存储
        super.save(accessTenantProduct);
        return accessTenantProduct;
    }


    @Override
    public List<AccessTenantProduct> getByTenantId(Integer id) {
        List<AccessTenantProduct> accessTenantProducts = accessTenantProductMapper.selectByTenantId(id);
        return accessTenantProducts;
    }

    @Override
    public void deleteByTenantId(Integer tenantId) {
        accessTenantProductMapper.deleteByTenantId(tenantId);
    }

    @Override
    public void insertList(List<AccessTenantProduct> accessTenantProductList) {
        accessTenantProductMapper.insertList(accessTenantProductList);
    }


}
