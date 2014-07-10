package com.nhn.pinpoint.profiler.interceptor;

import com.nhn.pinpoint.bootstrap.context.TraceContext;
import com.nhn.pinpoint.bootstrap.interceptor.StaticAroundInterceptor;
import com.nhn.pinpoint.bootstrap.interceptor.TraceContextSupport;
import com.nhn.pinpoint.profiler.util.DepthScope;
import com.nhn.pinpoint.profiler.util.Scope;

/**
 * @author emeroad
 */
public class ScopeDelegateStaticInterceptor implements StaticAroundInterceptor, TraceContextSupport {
    private final StaticAroundInterceptor delegate;
    private final Scope scope;


    public ScopeDelegateStaticInterceptor(StaticAroundInterceptor delegate, Scope scope) {
        if (delegate == null) {
            throw new NullPointerException("delegate must not be null");
        }
        if (scope == null) {
            throw new NullPointerException("scope must not be null");
        }
        this.delegate = delegate;
        this.scope = scope;
    }

    @Override
    public void before(Object target, String className, String methodName, String parameterDescription, Object[] args) {
        final int push = scope.push();
        if (push != DepthScope.ZERO) {
            return;
        }
        this.delegate.before(target, className, methodName, parameterDescription, args);
    }

    @Override
    public void after(Object target, String className, String methodName, String parameterDescription, Object[] args, Object result, Throwable throwable) {
        final int pop = scope.pop();
        if (pop != DepthScope.ZERO) {
            return;
        }
        this.delegate.after(target, className, methodName, parameterDescription, args, result, throwable);
    }


    @Override
    public void setTraceContext(TraceContext traceContext) {
        if (this.delegate instanceof TraceContextSupport) {
            ((TraceContextSupport) this.delegate).setTraceContext(traceContext);
        }
    }



}
