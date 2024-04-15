package com.ushareit.dstask.web.controller;

import com.ushareit.dstask.service.AttachmentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author fengxiao
 * @date 2021/11/1
 */
@Api(tags = "附件管理")
@RestController
@RequestMapping("attachment")
public class AttachmentController {

    @Autowired
    private AttachmentService attachmentService;

    @ApiOperation(value = "下载附件")
    @GetMapping("download")
    public ResponseEntity<Object> download(@RequestParam("id") Integer attachmentId) {
        return attachmentService.download(attachmentId);
    }

}
