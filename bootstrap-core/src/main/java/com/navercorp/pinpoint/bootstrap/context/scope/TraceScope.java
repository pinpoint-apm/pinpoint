package com.navercorp.pinpoint.bootstrap.context.scope;

import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;

/**
 * Created by Naver on 2015-11-30.
 */
public interface TraceScope {
    String getName();

    boolean tryEnter();
    boolean canLeave();
    void leave();

    boolean isActive();
}
