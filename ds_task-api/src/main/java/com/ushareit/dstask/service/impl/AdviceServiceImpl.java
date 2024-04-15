package com.ushareit.dstask.service.impl;


import com.ushareit.dstask.bean.Advice;
import com.ushareit.dstask.bean.Attachment;
import com.ushareit.dstask.bean.CloudResouce;
import com.ushareit.dstask.mapper.AdviceMapper;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import com.ushareit.dstask.service.AdvicekService;
import com.ushareit.dstask.service.AttachmentService;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import com.ushareit.dstask.web.factory.CloudFactory;
import com.ushareit.dstask.web.utils.DataCakeConfigUtil;
import com.ushareit.dstask.web.utils.cloud.CloudBaseClientUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.ArrayList;


/**
 * @author xuebotao
 * @date 2022-08-10
 */
@Service
public class AdviceServiceImpl extends AbstractBaseServiceImpl<Advice> implements AdvicekService {

    @Resource
    private AttachmentService attachmentService;

    @Resource
    private AdviceMapper adviceMapper;

    @Resource
    public CloudFactory cloudFactory;

    @Override
    public CrudMapper<Advice> getBaseMapper() {
        return adviceMapper;
    }

    @Override
    public Object save(Advice advice) {

        Integer tenantId = InfTraceContextHolder.get().getTenantId();
        advice.setTenantId(tenantId);

        advice.setAttachmentLists(new ArrayList<>());
        for (MultipartFile attachment : CollectionUtils.emptyIfNull(advice.getMultipartFileLists())) {
            if (attachment != null && !attachment.isEmpty()) {
                CloudResouce.DataResource defaultRegionConfig = DataCakeConfigUtil.getCloudResourcesService().getDefaultRegionConfig();
                CloudBaseClientUtil cloudClientUtil = cloudFactory.getCloudClientUtil(defaultRegionConfig.getRegionAlias());
                String url = cloudClientUtil.upload(attachment, "feedback", defaultRegionConfig.getRegionAlias());
                advice.getAttachmentLists().add(new Attachment()
                        .setFileUrl(url)
                        .setFileName(attachment.getOriginalFilename())
                        .setFileSize(attachment.getSize())
                        .setContentType(attachment.getContentType()));
            }
        }
        String attachIds = attachmentService.saveList(advice.getAttachmentLists());
        advice.setAttachmentIds(attachIds);
        advice.setCreateBy(InfTraceContextHolder.get().getUserName());
        advice.setUpdateBy(InfTraceContextHolder.get().getUserName());
        advice.setCreateTime(new Timestamp(System.currentTimeMillis()));
        return adviceMapper.insertSelective(advice);
    }
}
