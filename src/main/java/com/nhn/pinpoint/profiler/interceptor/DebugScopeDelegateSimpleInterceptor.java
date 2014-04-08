package com.nhn.pinpoint.profiler.interceptor;

import com.nhn.pinpoint.bootstrap.context.TraceContext;
import com.nhn.pinpoint.bootstrap.interceptor.ByteCodeMethodDescriptorSupport;
import com.nhn.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.nhn.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.nhn.pinpoint.bootstrap.interceptor.TraceContextSupport;
import com.nhn.pinpoint.bootstrap.logging.PLogger;
import com.nhn.pinpoint.bootstrap.logging.PLoggerFactory;
import com.nhn.pinpoint.profiler.util.DepthScope;

/**
 * @author emeroad
 */
public class DebugScopeDelegateSimpleInterceptor implements SimpleAroundInterceptor, ByteCodeMethodDescriptorSupport, TraceContextSupport {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();
    private final SimpleAroundInterceptor delegate;
    private final DepthScope scope;


    public DebugScopeDelegateSimpleInterceptor(SimpleAroundInterceptor delegate, DepthScope scope) {
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
    public void before(Object target, Object[] args) {
        final int push = scope.push();
        if (push != DepthScope.ZERO) {
            if (isDebug) {
                logger.debug("push {}. skip trace. level:{} {}", new Object[]{scope.getName(), push, delegate.getClass()});
            }
            return;
        }
        this.delegate.before(target, args);
    }

    @Override
    public void after(Object target, Object[] args, Object result) {
        final int pop = scope.pop();
        if (pop != DepthScope.ZERO) {
            if (isDebug) {
                logger.debug("pop {}. skip trace. level:{} {}", new Object[]{scope.getName(), pop, delegate.getClass()});
            }
            return;
        }
        this.delegate.after(target, args, result);
    }

    @Override
    public void setMethodDescriptor(MethodDescriptor descriptor) {
        if (this.delegate instanceof ByteCodeMethodDescriptorSupport) {
            ((ByteCodeMethodDescriptorSupport) this.delegate).setMethodDescriptor(descriptor);
        }
    }

    @Override
    public void setTraceContext(TraceContext traceContext) {
        if (this.delegate instanceof TraceContextSupport) {
            ((TraceContextSupport) this.delegate).setTraceContext(traceContext);
        }
    }
}
