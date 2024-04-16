package com.ushareit.dstask.service.impl;

import com.ushareit.dstask.bean.Label;
import com.ushareit.dstask.bean.LabelCollect;
import com.ushareit.dstask.bean.SysDict;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.constant.SymbolEnum;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.mapper.LabelCollectMapper;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import com.ushareit.dstask.service.LabelCollectService;
import com.ushareit.dstask.service.LabelService;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author wuyan
 * @date 2022/1/19
 */
@Slf4j
@Service
public class LabelCollectServiceImpl extends AbstractBaseServiceImpl<LabelCollect> implements LabelCollectService {
    @Resource
    private LabelCollectMapper labelCollectMapper;

    @Resource
    private LabelService labelService;

    @Override
    public CrudMapper<LabelCollect> getBaseMapper() {
        return labelCollectMapper;
    }

    @Override
    public LabelCollect collect(Integer labelId) {
        String current = InfTraceContextHolder.get().getUserName();
        LabelCollect collect = labelCollectMapper.selectByUser(current);
        if (collect == null) {
            collect = new LabelCollect();
            collect.setLabelIds(labelId.toString())
                    .setCreateBy(current);
            super.save(collect);
        } else {
            appendLabelId(collect, labelId);
            super.update(collect);
        }

        return collect;
    }


    @Override
    public void cancel(Integer labelId) {
        String current = InfTraceContextHolder.get().getUserName();
        LabelCollect collect = labelCollectMapper.selectByUser(current);
        if (collect == null) {
            throw new ServiceException(BaseResponseCodeEnum.NEVEER_COLLECTED);
        }

        deleteLabelId(collect, labelId);
        super.update(collect);

    }

    @Override
    public LabelCollect get() {
        String current = InfTraceContextHolder.get().getUserName();
        LabelCollect labelCollect = labelCollectMapper.selectByUser(current);
        produceLabels(labelCollect);
        return labelCollect;
    }

    @Override
    public Optional<LabelCollect> getLabelCollect(String username) {
        Example example = new Example(LabelCollect.class);
        example.or()
                .andEqualTo("createBy", username);

        return labelCollectMapper.selectByExample(example).stream().findFirst();
    }

    private void produceLabels(LabelCollect labelCollect) {
        if (labelCollect == null) {
            return;
        }

        String labelIds = labelCollect.getLabelIds();
        if (StringUtils.isEmpty(labelIds)) {
            return;
        }

        String[] arr = labelIds.split(",");
//        List<String> list = Arrays.asList(arr);
//        List<Label> labels = list.stream().map(labelId -> {
//            Label label = labelService.getLabels(Integer.parseInt(labelId));
//            if (label == null) {
//                return label;
//            }
//            String createBy = label.getCreateBy();
//            String current = InfTraceContextHolder.get().getUserName();
//            if (current.equals(createBy)) {
//                label.setIsMy(true);
//            }
//            return label;
//        }).filter(label -> label != null).filter(label -> label.getDeleteStatus() != 1).collect(Collectors.toList());
        List<Integer> lists = Arrays.stream(arr).map(Integer::parseInt).collect(Collectors.toList());
        List<Label> labels = labelService.getLabels(lists)
                .stream().peek(label -> {
                    String createBy = label.getCreateBy();
                    String current = InfTraceContextHolder.get().getUserName();
                    if (current.equals(createBy)) {
                        label.setIsMy(true);
                    }
                }).filter(Objects::nonNull).filter(label -> label.getDeleteStatus() != 1).collect(Collectors.toList());

        labelCollect.setLabels(labels);
    }

    private void appendLabelId(LabelCollect collect, Integer labelId) {
        String labelIds = collect.getLabelIds();
        List<String> list = strConvertList(labelIds);
        boolean contains = list.contains(labelId.toString());
        if (contains) {
            throw new ServiceException(BaseResponseCodeEnum.LABEL_COLLECTED);
        }

        // 不包含
        list.add(labelId.toString());
        String newLabelIds = list.stream().collect(Collectors.joining(SymbolEnum.COMMA.getSymbol()));
        collect.setLabelIds(newLabelIds);
    }

    private void deleteLabelId(LabelCollect collect, Integer labelId) {
        String labelIds = collect.getLabelIds();
        List<String> list = strConvertList(labelIds);

        boolean contains = list.contains(labelId.toString());
        if (!contains) {
            throw new ServiceException(BaseResponseCodeEnum.LABEL_CAN_NOT_CANCEL_COLLECTED);
        }

        list.remove(labelId.toString());
        String newLabelIds = list.stream().collect(Collectors.joining(SymbolEnum.COMMA.getSymbol()));
        collect.setLabelIds(newLabelIds);
    }

    private List<String> strConvertList(String string) {
        String[] ids = string.split(",");
        List<String> list = new ArrayList<>(Arrays.asList(ids));
        list.remove("");
        return list;
    }
}
