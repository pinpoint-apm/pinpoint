package com.nhn.pinpoint.profiler.modifier.db.interceptor;

import com.nhn.pinpoint.profiler.context.TraceContext;
import com.nhn.pinpoint.profiler.interceptor.*;
import com.nhn.pinpoint.profiler.interceptor.util.JDBCScope;
import com.nhn.pinpoint.profiler.logging.PLogger;
import com.nhn.pinpoint.profiler.logging.PLoggerFactory;
import com.nhn.pinpoint.profiler.util.DepthScope;

/**
 *
 */
public class JDBCScopeDelegateSimpleInterceptor implements SimpleAroundInterceptor, ByteCodeMethodDescriptorSupport, TraceContextSupport {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isTrace = logger.isTraceEnabled();
    private final SimpleAroundInterceptor delegate;


    public JDBCScopeDelegateSimpleInterceptor(SimpleAroundInterceptor delegate) {
        if (delegate == null) {
            throw new NullPointerException("delegate must not be null");
        }
        this.delegate = delegate;
    }

    @Override
    public void before(Object target, Object[] args) {
        final int push = JDBCScope.push();
        if (push != DepthScope.ZERO) {
            if (isTrace) {
                logger.trace("push bindValue scope. skip trace. level:{} {}", push, delegate.getClass());
            }
            return;
        }
        this.delegate.before(target, args);
    }

    @Override
    public void after(Object target, Object[] args, Object result) {
        final int pop = JDBCScope.pop();
        if (pop != DepthScope.ZERO) {
            if (isTrace) {
                logger.trace("pop bindValue scope. skip trace. level:{} {}", pop, delegate.getClass());
            }
            return;
        }
        this.delegate.after(target, args, result);
    }

    @Override
    public void setMethodDescriptor(MethodDescriptor descriptor) {
        if (this.delegate instanceof ByteCodeMethodDescriptorSupport) {
            ((ByteCodeMethodDescriptorSupport)this.delegate).setMethodDescriptor(descriptor);
        }
    }

    @Override
    public void setTraceContext(TraceContext traceContext) {
        if (this.delegate instanceof TraceContextSupport) {
            ((TraceContextSupport)this.delegate).setTraceContext(traceContext);
        }
    }
}
