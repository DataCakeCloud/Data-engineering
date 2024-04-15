package com.ushareit.dstask.configuration;


import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.ushareit.dstask.condition.MybatisPlusCondition;
import com.ushareit.dstask.constant.CommonConstant;
import com.ushareit.dstask.constant.DsTaskConstant;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import com.ushareit.dstask.web.interceptor.DataCakeTableNameHandler;
import com.ushareit.dstask.web.interceptor.DatacakeDynamicTableNameInterceptor;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import javax.annotation.Resource;

@Configuration
public class MybatisPlusConfig {

    @Resource
    public DataCakeSourceConfig dataCakeSourceConfig;

    @Conditional(MybatisPlusCondition.class)
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 动态表名插件
        DatacakeDynamicTableNameInterceptor dynamicTableNameInnerInterceptor = new DatacakeDynamicTableNameInterceptor();
        dynamicTableNameInnerInterceptor.setTableNameHandler(new DataCakeTableNameHandler() {
            String[] ignoreDbShard = CommonConstant.IGNORE_DB_SHARD;
            @Override
            public String dynamicTableName(String database, String sql, String tableName) {
                //无租户信息
                if (StringUtils.isEmpty(InfTraceContextHolder.get().getTenantName()) || StringUtils.isEmpty(database)){
                    return tableName;
                }

                //超级管理员租户使用默认数据库
                if (dataCakeSourceConfig.getSuperTenant().equals(InfTraceContextHolder.get().getTenantName())){
                    return tableName;
                }

                //被过滤的表
                PathMatcher pathMatcherToUse = new AntPathMatcher();
                if (ArrayUtils.isNotEmpty(ignoreDbShard)) {
                    for (String pattern : ignoreDbShard) {
                        if (pathMatcherToUse.match(pattern, tableName)) {
                            return tableName;
                        }
                    }
                }
                return "`" + database + "_" + InfTraceContextHolder.get().getTenantName() + "`." + tableName;
            }
        });

        interceptor.addInnerInterceptor(dynamicTableNameInnerInterceptor);
        return interceptor;
    }
}