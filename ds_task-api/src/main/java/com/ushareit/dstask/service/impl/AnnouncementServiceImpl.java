package com.ushareit.dstask.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageInfo;
import com.ushareit.dstask.bean.*;
import com.ushareit.dstask.constant.BaseResponseCodeEnum;
import com.ushareit.dstask.constant.DsTaskConstant;
import com.ushareit.dstask.exception.ServiceException;
import com.ushareit.dstask.mapper.AnnouncementMapper;
import com.ushareit.dstask.repositry.mapper.CrudMapper;
import com.ushareit.dstask.service.*;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import com.ushareit.dstask.web.factory.CloudFactory;
import com.ushareit.dstask.web.utils.DataCakeConfigUtil;
import com.ushareit.dstask.web.utils.DateUtil;
import com.ushareit.dstask.web.utils.cloud.CloudBaseClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/**
 * @author wuyan
 * @date 2022/4/12
 */
@Slf4j
@Service
public class AnnouncementServiceImpl extends AbstractBaseServiceImpl<Announcement> implements AnnouncementService {
    @Resource
    private AnnouncementMapper announcementMapper;

    @Resource
    private CloudFactory cloudFactory;

    @Override
    public CrudMapper<Announcement> getBaseMapper() {
        return announcementMapper;
    }

    @Override
    public Object save(Announcement announcement) {
        announcement
                .setOnline(0)
                .setDeleteStatus(DeleteEntity.NOT_DELETE)
                .setCreateBy(InfTraceContextHolder.get().getUserName())
                .setUpdateBy(InfTraceContextHolder.get().getUserName());
        super.save(announcement);
        return announcement;
    }

    @Override
    public void update(@RequestBody @Valid Announcement announcement) {
        Announcement fromDb = checkExist(announcement.getId());
        checkOnline(fromDb);
        announcement
                .setCreateBy(InfTraceContextHolder.get().getUserName())
                .setUpdateBy(InfTraceContextHolder.get().getUserName());
        super.update(announcement);
    }

    @Override
    public void delete(Object id) {
        Announcement fromDb = checkExist(id);
        fromDb.setDeleteStatus(DeleteEntity.DELETE).setUpdateTime(new Timestamp(System.currentTimeMillis())).setUpdateBy(InfTraceContextHolder.get().getUserName());
        super.update(fromDb);

    }

    @Override
    public Announcement getById(Object id) {
        return checkExist(id);
    }

    @Override
    public PageInfo<Announcement> listByPage(int pageNum, int pageSize, Map<String, String> paramMap) {
        Page<Announcement> page = announcementMapper.listByMap(paramMap);
        List<Announcement> list = page.getResult();
        list.forEach(announcement -> {
            announcement.setContent(null);
        });
        PageInfo<Announcement> pageInfo = getPageInfo(pageNum, pageSize, list);
        return pageInfo;
    }


    private Announcement checkExist(Object id) {
        Announcement announcement = super.getById(id);
        if (announcement == null || DeleteEntity.DELETE.equals(announcement.getDeleteStatus())) {
            throw new ServiceException(BaseResponseCodeEnum.DATA_NOT_FOUND, "公告不存在");
        }

        return announcement;
    }

    private void checkOnline(Announcement announcement) {
        if (announcement.getOnline() == 1) {
            // 上线 不可编辑
            throw new ServiceException(BaseResponseCodeEnum.ANNOUNCEMENT_IS_ONLINE);
        }
    }

    private void checkOffline(Announcement announcement) {
        if (announcement.getOnline() == 0) {
            // 上线 不可编辑
            throw new ServiceException(BaseResponseCodeEnum.ANNOUNCEMENT_IS_OFFLINE);
        }
    }

    @Override
    public void onlineAndOffline(Integer id, Integer online) {
        Announcement fromDb = checkExist(id);
        if (online == 0) {
            // 进行下线操作， 需要校验是否已经下线
            checkOffline(fromDb);
        } else {
            checkOnline(fromDb);
        }

        fromDb.setOnline(online)
                .setCreateBy(InfTraceContextHolder.get().getUserName())
                .setUpdateBy(InfTraceContextHolder.get().getUserName());
        super.update(fromDb);
    }

    @Override
    public List<Announcement> limit() {
        String current = DateUtil.last3Month(DateUtil.getNowDateStr());
        List<Announcement> announcements = announcementMapper.limit(current);
        announcements.forEach(announcement -> {
            announcement.setContent(null);
        });
        return announcements;
    }

    @Override
    public String uploadImage(MultipartFile image) {
        CloudResouce.DataResource defaultRegionConfig = DataCakeConfigUtil.getCloudResourcesService().getDefaultRegionConfig();
        CloudBaseClientUtil cloudClientUtil = cloudFactory.getCloudClientUtil(defaultRegionConfig.getRegionAlias());
        return cloudClientUtil.upload(image, "feedback", defaultRegionConfig.getRegionAlias());
    }
}
