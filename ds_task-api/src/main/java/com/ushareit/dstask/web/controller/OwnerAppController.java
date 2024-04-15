package com.ushareit.dstask.web.controller;

import com.ushareit.dstask.bean.ActorDefinition;
import com.ushareit.dstask.bean.OwnerApp;
import com.ushareit.dstask.common.vo.OwnerAppVO;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.service.BaseService;
import com.ushareit.dstask.service.OwnerAppService;
import com.ushareit.dstask.third.airbyte.common.vo.DestinationDefinitionRead;
import com.ushareit.dstask.web.ddl.metadata.Table;
import com.ushareit.dstask.web.vo.BaseResponse;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @auther tiyongshuai
 * @data 2024/3/28
 * @description
 */

@Slf4j
@Api(tags = "归属应用")
@RestController
@RequestMapping("/owner_app")
public class OwnerAppController extends BaseBusinessController<OwnerApp>{

    @Autowired
    private OwnerAppService ownerAppService;

    @Override
    public BaseService<OwnerApp> getBaseService() {
        return ownerAppService;
    }

    @GetMapping("getAll")
    public BaseResponse getAll() {
        OwnerApp ownerApp = new OwnerApp();
        ownerApp.setDeleteStatus(0);
        List<OwnerApp> ownerApps = ownerAppService.listByExample(ownerApp);
        List<OwnerAppVO> collect = ownerApps.stream().map(e -> {
            OwnerAppVO ownerAppVO = new OwnerAppVO();
            BeanUtils.copyProperties(e, ownerAppVO);
            return ownerAppVO;
        }).collect(Collectors.toList());
        return BaseResponse.success(collect);
    }
}
