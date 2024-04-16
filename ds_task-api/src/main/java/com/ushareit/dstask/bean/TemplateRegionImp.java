package com.ushareit.dstask.bean;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Data
@Entity
@Builder
@Table(name = "template_region_imp")
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@ApiModel("模版分region存放信息")
public class TemplateRegionImp extends DataEntity {
    @Column(name = "template_code")
    String templateCode;
    @Column(name = "region_code")
    String regionCode;
    @Column(name = "url")
    String url;
    @Column(name = "main_class")
    String mainClass;
    @Column(name = "image")
    String image;
}
