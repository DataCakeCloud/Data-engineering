package com.ushareit.dstask.web.controller;

import com.ushareit.dstask.bean.SysDict;
import com.ushareit.dstask.service.BaseService;
import com.ushareit.dstask.service.SysDictService;
import com.ushareit.dstask.web.vo.BaseResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * @author: licg
 * @create: 2021-05-12 15:24
 */
@Api(tags = "数据字典管理")
@RestController
@RequestMapping("/sysdict")
public class SysDictController extends BaseBusinessController<SysDict> {

    @Autowired
    private SysDictService sysDictService;

    @Override
    public BaseService<SysDict> getBaseService() {
        return sysDictService;
    }

    @ApiOperation("根据parentCode获取配置组织成map信息")
    @GetMapping(value = "/mapByParentCode")
    public BaseResponse<?> map(String parentCode) {
        return BaseResponse.success(sysDictService.getConfigByParentCode(parentCode));
    }

    @ApiOperation("根据parentCode获取配置组织成map信息")
    @GetMapping(value = "/getTemplateList")
    public BaseResponse<?> getTemplateList() {
        return BaseResponse.success(sysDictService.getTemplateList());
    }
}
