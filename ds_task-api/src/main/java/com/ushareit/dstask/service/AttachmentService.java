package com.ushareit.dstask.service;

import com.ushareit.dstask.bean.Attachment;
import org.springframework.http.ResponseEntity;

import java.util.Collection;

/**
 * @author fengxiao
 * @date 2021/11/1
 */
public interface AttachmentService extends BaseService<Attachment> {

    /**
     * 保存多个附件，并返回ID集合
     *
     * @param attachmentList 附件列表
     * @return ID集合，以逗号分隔
     */
    String saveList(Collection<Attachment> attachmentList);

    /**
     * 下载附件
     *
     * @param id 反馈ID
     */
    ResponseEntity<Object> download(Integer id);
}
