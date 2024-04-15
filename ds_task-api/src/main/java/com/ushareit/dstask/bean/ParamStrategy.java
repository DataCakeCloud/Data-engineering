package com.ushareit.dstask.bean;


import com.alibaba.fastjson.JSON;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author xuebotao
 * @date 2023-02-17
 */
@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ParamStrategy extends DeleteEntity {

    public String[] units;
    public String[] fixedValue;
    //是否忽略大小写
    public Boolean isIgnoreCase = true;
    public Range valueRange;

    @Data
    public class Range {
        public Integer min;
        public Integer max;

        public Range() {

        }
    }

}
