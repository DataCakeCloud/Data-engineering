package com.ushareit.dstask.web.controller;

import com.github.pagehelper.PageInfo;
import com.ushareit.dstask.bean.Feedback;
import com.ushareit.dstask.bean.FeedbackProcessItem;
import com.ushareit.dstask.bean.UserGroupRelation;
import com.ushareit.dstask.common.param.FeedbackSaveParam;
import com.ushareit.dstask.common.param.FeedbackSearchParam;
import com.ushareit.dstask.common.vo.FeedbackVO;
import com.ushareit.dstask.common.vo.UserGroupVo;
import com.ushareit.dstask.service.AttachmentService;
import com.ushareit.dstask.service.BaseService;
import com.ushareit.dstask.service.FeedbackService;
import com.ushareit.dstask.service.UserGroupService;
import com.ushareit.dstask.third.scmp.SCMPService;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import com.ushareit.dstask.web.factory.CloudFactory;
import com.ushareit.dstask.web.vo.BaseResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author fengxiao
 * @date 2021/10/8
 */
@Api(tags = "反馈管理")
@RestController
@RequestMapping("feedback")
public class FeedbackController extends BaseBusinessController<Feedback> {

    @Autowired
    private SCMPService scmbService;

    @Autowired
    private FeedbackService feedbackService;

    @Autowired
    private AttachmentService attachmentService;

    @Resource
    public CloudFactory cloudFactory;
    @Autowired
    private UserGroupService userGroupService;

    @Override
    public BaseService<Feedback> getBaseService() {
        return feedbackService;
    }

    @ApiOperation("增加反馈")
    @PostMapping("save")
    public BaseResponse<?> save(@Valid FeedbackSaveParam param) {
        feedbackService.save(param.toEntity(cloudFactory));
        return BaseResponse.success();
    }

    @ApiOperation("更新反馈")
    @GetMapping("updateAndSelect")
    public BaseResponse<?> updateAndSelect(@Valid Feedback param) {
        return BaseResponse.success(feedbackService.updateAndSelect(param));
    }

    @ApiOperation("受理")
    @PostMapping("accept")
    public BaseResponse<?> accept(@Valid FeedbackProcessItem param) {
        feedbackService.accept(param.getFeedbackId(), null, null);
        return BaseResponse.success();
    }

    @ApiOperation("通过钉钉受理")
    @GetMapping("acceptByDingding")
    public BaseResponse<?> acceptByDingding(@RequestParam("feedbackId") Integer feedbackId, @RequestParam("accept") String accept) {
        feedbackService.accept(feedbackId, "dingding", accept);
        return BaseResponse.success();
    }

    @ApiOperation("转让")
    @PostMapping("assign")
    public BaseResponse<?> assign(@RequestParam("feedbackId") Integer feedbackId,
                                  @RequestParam("assignee") String assignee,
                                  @RequestParam("reason") String reason) {
        feedbackService.assign(feedbackId, assignee, reason, null);
        return BaseResponse.success();
    }

    @ApiOperation("通过钉钉转让")
    @GetMapping("assignByDingding")
    public BaseResponse<?> assignByDingding(@RequestParam("feedbackId") Integer feedbackId,
                                  @RequestParam("module") String module) {
        feedbackService.assign(feedbackId, null, "通过钉钉转让", module);
        return BaseResponse.success();
    }

    @ApiOperation("修改解决方案")
    @PostMapping("modify")
    public BaseResponse<?> modify(@RequestParam("feedbackId") Integer feedbackId,
                                  @RequestParam("reason") String reason) {
        feedbackService.modify(feedbackId, reason);
        return BaseResponse.success();
    }

    @ApiOperation("关闭")
    @PostMapping("close")
    public BaseResponse<?> close(@Valid FeedbackProcessItem param) {
        feedbackService.close(param.getFeedbackId(), param.getReason());
        return BaseResponse.success();
    }

    @ApiOperation("工单重开")
    @GetMapping("reopen")
    public BaseResponse<?> reopen(@Valid FeedbackProcessItem param) {
        return BaseResponse.success(feedbackService.reopen(param));
    }


    @ApiOperation("评分")
    @PostMapping("score")
    public BaseResponse<?> score(@Valid FeedbackProcessItem param) {
        feedbackService.score(param.getFeedbackId(), param.getScore());
        return BaseResponse.success();
    }

    @ApiOperation(value = "反馈分页列表")
    @GetMapping("pages")
    public BaseResponse<PageInfo<FeedbackVO>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                                   @RequestParam(defaultValue = "50") Integer pageSize,
                                                   @Valid @ModelAttribute FeedbackSearchParam param) {
        Example example = param.toExample();
        if(!InfTraceContextHolder.get().getAdmin()){
            Example.Criteria criteria = new Example(Feedback.class).createCriteria();
            criteria.andEqualTo("userGroup",InfTraceContextHolder.get().getUuid());
            example.and(criteria);
        }
        PageInfo<FeedbackVO> page = feedbackService.page(pageNum, pageSize, example);
        return BaseResponse.success(page);
    }

    @ApiOperation("导出")
    @GetMapping("export")
    public void export(@Valid @ModelAttribute FeedbackSearchParam param,
                                  HttpServletResponse response) {
        feedbackService.export(param, response);
    }


}
