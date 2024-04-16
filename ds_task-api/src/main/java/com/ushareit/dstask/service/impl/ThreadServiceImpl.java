package com.ushareit.dstask.service.impl;

import com.ushareit.dstask.common.handle.AbstractCoundownRunnable;
import com.ushareit.dstask.service.ThreadService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Slf4j
public class ThreadServiceImpl implements ThreadService {


    private ExecutorService executorService=Executors.newFixedThreadPool(30);

    public void multi(List<? extends AbstractCoundownRunnable> runnables){
        if (CollectionUtils.isNotEmpty(runnables)){
            CountDownLatch countDownLatch=new CountDownLatch(runnables.size());
            for ( AbstractCoundownRunnable runnable:runnables){
                runnable.uCountDownLatch(countDownLatch);
            }
            runnables.forEach(abstractCoundownRunnable -> {
                executorService.execute(abstractCoundownRunnable);
            });
            try {
                countDownLatch.await();
                countDownLatch=null;
            } catch (InterruptedException e) {
                log.error("",e);
            }
        }

    }


}
