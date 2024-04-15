package com.ushareit.dstask.web.controller;

import com.github.pagehelper.PageInfo;
import com.ushareit.dstask.bean.ActorDefinition;
import com.ushareit.dstask.bean.DeleteEntity;
import com.ushareit.dstask.constant.ActorTypeEnum;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.constant.IsOpenEnum;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.service.ActorDefinitionService;
import com.ushareit.dstask.third.airbyte.common.param.DestinationDefinitionCreate;
import com.ushareit.dstask.third.airbyte.common.param.DestinationDefinitionSearch;
import com.ushareit.dstask.third.airbyte.common.param.DestinationDefinitionUpdate;
import com.ushareit.dstask.third.airbyte.common.vo.DestinationDefinitionRead;
import com.ushareit.dstask.third.airbyte.common.vo.DestinationDefinitionSpecificationRead;
import com.ushareit.dstask.web.factory.CloudFactory;
import com.ushareit.dstask.web.utils.PageUtils;
import com.ushareit.dstask.web.vo.BaseResponse;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author fengxiao
 * @date 2022/7/22
 */
@Api(tags = "数据 destination 定义管理")
@RestController
@RequestMapping("actor/destination_definitions")
public class DestinationDefinitionController {

    @Autowired
    private ActorDefinitionService actorDefinitionService;

    @Resource
    public CloudFactory cloudFactory;

    @PostMapping("create")
    public BaseResponse<?> create(@Valid DestinationDefinitionCreate destinationDefinitionCreate) {
        return BaseResponse.success(actorDefinitionService.save(destinationDefinitionCreate.toAddEntity(cloudFactory)));
    }

    @PostMapping("update")
    public BaseResponse<?> update(@Valid DestinationDefinitionUpdate destinationDefinitionUpdate) {
        actorDefinitionService.update(destinationDefinitionUpdate.toUpdateEntity(cloudFactory));
        return BaseResponse.success();
    }

    @PostMapping("open")
    public BaseResponse<?> open(@RequestParam("destinationDefinitionId") Integer destinationDefinitionId,
                                @RequestParam("isOpen") Integer isOpen) {
        ActorDefinition actorDefinition = actorDefinitionService.getById(destinationDefinitionId);
        if (actorDefinition == null) {
            throw new ServiceException(BaseResponseCodeEnum.SYS_ERR.name(), "数据源定义信息获取失败");
        }
        IsOpenEnum.validate(isOpen);

        ActorDefinition toUpdateParam = new ActorDefinition();
        toUpdateParam.setId(destinationDefinitionId);
        toUpdateParam.setIsOpen(isOpen);

        actorDefinitionService.update(toUpdateParam);
        return BaseResponse.success();
    }

    @PostMapping("delete")
    public BaseResponse<?> delete(@RequestParam("destinationDefinitionId") Integer destinationDefinitionId) {
        actorDefinitionService.delete(destinationDefinitionId);
        return BaseResponse.success();
    }

    @GetMapping("get")
    public BaseResponse<DestinationDefinitionRead> get(@RequestParam("destinationDefinitionId") Integer destinationDefinitionId) {
        ActorDefinition actorDefinition = actorDefinitionService.getById(destinationDefinitionId);
        if (actorDefinition == null) {
            throw new ServiceException(BaseResponseCodeEnum.SYS_ERR.name(), "数据定义信息获取失败");
        }
        return BaseResponse.success(new DestinationDefinitionRead(actorDefinition));
    }

    @GetMapping("page")
    public BaseResponse<PageInfo<DestinationDefinitionRead>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                                                  @RequestParam(defaultValue = "50") Integer pageSize,
                                                                  @Valid @ModelAttribute DestinationDefinitionSearch param) {
        PageInfo<ActorDefinition> pageInfo = actorDefinitionService.listByPage(pageNum, pageSize, param.toExample());
        return BaseResponse.success(PageUtils.map(pageInfo, DestinationDefinitionRead::new));
    }

    @GetMapping("list")
    public BaseResponse<List<DestinationDefinitionRead>> list() {
        Example example = new Example(ActorDefinition.class);
        Example.Criteria criteria = example.or();
        criteria.andEqualTo("deleteStatus", DeleteEntity.NOT_DELETE);
        criteria.andEqualTo("actorType", ActorTypeEnum.destination);
        criteria.andEqualTo("isOpen", 1);

        List<ActorDefinition> pageList = actorDefinitionService.listByExample(example);
        return BaseResponse.success(pageList.stream().map(DestinationDefinitionRead::new).collect(Collectors.toList()));
    }

    @GetMapping("spec")
    public BaseResponse<DestinationDefinitionSpecificationRead> spec(@RequestParam("destinationDefinitionId") Integer destinationDefinitionId) {
        ActorDefinition actorDefinition = actorDefinitionService.getById(destinationDefinitionId);
        if (actorDefinition == null) {
            throw new ServiceException(BaseResponseCodeEnum.SYS_ERR.name(), "数据源定义信息获取失败");
        }
        return BaseResponse.success(new DestinationDefinitionSpecificationRead(actorDefinition));
    }

}
