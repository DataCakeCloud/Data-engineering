package com.ushareit.dstask.common.handle;

import java.util.concurrent.CountDownLatch;

public abstract  class AbstractCoundownRunnable implements Runnable{
    public CountDownLatch countDownLatch;

    public abstract void uCountDownLatch(CountDownLatch countDownLatch);
}
