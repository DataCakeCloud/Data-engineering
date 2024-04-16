package com.ushareit.dstask.aspect;

import com.ushareit.dstask.annotation.DisLock;
import com.ushareit.dstask.constant.DsTaskConstant;
import com.ushareit.dstask.service.LockService;
import com.ushareit.dstask.trace.holder.InfTraceContextHolder;
import com.ushareit.dstask.web.utils.UuidUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Slf4j
@Aspect
@Component
@Order(Ordered.LOWEST_PRECEDENCE - 9000)
public class DisLockAspect {

    @Autowired
    private LockService lockService;

    @Pointcut("@annotation(com.ushareit.dstask.annotation.DisLock)")
    public void annotationPointcut() {
    }

    @Around(value = "annotationPointcut() && @annotation(disLock)")
    public Object doAround(ProceedingJoinPoint joinPoint, DisLock disLock) throws Throwable {
        // 此处进入到方法前  可以实现一些业务逻辑  在进入方法前进行加锁
        // put slf4j mdc
        String traceId = UuidUtil.getUuid32();
        MDC.put(DsTaskConstant.LOG_TRACE_ID, traceId);
        InfTraceContextHolder.get().setTraceId(traceId);
        InfTraceContextHolder.get().setStartTime(new Date());

        try {
            log.debug("try get lock , lock name is {}", disLock.key());
            if (StringUtils.isEmpty(disLock.key()) || !lockService.tryLock(disLock.key(), disLock.expiredSeconds())) {
                log.debug(" create lock failure key not could null  or {} lock already exist ", disLock.key());
                return null;
            }

            log.debug("success get lock , lock name is {}", disLock.key());
            return joinPoint.proceed();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }


    /**
     * 在切入点return内容之后切入内容（可以用来对处理返回值做一些加工处理）
     *
     * @param disLock
     */
    @AfterReturning("annotationPointcut() && @annotation(disLock)")
    public void doAfterReturning(DisLock disLock) {
        try {
            if (StringUtils.isEmpty(disLock.key())) {
                log.debug("lock key not could null!");
                return;
            }

            if (!disLock.isRelease()) {
                return;
            }

            lockService.unlock(disLock.key());
            log.debug("expire lock name is {}", disLock.key());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            Date startTime = InfTraceContextHolder.get().getStartTime();
            if (startTime != null) {
                log.debug("execute total cost {} seconds", TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()
                        - startTime.getTime()));
            }
            MDC.remove(DsTaskConstant.LOG_TRACE_ID);
            InfTraceContextHolder.remove();
        }
    }

}
