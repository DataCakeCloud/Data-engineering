package com.ushareit.dstask.web.controller;

import com.ushareit.dstask.bean.Label;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.service.BaseService;
import com.ushareit.dstask.service.LabelService;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import com.ushareit.dstask.web.vo.BaseResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/**
 * @author wuyan
 * @date 2021/9/22
 */
@Api(tags = "标签管理")
@RestController
@RequestMapping("/label")
public class LabelController extends BaseBusinessController<Label> {
    @Autowired
    private LabelService labelService;


    @Override
    public BaseService<Label> getBaseService() {
        return labelService;
    }

    @Override
    @ApiOperation(value = "创建标签")
    @ApiResponses({
            @ApiResponse(code = 200, response = BaseResponse.class, message = "成功")
    })
    @PostMapping("/add")
    public BaseResponse add(@RequestBody @Valid Label label) {
        String groupName = getCurrentUser().getGroupName();
        if (StringUtils.isBlank(groupName)) {
            throw new ServiceException(BaseResponseCodeEnum.NO_GROUP);
        }
        label.setTenancyCode(groupName.split(",")[0]);
        return super.add(label);
    }

    @Override
    @GetMapping("/list")
    public BaseResponse list(Label label) {
        label.setCreateBy(InfTraceContextHolder.get().getUserName());
        return BaseResponse.success(labelService.list(label));
    }

    @GetMapping("/count")
    public BaseResponse count(@RequestParam("time") Timestamp time) {
        Integer count = labelService.count(time);
        return BaseResponse.success(count);
    }
}
