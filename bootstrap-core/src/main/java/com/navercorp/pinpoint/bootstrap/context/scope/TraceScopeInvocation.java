package com.navercorp.pinpoint.bootstrap.context.scope;

import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;

/**
 * Created by Naver on 2015-11-30.
 */
public interface TraceScopeInvocation {
    String getName();

    boolean tryEnter(ExecutionPolicy policy);
    boolean canLeave(ExecutionPolicy policy);
    void leave(ExecutionPolicy policy);

    boolean isActive();
}
