package com.ushareit.dstask.service.impl;

import com.github.pagehelper.PageInfo;
import com.ushareit.dstask.bean.Auditlog;
import com.ushareit.dstask.bean.Task;
import com.ushareit.dstask.constant.DsTaskConstant;
import com.ushareit.dstask.mapper.AuditlogMapper;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import com.ushareit.dstask.service.AuditlogService;
import com.ushareit.dstask.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Service
public class AuditlogServiceImpl extends AbstractBaseServiceImpl<Auditlog> implements AuditlogService {

    @Resource
    private AuditlogMapper auditlogMapper;

    @Autowired
    public TaskService taskService;

    @Override
    public CrudMapper<Auditlog> getBaseMapper() {
        return auditlogMapper;
    }

    @Override
    public PageInfo<Auditlog> pages(Integer pageNum, Integer pageSize, Auditlog auditlog) {
        Example example = new Example(Auditlog.class);
        Example.Criteria criteria = example.or();
        if (auditlog.getEventId() != null && auditlog.getModule() != null) {
            criteria.andEqualTo("eventId", auditlog.getEventId());
            criteria.andEqualTo("module", auditlog.getModule());
        }
        if (StringUtils.isNotEmpty(auditlog.getEventCode())) {
            criteria.andEqualTo("eventCode", auditlog.getEventCode());
        }
        example.setOrderByClause(" create_time DESC ");
        PageInfo<Auditlog> auditlogPageInfo = listByPage(pageNum, pageSize, example);

        if (StringUtils.isNotEmpty(auditlog.getModule()) && auditlog.getModule().equalsIgnoreCase(DsTaskConstant.WORKFLOW)) {
            return auditlogPageInfo;
        }

        Task byId = taskService.getById(auditlog.getEventId());
        List<Auditlog> auditlogslist = auditlogPageInfo.getList();
        for (Auditlog au : auditlogslist) {
            if (au.getEventVersion() != null && au.getEventVersion().equals(byId.getCurrentVersion())) {
                au.setCurrentVersion(true);
            }
        }
        auditlogPageInfo.setList(auditlogslist);
        return auditlogPageInfo;
    }

    @Override
    public List<String> type(String module) {
        return auditlogMapper.selectCode(module);
    }



}