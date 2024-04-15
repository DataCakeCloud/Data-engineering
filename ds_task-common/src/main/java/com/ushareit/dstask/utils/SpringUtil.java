package com.ushareit.dstask.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author fengxiao
 * @date 2023/2/9
 */
@Lazy(false)
@Component
public class SpringUtil implements ApplicationContextAware {

    private static ApplicationContext APPLICATION_CONTEXT;

    public static <BEAN> BEAN getBean(Class<BEAN> beanClass) {
        if (APPLICATION_CONTEXT == null) {
            throw new NullPointerException("需要待服务启动后才可调用");
        }

        return APPLICATION_CONTEXT.getBean(beanClass);
    }

    public static <BEAN> Map<String, BEAN> getBeansMap(Class<BEAN> beanClass) {
        if (APPLICATION_CONTEXT == null) {
            throw new NullPointerException("需要待服务启动后才可调用");
        }

        return APPLICATION_CONTEXT.getBeansOfType(beanClass);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        APPLICATION_CONTEXT = applicationContext;
    }

}
