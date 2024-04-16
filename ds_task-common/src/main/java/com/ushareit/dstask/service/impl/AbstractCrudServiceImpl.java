package com.ushareit.dstask.service.impl;

import cn.hutool.core.util.ReflectUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ushareit.dstask.bean.BaseEntity;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.constant.SymbolEnum;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import com.ushareit.dstask.service.CrudService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Abstract base service
 *
 * @author Much
 * @date 2018/10/26
 */
@Slf4j
public abstract class AbstractCrudServiceImpl<T extends BaseEntity> implements CrudService<T> {

    /**
     * get base mapper
     *
     * @return base mapper
     */
    public abstract CrudMapper<T> getBaseMapper();

    @Override
    public List<T> listByIds(Stream<Integer> ids) {
        String idsString = StringUtils.join(ids.iterator(), SymbolEnum.COMMA.getSymbol());
        if (StringUtils.isBlank(idsString)) {
            return Collections.emptyList();
        }

        return getBaseMapper().selectByIds(idsString);
    }

    @Override
    public List<T> listByIds(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return listByIds(ids.stream());
    }

    @Override
    public Map<Integer, T> mapByIds(Stream<Integer> ids) {
        return listByIds(ids).stream().collect(Collectors.toMap(T::getId, Function.identity()));
    }

    @Override
    public List<T> listByExample(T t) {
        return getBaseMapper().select(t);
    }

    @Override
    public List<T> listByExample(Example e) {
        return getBaseMapper().selectByExample(e);
    }

    public List<T> listByMap(Map<String, String> paramMap) {
        return getBaseMapper().listByMap(paramMap);
    }

    @Override
    public PageInfo<T> listByPage(int pageNum, int pageSize, T t) {
        PageHelper.startPage(pageNum, pageSize);
        List<T> pageRecord = getBaseMapper().select(t);
        return new PageInfo<>(pageRecord);
    }

    @Override
    public PageInfo<T> listByPage(int pageNum, int pageSize, T t, String sortedField, boolean desc) {
        PageHelper.startPage(pageNum, pageSize);
        Example example = new Example(t.getClass());
        Example.Criteria criteria = example.or();

        Arrays.stream(ReflectUtil.getFields(t.getClass()))
                .peek(field -> field.setAccessible(true))
                .filter(field -> {
                    try {
                        return field.get(t) != null
                                && !Modifier.isStatic(field.getModifiers()) // 非 static
                                && !Modifier.isFinal(field.getModifiers()); // 非 final
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        return false;
                    }
                })
                .forEach(field -> {
                    try {
                        criteria.andEqualTo(field.getName(), field.get(t));
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                });

        example.setOrderByClause(sortedField + StringUtils.SPACE + (desc ? "DESC" : "ASC"));
        List<T> pageRecord = getBaseMapper().selectByExample(example);
        return new PageInfo<>(pageRecord);
    }

    @Override
    public PageInfo<T> listByPage(int pageNum, int pageSize, Map<String, String> paramMap) {
        PageHelper.startPage(pageNum, pageSize);
        List<T> pageRecord = getBaseMapper().listByMap(paramMap);
        return new PageInfo<>(pageRecord);
    }

    @Override
    public PageInfo<T> listByPage(int pageNum, int pageSize, Example example) {
        PageHelper.startPage(pageNum, pageSize);
        List<T> pageRecord = getBaseMapper().selectByExample(example);
        return new PageInfo<>(pageRecord);
    }

    @Override
    public T getById(Object id) {
        return getBaseMapper().selectByPrimaryKey(id);
    }

    @Override
    public T getByName(String name) {
        return getBaseMapper().selectByName(name);
    }

    @Override
    public T selectOne(T t) {
        return getBaseMapper().selectOne(t);
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public Object save(T t) {
        return getBaseMapper().insertSelective(t);
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void save(List<T> t) {
        getBaseMapper().insertList(t);
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void update(T t) {
        if (t.getId() == null) {
            throw new ServiceException(BaseResponseCodeEnum.CLI_ID_NOTNULL.name(), "更新数据时ID不能为空");
        }
        getBaseMapper().updateByPrimaryKeySelective(t);
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public void update(List<T> tList) {
        batchUpdate(tList);
    }

    private void batchUpdate(List<T> tList) {
        for (T t : tList) {
            getBaseMapper().updateByPrimaryKeySelective(t);
        }
    }

    @Override
    public void delete(Object id) {
        getBaseMapper().deleteByPrimaryKey(id);
    }


    public <T> PageInfo<T> getPageInfo(int currentPage, int pageSize, List<T> list) {
        if (list == null) {
            list = new ArrayList<>();
        }
        int total = list.size();
        if (total > pageSize) {
            int toIndex = pageSize * currentPage;
            if (toIndex > total) {
                toIndex = total;
            }
            list = list.subList(pageSize * (currentPage - 1), toIndex);
        }
        Page<T> page = new Page<>(currentPage, pageSize);
        page.addAll(list);
        page.setPages((total + pageSize - 1) / pageSize);
        page.setTotal(total);

        PageInfo<T> pageInfo = new PageInfo<>(page);
        return pageInfo;
    }


}
