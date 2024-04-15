package com.ushareit.dstask.common.vo;

import com.ushareit.dstask.bean.Dictionary;
import lombok.Data;

/**
 * @author fengxiao
 * @date 2021/10/8
 */
@Data
public class DictionaryNameVO {

    private Integer id;
    private String name;

    public DictionaryNameVO(Dictionary dictionary) {
        this.id = dictionary.getId();
        this.name = dictionary.getChineseName();
    }

}
