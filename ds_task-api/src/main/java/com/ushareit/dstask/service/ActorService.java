package com.ushareit.dstask.service;

import com.ushareit.dstask.bean.Actor;
import com.ushareit.dstask.bean.ActorShare;
import com.ushareit.dstask.common.vo.ActorUserGroupVo;
import com.ushareit.dstask.common.vo.UserGroupVo;

import java.util.List;
import java.util.Map;

/**
 * @author fengxiao
 * @date 2022/7/25
 */
public interface ActorService extends BaseService<Actor> {

    List<Actor> selectByActorDefinitionIds(List<Integer> ids, String region);

    boolean checkExist(Integer actorId, String name, String actorType);

    List<Actor> selectActorByDatabase(String database);

    void addActorShare(ActorShare actorShare);

    Map<Integer, Map<String,Boolean>> doAuthEdit(String id);

    void deleteActorShare(Integer id);

    List<ActorShare> listActorShare(Integer actorId);

    List<String> selectActorIdByShareId();

    /**
     * 列出数据源有哪些用户组
     * @param id
     * @return
     */
    List<UserGroupVo> listUserGroupPrivilege(Integer id);

    /**
     * 保存数据源权限
     * @param actorUserGroupVo
     */
    void saveActorUserGroupPrivileges(ActorUserGroupVo actorUserGroupVo);

    List<Actor> selectByConfigInfo(String where);
}
