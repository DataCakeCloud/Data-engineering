package com.ushareit.dstask.bean;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * author:xuebotao
 * data:2023-10-17
 */


@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClearStopParam {

    private String name;

    private String executionDate;

    private Boolean isCheckUpstream;

    private String status;

}
