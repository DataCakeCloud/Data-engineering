package com.ushareit.dstask.configuration;

import com.ushareit.dstask.condition.SchedulerCondition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.TaskManagementConfigUtils;

/**
 * @author fengxiao
 * @date 2022/11/8
 */
@Configuration
public class ScheduleConfiguration {

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduling = new ThreadPoolTaskScheduler();
        scheduling.setPoolSize(10);
        scheduling.initialize();
        return scheduling;
    }

    @Conditional(SchedulerCondition.class)
    @Bean(name = TaskManagementConfigUtils.SCHEDULED_ANNOTATION_PROCESSOR_BEAN_NAME)
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public ScheduledAnnotationBeanPostProcessor scheduledAnnotationProcessor() {
        return new ScheduledAnnotationBeanPostProcessor();
    }

}
