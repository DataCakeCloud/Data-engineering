package com.ushareit.dstask.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.util.Date;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "short_url")
public class ShortUrlEntity extends BaseEntity{
    @Column(name = "real_url")
    private String realUrl;
    @Column(name = "url_id")
    private String urlId;//生成的短链字符串

    @Column(name = "create_time")
    private Date createTime;
}
