package com.ushareit.dstask.web.factory;

/**
 * @author: licg
 * @create: 2021-05-12 15:24
 */
public interface Job {
    void beforeExec() throws Exception;

    void beforeCheck() throws Exception;

    void afterCheck() throws Exception;

    void afterExec();
}