package com.ushareit.dstask.web.controller;

import com.github.pagehelper.PageInfo;
import com.ushareit.dstask.bean.ActorDefinition;
import com.ushareit.dstask.bean.DeleteEntity;
import com.ushareit.dstask.constant.ActorTypeEnum;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.constant.IsOpenEnum;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.service.ActorDefinitionService;
import com.ushareit.dstask.third.airbyte.common.param.SourceDefinitionCreate;
import com.ushareit.dstask.third.airbyte.common.param.SourceDefinitionSearch;
import com.ushareit.dstask.third.airbyte.common.param.SourceDefinitionUpdate;
import com.ushareit.dstask.third.airbyte.common.vo.SourceDefinitionRead;
import com.ushareit.dstask.third.airbyte.common.vo.SourceDefinitionSpecificationRead;
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
@Api(tags = "数据 source 定义管理")
@RestController
@RequestMapping("actor/source_definitions")
public class SourceDefinitionController {

    @Autowired
    private ActorDefinitionService actorDefinitionService;

    @Resource
    public CloudFactory cloudFactory;

    @PostMapping("create")
    public BaseResponse<?> create(@Valid SourceDefinitionCreate sourceDefinitionCreate) {
        return BaseResponse.success(actorDefinitionService.save(sourceDefinitionCreate.toAddEntity(cloudFactory)));
    }

    @PostMapping("update")
    public BaseResponse<?> update(@Valid SourceDefinitionUpdate sourceDefinitionUpdate) {
        actorDefinitionService.update(sourceDefinitionUpdate.toUpdateEntity());
        return BaseResponse.success();
    }

    @PostMapping("open")
    public BaseResponse<?> open(@RequestParam("sourceDefinitionId") Integer sourceDefinitionId,
                                @RequestParam("isOpen") Integer isOpen) {
        ActorDefinition actorDefinition = actorDefinitionService.getById(sourceDefinitionId);
        if (actorDefinition == null) {
            throw new ServiceException(BaseResponseCodeEnum.SYS_ERR.name(), "数据源定义信息获取失败");
        }
        IsOpenEnum.validate(isOpen);

        ActorDefinition toUpdateParam = new ActorDefinition();
        toUpdateParam.setId(sourceDefinitionId);
        toUpdateParam.setIsOpen(isOpen);

        actorDefinitionService.update(toUpdateParam);
        return BaseResponse.success();
    }

    @PostMapping("delete")
    public BaseResponse<?> delete(@RequestParam("sourceDefinitionId") Integer sourceDefinitionId) {
        actorDefinitionService.delete(sourceDefinitionId);
        return BaseResponse.success();
    }

    @GetMapping("get")
    public BaseResponse<SourceDefinitionRead> get(@RequestParam("sourceDefinitionId") Integer sourceDefinitionId) {
        ActorDefinition actorDefinition = actorDefinitionService.getById(sourceDefinitionId);
        if (actorDefinition == null) {
            throw new ServiceException(BaseResponseCodeEnum.SYS_ERR.name(), "数据源定义信息获取失败");
        }
        return BaseResponse.success(new SourceDefinitionRead(actorDefinition));
    }

    @GetMapping("page")
    public BaseResponse<PageInfo<SourceDefinitionRead>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                                             @RequestParam(defaultValue = "50") Integer pageSize,
                                                             @Valid @ModelAttribute SourceDefinitionSearch param) {
        PageInfo<ActorDefinition> pageInfo = actorDefinitionService.listByPage(pageNum, pageSize, param.toExample());
        return BaseResponse.success(PageUtils.map(pageInfo, SourceDefinitionRead::new));
    }

    @GetMapping("list")
    public BaseResponse<List<SourceDefinitionRead>> list() {
        Example example = new Example(ActorDefinition.class);
        Example.Criteria criteria = example.or();
        criteria.andEqualTo("deleteStatus", DeleteEntity.NOT_DELETE);
        criteria.andEqualTo("actorType", ActorTypeEnum.source);
        criteria.andEqualTo("isOpen", 1);

        List<ActorDefinition> pageList = actorDefinitionService.listByExample(example);
        return BaseResponse.success(pageList.stream().map(SourceDefinitionRead::new).collect(Collectors.toList()));
    }

    @GetMapping("spec")
    public BaseResponse<SourceDefinitionSpecificationRead> spec(@RequestParam("sourceDefinitionId") Integer sourceDefinitionId) {
        ActorDefinition actorDefinition = actorDefinitionService.getById(sourceDefinitionId);
        if (actorDefinition == null) {
            throw new ServiceException(BaseResponseCodeEnum.SYS_ERR.name(), "数据源定义信息获取失败");
        }
        return BaseResponse.success(new SourceDefinitionSpecificationRead(actorDefinition));
    }

}
