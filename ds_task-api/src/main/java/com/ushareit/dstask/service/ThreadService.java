package com.ushareit.dstask.service;


import com.ushareit.dstask.common.handle.AbstractCoundownRunnable;

import java.util.List;

public interface ThreadService {
    void multi(List<? extends AbstractCoundownRunnable> runnables);
}
