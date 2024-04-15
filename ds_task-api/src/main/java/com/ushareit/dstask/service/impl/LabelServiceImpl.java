package com.ushareit.dstask.service.impl;

import com.ushareit.dstask.bean.*;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.constant.DsTaskConstant;
import com.ushareit.dstask.constant.SymbolEnum;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.mapper.LabelMapper;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import com.ushareit.dstask.service.LabelCollectService;
import com.ushareit.dstask.service.LabelService;
import com.ushareit.dstask.service.TaskService;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author wuyan
 * @date 2021/9/22
 */
@Slf4j
@Service
public class LabelServiceImpl extends AbstractBaseServiceImpl<Label> implements LabelService {

    @Resource
    public TaskService taskService;
    @Resource
    private LabelMapper labelMapper;
    @Resource
    private LabelCollectService labelCollectService;

    @Override
    public CrudMapper<Label> getBaseMapper() {
        return labelMapper;
    }

    @Override
    public Object save(Label label) {
        preCheckCommon(label);
        super.save(label);
        return label;
    }


    @Override
    public void update(Label label) {
        //1.ID不为空校验
        if (label == null || label.getId() == null) {
            throw new ServiceException(BaseResponseCodeEnum.CLI_PARAM_REQUIRED);
        }

        preCheckCommon(label);
        Label labelFromDb = checkExist(label.getId());
        label.setCreateBy(labelFromDb.getCreateBy());
        label.setUpdateBy(InfTraceContextHolder.get().getUserName());
        super.update(label);
    }

    @Override
    public Label getById(Object id) {
        Label label = checkExist(Integer.parseInt(id.toString()));
        label = StringUtils.isEmpty(label.getTasks()) ? label : produceTaskListOfLabel(label);
        return label;
    }

    @Override
    public Label getLabel(Integer id) {
        return labelMapper.getById(id);
    }


    @Override
    public List<Label> getLabels(List<Integer> ids) {
        if (ids.isEmpty()) {
            return new ArrayList<>();
        }
        Example example = new Example(Label.class);
        example.or().andIn("id", ids).andEqualTo("deleteStatus", DeleteEntity.NOT_DELETE);
        return labelMapper.selectByExample(example);
    }

    private Label produceTaskListOfLabel(Label label) {
        String tasks = label.getTasks();
        String[] arr = tasks.split(",");
        List<Integer> ids = Arrays.asList(arr).stream().map(taskId -> {
            int i = Integer.parseInt(taskId);
            return i;
        }).collect(Collectors.toList());
        List<Task> list = taskService.listByIds(ids).stream().filter(task -> task.getDeleteStatus() == 0).collect(Collectors.toList());
        label.setList(list);
        return label;
    }

    @Override
    public Map<String, List<Label>> list(Label label) {

//        List<Label> current = labelMapper.select(label);
//        List<Label> others = labelMapper.selectOthers(label);

        //查出全部
        Example example = new Example(Label.class);
        example.or().andEqualTo("deleteStatus", DeleteEntity.NOT_DELETE);
        List<Label> allLabel = labelMapper.selectByExample(example);

        List<Label> current = new ArrayList<>();
        List<Label> others = new ArrayList<>();
        List<Label> collect = allLabel.stream().map(data -> {
            if (data.getCreateBy().equals(label.getCreateBy())) {
                current.add(data);
            } else {
                others.add(data);
            }
            return data;
        }).collect(Collectors.toList());

        Map<String, List<Label>> result = new HashMap<>(2);
        current.stream().forEach(la -> la.setIsMy(true));
        result.put("current", current);
        result.put("others", others);
        return result;
    }

    @Override
    public void delete(Object id) {
        Label label = checkExist(Integer.parseInt(id.toString()));
        label.setDeleteStatus(1);
        label.setUpdateBy(InfTraceContextHolder.get().getUserName());
        super.update(label);
    }

    @Override
    public Label checkExist(Integer id) {
        Label label = labelMapper.getById(id);
        if (label == null) {
            throw new ServiceException(BaseResponseCodeEnum.DATA_NOT_FOUND, "标签id:" + id + "不存在");
        }
        return label;
    }

    @Override
    public Integer count(Timestamp time) {
        List<Map<String, Integer>> list = labelMapper.count(time);
        int num = list.stream().mapToInt(map -> {
            Object tmp = map.get("num");
            return Integer.parseInt(tmp.toString());
        }).reduce((x, y) -> x + y).getAsInt();

        return num;
    }

    private void preCheckCommon(Label label) {
        //1.校验 名称
        if (!match(label.getName(), DsTaskConstant.LABEL_NAME_PATTERN)) {
            throw new ServiceException(BaseResponseCodeEnum.LABEL_NAME_NOT_MATCH);
        }

        //2.Name不重复校验
        super.checkOnUpdate(super.getByName(label.getName()), label);
    }

    @Override
    public List<Label> getList(String username) {
        Example example = new Example(Label.class);
        example.or()
                .andEqualTo("createBy", username)
                .andEqualTo("deleteStatus", DeleteEntity.NOT_DELETE);

        Optional<LabelCollect> labelCollect = labelCollectService.getLabelCollect(username);
        if (labelCollect.isPresent() && StringUtils.isNotBlank(labelCollect.get().getLabelIds())) {
            List<Integer> labelIds = Arrays.stream(labelCollect.get().getLabelIds().split(SymbolEnum.COMMA.getSymbol()))
                    .map(Integer::parseInt).collect(Collectors.toList());

            if (CollectionUtils.isNotEmpty(labelIds)) {
                example.or()
                        .andIn("id", labelIds)
                        .andEqualTo("deleteStatus", DeleteEntity.NOT_DELETE);
            }
        }
        return labelMapper.selectByExample(example);
    }
}
