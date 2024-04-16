package com.ushareit.dstask.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;

/**
 * @author tianxu
 * @date 2023/4/18 18:01
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "AkSkRequest")
public class AkSkRequest {
    Integer type;
    String description;
}
