package com.ushareit.dstask.service.impl;

import com.ushareit.dstask.bean.Dictionary;
import com.ushareit.dstask.common.vo.DictionaryNameVO;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.mapper.DictionaryMapper;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import com.ushareit.dstask.service.DictionaryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author: licg
 * @create: 2021-05-12 15:24
 */
@Slf4j
@Service
public class DictionaryServiceImpl extends AbstractBaseServiceImpl<Dictionary> implements DictionaryService {

    @Resource
    private DictionaryMapper dictionaryMapper;

    @Override
    public CrudMapper<Dictionary> getBaseMapper() {
        return dictionaryMapper;
    }

    @Override
    public void delete(Object id) {
        Dictionary dictionary = checkDictionaryExist(id);
        dictionary.setDeleteStatus(1);
        super.update(dictionary);
    }

    private Dictionary checkDictionaryExist(Object id) {
        Dictionary dictionary = super.getById(id);
        if (dictionary == null || dictionary.getDeleteStatus() == 1) {
            throw new ServiceException(BaseResponseCodeEnum.DATA_NOT_FOUND, BaseResponseCodeEnum.DATA_NOT_FOUND.getMessage());
        }
        return dictionary;
    }

    @Override
    public List<DictionaryNameVO> searchByName(String keywords) {
        Example example = new Example(Dictionary.class);
        if (StringUtils.isNotBlank(keywords)) {
            example.or().andLike("chineseName", "%" + keywords + "%");
        }

        return listByExample(example).stream()
                .map(DictionaryNameVO::new)
                .sorted(Comparator.comparing(DictionaryNameVO::getName))
                .collect(Collectors.toList());
    }
}
