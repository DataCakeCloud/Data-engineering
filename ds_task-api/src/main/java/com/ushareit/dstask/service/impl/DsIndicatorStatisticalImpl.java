package com.ushareit.dstask.service.impl;


import com.ushareit.dstask.annotation.DisLock;
import com.ushareit.dstask.annotation.MultiTenant;
import com.ushareit.dstask.bean.DsIndicatorStatistical;
import com.ushareit.dstask.mapper.DsIndicatorStatisticalMapper;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import com.ushareit.dstask.service.*;
import com.ushareit.dstask.web.utils.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;


import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;


/**
 * @author: xuebotao
 * @create: 2021-12-03
 */
@Slf4j
@Service
public class DsIndicatorStatisticalImpl extends AbstractBaseServiceImpl<DsIndicatorStatistical> implements DsIndicatorStatisticalService {

    @Resource
    private DsIndicatorStatisticalMapper dsIndicatorStatisticalMapper;

    @Override
    public CrudMapper<DsIndicatorStatistical> getBaseMapper() {
        return dsIndicatorStatisticalMapper;
    }

    @Autowired
    private ArtifactVersionService artifactVersionService;
    @Autowired
    private DsIndicatorStatisticalService dsIndicatorStatisticalService;

    @Autowired
    private TaskService taskService;
    @Autowired
    private OperateLogService operateLogService;

    @Override
    @Scheduled(initialDelay = 2000, fixedDelay = 600000)
    public void insertIndicators() {
//        dsIndicatorStatisticalService.insertIndicatorsInternal();
    }

    @MultiTenant
    @DisLock(key = "insertIndicators", expiredSeconds = 540, isRelease = false)
    public void insertIndicatorsInternal() {
        HashMap<String, Integer> taskIndicators = taskService.getTaskIndicators();
        HashMap<String, Integer> artifactIndicators = artifactVersionService.getArtifactIndicators();
        HashMap<String, Integer> resultMap = new HashMap<>();
        resultMap.putAll(taskIndicators);
        resultMap.putAll(artifactIndicators);
//        resultMap.putAll(operateLogService.getIndicators());
        //数据插入
        insertData(resultMap);
    }

    //数据插入
    public void insertData(HashMap<String, Integer> resultMap) {
        String nowDay = DateUtil.getNowDateStr();
        resultMap.forEach((key, value) -> {
            DsIndicatorStatistical findBean = getDataByDtAndName(nowDay, key);
            if (findBean == null) {
                super.save(assemblyBean(findBean, nowDay, key, value));
            } else {
                super.update(assemblyBean(findBean, nowDay, key, value));
            }
        });
    }


    @Override
    public DsIndicatorStatistical getDataByDtAndName(String dt, String name) {
        List<DsIndicatorStatistical> lists = dsIndicatorStatisticalMapper.getDataByDtAndName(dt, name);
        if (lists.isEmpty()) {
            return null;
        }
        return lists.stream().findFirst().orElse(null);
    }

    /**
     * 组装实体
     *
     * @return
     */
    public DsIndicatorStatistical assemblyBean(DsIndicatorStatistical dsIndicatorStatistical,
                                               String dt, String name, Integer value) {
        if (dsIndicatorStatistical == null) {
            dsIndicatorStatistical = new DsIndicatorStatistical();
        }
        dsIndicatorStatistical.setDt(dt);
        dsIndicatorStatistical.setIndicators(name);
        dsIndicatorStatistical.setValue(value);
        dsIndicatorStatistical.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        return dsIndicatorStatistical;
    }


}
