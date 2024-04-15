package com.ushareit.dstask;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author wuyan
 * @date 2019/7/22
 **/
@ServletComponentScan
//@MapperScan(basePackages = {"com.ushareit.dstask.mapper"})
@EnableCaching
//@EnableScheduling
@EnableTransactionManagement
@EnableAsync
@SpringBootApplication(scanBasePackages = {"com.ushareit.interceptor", "com.ushareit.dstask"})
public class DsTaskApplication {
    private static ConfigurableApplicationContext applicationContext = null;

    public static void main(String[] args) {
        applicationContext = SpringApplication.run(DsTaskApplication.class, args);
    }

    public static <T> T getBean(Class<T> c){
        return applicationContext.getBean(c);
    }
    public static ConfigurableApplicationContext getApplicationContext(){
        return applicationContext;
    }


}