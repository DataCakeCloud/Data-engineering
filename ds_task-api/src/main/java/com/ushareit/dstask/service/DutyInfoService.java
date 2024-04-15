package com.ushareit.dstask.service;



import com.ushareit.dstask.bean.DutyInfo;


/**
 * @author xuebotao
 * @date 2022-11-25
 */
public interface DutyInfoService extends BaseService<DutyInfo> {


    /**
     * 获取当前值班人
     */
    String getDutyMan(String module);


}
