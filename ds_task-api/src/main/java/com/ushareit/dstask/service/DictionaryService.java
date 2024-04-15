package com.ushareit.dstask.service;

import com.ushareit.dstask.bean.Dictionary;
import com.ushareit.dstask.common.vo.DictionaryNameVO;

import java.util.List;

/**
 * @author: licg
 * @create: 2021-05-12 15:24
 */
public interface DictionaryService extends BaseService<Dictionary> {

    /**
     * 根据关键字模糊查询
     *
     * @param keywords 关键字
     */
    List<DictionaryNameVO> searchByName(String keywords);
}
