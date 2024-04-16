package com.ushareit.dstask.mapper;

import com.ushareit.dstask.bean.Attachment;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author fengxiao
 * @date 2021/11/1
 */
@Mapper
public interface AttachmentMapper extends CrudMapper<Attachment> {
}
