package com.ushareit.dstask.service;


import com.ushareit.dstask.bean.SparkParamRestrict;

import java.util.List;


/**
 * @author xuebotao
 * @date 2023-02-17
 */
public interface SparkParamRestrictService extends BaseService<SparkParamRestrict> {


    List<SparkParamRestrict> getAllVauleCheck();

}
