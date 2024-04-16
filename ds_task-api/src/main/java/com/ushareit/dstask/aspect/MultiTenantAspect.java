package com.ushareit.dstask.aspect;

import com.ushareit.dstask.annotation.MultiTenant;
import com.ushareit.dstask.bean.AccessTenant;
import com.ushareit.dstask.condition.MybatisPlusCondition;
import com.ushareit.dstask.constant.DsTaskConstant;
import com.ushareit.dstask.service.AccessTenantService;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import com.ushareit.dstask.web.utils.DataCakeConfigUtil;
import com.ushareit.dstask.web.utils.Mappers;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

import static com.ushareit.dstask.constant.CommonConstant.SHAREIT_TENANT_NAME;

/**
 * @author fengxiao
 * @date 2022/12/27
 */
@Slf4j
@Aspect
@Component
@Order(Ordered.LOWEST_PRECEDENCE - 1000)
@Conditional(MybatisPlusCondition.class)
public class MultiTenantAspect {

    @Autowired
    private AccessTenantService accessTenantService;

    @Pointcut("@annotation(com.ushareit.dstask.annotation.MultiTenant)")
    public void multi() {
    }

    @Around("multi() && @annotation(multiTenant)")
    public Object doAround(ProceedingJoinPoint joinPoint, MultiTenant multiTenant) throws Throwable {
        List<AccessTenant> accessTenantList = accessTenantService.getActiveList();
        log.debug("prepare to execute {}.{} for multi tenant {}", joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName(), Mappers.mapToList(accessTenantList, AccessTenant::getName));

        Object result = accessTenantList.stream().map(item -> {
            InfTraceContextHolder.get().setTenantName(item.getName());
            if (StringUtils.isNotEmpty(item.getName()) && !DataCakeConfigUtil.getDataCakeConfig().getDcRole() &&
                    (item.getName().equals(DataCakeConfigUtil.getDataCakeSourceConfig().getSuperTenant()) || item.getName().equals(SHAREIT_TENANT_NAME))) {
                InfTraceContextHolder.get().setIsPrivate(false);
            }
            MDC.put(DsTaskConstant.LOG_TENANT_NAME, item.getName());

            try {
                log.debug("start execute for tenant {}", item.getName());
                return joinPoint.proceed();
            } catch (Throwable e) {
                log.error(e.getMessage(), e);
                return null;
            } finally {
                log.debug("finish execute for tenant {}", item.getName());
                InfTraceContextHolder.get().setTenantName(null);
            }
        }).filter(Objects::nonNull).reduce((x, y) -> x).orElse(null);

        MDC.remove(DsTaskConstant.LOG_TENANT_NAME);
        return result;
    }

}
