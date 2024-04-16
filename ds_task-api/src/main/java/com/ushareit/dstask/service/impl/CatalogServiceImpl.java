package com.ushareit.dstask.service.impl;

import com.ushareit.dstask.bean.Catalog;
import com.ushareit.dstask.bean.DeleteEntity;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.mapper.CatalogMapper;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import com.ushareit.dstask.service.CatalogService;
import com.ushareit.dstask.service.SysDictService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import java.util.List;

/**
 * @author: licg
 * @create: 2021-05-09 10:59
 **/
@Service
@Slf4j
public class CatalogServiceImpl extends AbstractBaseServiceImpl<Catalog> implements CatalogService {
    @Autowired
    private CatalogMapper catalogMapper;

    @Autowired
    private SysDictService dictService;

    @Override
    public CrudMapper<Catalog> getBaseMapper() {
        return catalogMapper;
    }


    @Override
    public List<Catalog> getConfigByCode(@Valid String code) {
        return catalogMapper.select(Catalog.builder().componentCode(code).build());
    }

    @Override
    public void delete(Object id) {
        Catalog catalog = checkExist(id);
        //检测目录是否为空
        if (catalogMapper.selectCount((Catalog)new Catalog().setParentId((Integer)id).setDeleteStatus(DeleteEntity.NOT_DELETE)) > 0) {
            throw new ServiceException(BaseResponseCodeEnum.DELETE_FAIL,"子目录不为空，不可删除！");
        }
        catalog.setDeleteStatus(1);
        super.update(catalog);
    }

    private Catalog checkExist(Object id) {
        Catalog catalog = super.getById(id);
        if (catalog == null || catalog.getDeleteStatus() == 1) {
            throw new ServiceException(BaseResponseCodeEnum.DATA_NOT_FOUND);
        }
        return catalog;
    }

    @Override
    public Object save(Catalog catalogFromWeb) {
        //1.参数预校验
        super.checkOnUpdate(super.getByName(catalogFromWeb.getValue()), catalogFromWeb);

        super.save(catalogFromWeb);
        return catalogFromWeb;
    }

    @Override
    public void update(Catalog catalogFromWeb) {
        //1.ID不为空校验
        if (catalogFromWeb == null || catalogFromWeb.getId() == null) {
            throw new ServiceException(BaseResponseCodeEnum.CLI_PARAM_REQUIRED);
        }

        //2.参数预校验
        super.checkOnUpdate(super.getByName(catalogFromWeb.getValue()), catalogFromWeb);

        super.update(catalogFromWeb);
    }


}
