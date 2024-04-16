package com.ushareit.dstask.schedule;

import com.ushareit.dstask.annotation.DisLock;
import com.ushareit.dstask.annotation.MultiTenant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author fengxiao
 * @date 2022/12/27
 */
@Component
public class MultiTenantTask {

    @Autowired
    private MultiService multiService;

    @MultiTenant
    //@Scheduled(fixedDelay = 5000)
    @DisLock(key = "multiTest", expiredSeconds = 10 * 60, isRelease = false)
    public void execute() {
        multiService.monitor();
    }

}
