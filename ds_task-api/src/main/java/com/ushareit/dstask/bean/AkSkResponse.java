package com.ushareit.dstask.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import java.sql.Timestamp;

/**
 * @author tianxu
 * @date 2023/4/18 18:01
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "AkSkResponse")
public class AkSkResponse {
    private String ak;
    private String sk;
    private String email;
    private String description;
    private String datacakeToken;
    private String deadline;
}
