package com.ushareit.dstask.web.controller;

import com.ushareit.dstask.bean.BaseEntity;
import com.ushareit.dstask.bean.DataEntity;
import com.ushareit.dstask.bean.DeleteEntity;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.service.BaseService;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import com.ushareit.dstask.web.vo.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.sql.Timestamp;
import java.util.Map;

/**
 * @author zhaopan
 * @date 2018/10/30
 **/
@Slf4j
public abstract class BaseBusinessController<T extends BaseEntity> extends BaseController {

    /**
     * get base service
     *
     * @return base service
     */
    public abstract BaseService<T> getBaseService();

    @GetMapping("/list")
    public BaseResponse list(T t) {
        // 添加信息
        if (t instanceof DeleteEntity) {
            DeleteEntity deleteEntity = (DeleteEntity)t;
            deleteEntity.setDeleteStatus(0);
        }
        return BaseResponse.success(getBaseService().listByExample(t));
    }

    @GetMapping("/page")
    public BaseResponse page(@RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "50") Integer pageSize,
                             @RequestParam Map<String, String> paramMap) {

        return BaseResponse.success(getBaseService().listByPage(pageNum, pageSize, paramMap));
    }

    @GetMapping("/get")
    public BaseResponse getById(@RequestParam(required = false) Object id, @RequestParam(required = false) String name,
                                @RequestParam(required = false) Integer version) {
        if (id != null) {
            return BaseResponse.success(getBaseService().getById(id));
        }

        if (StringUtils.isNoneEmpty(name)) {
            return BaseResponse.success(getBaseService().getByName(name));
        }

        if (getBaseService().getById(id) == null) {
            return BaseResponse.error(BaseResponseCodeEnum.DATA_NOT_FOUND);
        }

        return BaseResponse.success(getBaseService().getById(id));
    }

    @PostMapping("/add")
    public BaseResponse add(@RequestBody @Valid T t) {
        // 添加user信息
        if (t instanceof DataEntity) {
            DataEntity dataEntity = (DataEntity) t;
            dataEntity.setCreateTime(new Timestamp(System.currentTimeMillis()));
            dataEntity.setCreateBy(getCurrentUser().getUserName());
            dataEntity.setUpdateTime(new Timestamp(System.currentTimeMillis()));
            dataEntity.setUpdateBy(getCurrentUser().getUserName());

        }
        Object save = getBaseService().save(t);
        return BaseResponse.success(BaseResponseCodeEnum.SUCCESS, save);
    }

    @PutMapping("/update")
    public BaseResponse update(@RequestBody @Valid T t) {
        // 添加user信息
        if (t instanceof DataEntity) {
            DataEntity dataEntity = (DataEntity) t;
            dataEntity.setUpdateTime(new Timestamp(System.currentTimeMillis()));
            dataEntity.setUpdateBy(super.getCurrentUser().getUserName());
        }

        try {
            getBaseService().update(t);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return BaseResponse.error(BaseResponseCodeEnum.FAILED_UPDATE.name(), e.getMessage());
        }


        return BaseResponse.success(BaseResponseCodeEnum.SUCCESS);
    }

    @DeleteMapping("/delete")
    public BaseResponse delete(@RequestParam @Valid Object id) {
        try {
            getBaseService().delete(id);
        } catch (ServiceException e) {
            return BaseResponse.error(e.getCodeStr(), e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return BaseResponse.error(BaseResponseCodeEnum.CLI_DELETE_ILLEGAL, e);
        }
        return BaseResponse.success();
    }
}
