package com.ushareit.dstask.service;

import com.ushareit.dstask.bean.Announcement;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author wuyan
 * @date 2022/4/12
 */
public interface AnnouncementService extends BaseService<Announcement>{
    void onlineAndOffline(Integer id, Integer online);

    List<Announcement> limit();

    String uploadImage(MultipartFile image);
}
