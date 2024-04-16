package com.ushareit.dstask.web.controller;

import com.ushareit.dstask.bean.ArtifactVersion;
import com.ushareit.dstask.bean.DeleteEntity;
import com.ushareit.dstask.constant.DsTaskConstant;
import com.ushareit.dstask.service.ArtifactVersionService;
import com.ushareit.dstask.service.BaseService;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import com.ushareit.dstask.web.vo.BaseResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * @author: licg
 * @create: 2021-05-12 15:24
 */
@Api(tags = "工件版本管理")
@RestController
@RequestMapping("/artifactversion")
public class ArtifactVersionController extends BaseBusinessController<ArtifactVersion> {

    @Autowired
    private ArtifactVersionService artifactVersionService;

    @Override
    public BaseService<ArtifactVersion> getBaseService() {
        return artifactVersionService;
    }

    @ApiOperation(value = "下载")
    @GetMapping("/download")
    public ResponseEntity<Object> download(@RequestParam("artifactVersionId") Integer artifactVersionId,
                                           @RequestParam("tenantId") Integer tenantId) throws IOException {
        return artifactVersionService.download(artifactVersionId, tenantId);
    }
}
