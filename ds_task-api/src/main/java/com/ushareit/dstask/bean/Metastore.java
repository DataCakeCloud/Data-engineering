package com.ushareit.dstask.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;

/**
 * @author wuyan
 * @date 2022/3/22
 */
@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Metastore {
    private String uri;

    private String tableProp;
}
