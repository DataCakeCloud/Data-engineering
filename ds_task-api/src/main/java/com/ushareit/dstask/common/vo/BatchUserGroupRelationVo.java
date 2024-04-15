package com.ushareit.dstask.common.vo;

import com.google.common.collect.Lists;
import com.ushareit.dstask.bean.UserGroupRelation;
import com.ushareit.dstask.utils.GsonUtil;
import lombok.Data;

import java.util.List;

@Data
public class BatchUserGroupRelationVo {
    private List<UserGroupRelation> userGroupRelationList;

    public static void main(String[] args) {
        UserGroupRelation userGroupRelation=new UserGroupRelation();
        BatchUserGroupRelationVo batchUserGroupRelationVo=new BatchUserGroupRelationVo();
        batchUserGroupRelationVo.setUserGroupRelationList(Lists.newArrayList(userGroupRelation));
        System.out.println(GsonUtil.toJson(batchUserGroupRelationVo,false));
    }
}
