package com.ushareit.dstask.web.controller;

import com.ushareit.dstask.bean.Artifact;
import com.ushareit.dstask.bean.TaskFolderRelation;
import com.ushareit.dstask.bean.UserGroupRelation;
import com.ushareit.dstask.common.vo.ArtifactNameVO;
import com.ushareit.dstask.common.vo.UserGroupVo;
import com.ushareit.dstask.service.ArtifactService;
import com.ushareit.dstask.service.BaseService;
import com.ushareit.dstask.service.UserGroupService;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import com.ushareit.dstask.web.vo.BaseResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author: licg
 * @create: 2021-05-12 15:24
 */
@Api(tags = "工件管理")
@RestController
@RequestMapping("/artifact")
public class ArtifactController extends BaseBusinessController<Artifact> {

    @Autowired
    private ArtifactService artifactService;
    @Autowired
    private UserGroupService userGroupService;

    @Override
    public BaseService<Artifact> getBaseService() {
        return artifactService;
    }

    @Override
    @ApiOperation(value = "创建artifact")
    @ApiResponses({
            @ApiResponse(code = 200, response = BaseResponse.class, message = "成功")
    })
    @PostMapping("/add")
    public BaseResponse<?> add(@Valid Artifact artifact) {
        artifact.setUserGroup(InfTraceContextHolder.get().getUuid());
        BaseResponse baseResponse=super.add(artifact);
        return baseResponse;
    }

    @Override
    @ApiOperation(value = "更新artifact")
    @PostMapping("/update")
    public BaseResponse<?> update(@Valid Artifact artifact) {
        return super.update(artifact);
    }

    @ApiOperation(value = "工件名模糊查询")
    @GetMapping("name/search")
    public BaseResponse<List<ArtifactNameVO>> search(@RequestParam String name) {
        return BaseResponse.success(artifactService.searchByName(name));
    }

    @Override
    @GetMapping("/page")
    public BaseResponse page(@RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "50") Integer pageSize,
                             @RequestParam Map<String, String> paramMap) {
        paramMap.put("userGroupDetail",InfTraceContextHolder.get().getUuid());
//        if(!InfTraceContextHolder.get().getAdmin()){
//
//        }
        return BaseResponse.success(artifactService.page(pageNum, pageSize, paramMap));
    }

}
