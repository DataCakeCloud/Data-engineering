package com.ushareit.dstask.service.impl;

import cn.hutool.core.net.URLEncodeUtil;
import com.ushareit.dstask.bean.Attachment;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.constant.SymbolEnum;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.mapper.AttachmentMapper;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import com.ushareit.dstask.service.AttachmentService;
import com.ushareit.dstask.web.factory.CloudFactory;
import com.ushareit.dstask.web.utils.cloud.CloudBaseClientUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * @author fengxiao
 * @date 2021/11/1
 */
@Service
public class AttachmentServiceImpl extends AbstractBaseServiceImpl<Attachment> implements AttachmentService {

    @Resource
    private AttachmentMapper attachmentMapper;

    @Resource
    private CloudFactory cloudFactory;


    @Override
    public CrudMapper<Attachment> getBaseMapper() {
        return attachmentMapper;
    }

    @Override
    public String saveList(Collection<Attachment> attachmentList) {
        return CollectionUtils.emptyIfNull(attachmentList).stream().map(item -> {
            save(item);
            return item.getId().toString();
        }).collect(Collectors.joining(SymbolEnum.COMMA.getSymbol()));
    }

    @Override
    public ResponseEntity<Object> download(Integer id) {
        Attachment attachment = attachmentMapper.selectByPrimaryKey(id);
        if (attachment == null) {
            throw new ServiceException(BaseResponseCodeEnum.CLI_PARAM_ILLEGAL.name(), "附件ID不存在");
        }
        String url = attachment.getFileUrl();
        if (StringUtils.isBlank(url)) {
            throw new ServiceException(BaseResponseCodeEnum.OBS_URL_NOT_EXIST.name(), "附件不能为空");
        }

        InputStreamResource resource;
        try {
            CloudBaseClientUtil cloudClientUtil = cloudFactory.getCloudClientUtilByUrl(url);
            FileSystemResource file = new FileSystemResource(cloudClientUtil.download(url));
            resource = new InputStreamResource(file.getInputStream());
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", String.format("attachment;filename=%s;filename*=utf-8''%s",
                    URLEncodeUtil.encode(attachment.getFileName()), URLEncodeUtil.encode(attachment.getFileName())));
            headers.add("Cache-Control", "no-cache,no-store,must-revalidate");

//            headers.add("Content-Disposition", String.format("attachment;filename=%s;filename*=utf-8''%s",
//                    URLEncodeUtil.encode(attachment.getFileName()), URLEncodeUtil.encode(attachment.getFileName())));
            headers.add("Cache-Control", "no-cache,no-store,must-revalidate");
            headers.add("Pragma", "no-cache");
            headers.add("Expires", "0");

            ResponseEntity.BodyBuilder ok = ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(file.contentLength())
                    .contentType(MediaType.parseMediaType(attachment.getContentType()));

            return ok.body(resource);
        } catch (IOException e) {
            throw new ServiceException(BaseResponseCodeEnum.OBS_DOWNLOAD_FAIL);
        }
    }
}
