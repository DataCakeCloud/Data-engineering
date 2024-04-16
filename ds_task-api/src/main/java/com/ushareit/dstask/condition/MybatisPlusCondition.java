package com.ushareit.dstask.condition;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * @author lcg
 */
public class MybatisPlusCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return Boolean.parseBoolean(context.getEnvironment().getProperty("enable.dbshard"));
    }
}
