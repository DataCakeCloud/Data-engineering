package com.ushareit.dstask.service;

import com.ushareit.dstask.bean.JDBCLock;


public interface LockService extends BaseService<JDBCLock> {

    /**
     * 尝试获取锁
     *
     * @param tag            锁的键
     * @param expiredSeconds 锁的过期时间（单位：秒）
     * @return
     */
    boolean tryLock(String tag, Integer expiredSeconds);

    /**
     * 释放锁
     *
     * @param tag 锁的键
     */
    void unlock(String tag);


}
