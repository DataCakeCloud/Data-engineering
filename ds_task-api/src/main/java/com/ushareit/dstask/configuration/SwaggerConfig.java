package com.ushareit.dstask.configuration;

import com.google.common.base.Predicates;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @program: hebe
 * @description:
 * @author: wuyan
 * @create: 2020-05-13 15:33
 **/
@EnableSwagger2
@Configuration
public class SwaggerConfig {
    @Bean
    public Docket createRestApi() {
        Docket docket = new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .pathMapping("/")
                .select() // 选择哪些路径和api会生成document
                .apis(RequestHandlerSelectors.basePackage("com.ushareit.dstask"))// 对所有api进行监控
                .paths(Predicates.not(PathSelectors.regex("/error.*")))//错误路径不监控
                .paths(PathSelectors.regex("/.*"))// 对根下所有路径进行监控
                .build();
        return docket;

    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                //标题
                .title("《DataCake操作平台接口文档》")
                //描述
                .description("description:接口文档")
                //作者信息
                .contact(new Contact("licg", "https://shimo.im/docs/pmkxQmWOO4COMKAN", "licg@ushareit.com"))
                //版本号
                .version("0.1.0")
                .build();
    }
}