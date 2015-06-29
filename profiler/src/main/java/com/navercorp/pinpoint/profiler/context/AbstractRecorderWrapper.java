package com.navercorp.pinpoint.profiler.context;

import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.util.StringUtils;
import com.navercorp.pinpoint.common.trace.AnnotationKey;

public abstract class AbstractRecorderWrapper {

    protected final TraceContext traceContext;
    
    public AbstractRecorderWrapper(final TraceContext traceContext) {
        this.traceContext = traceContext;
    }
    
    public void recordException(Throwable th) {
        if (th == null) {
            return;
        }
        final String drop = StringUtils.drop(th.getMessage(), 256);
        // An exception that is an instance of a proxy class could make something wrong because the class name will vary.
        final int exceptionId = traceContext.cacheString(th.getClass().getName());
        setExceptionInfo(exceptionId, drop);
    }

    abstract void setExceptionInfo(int exceptionClassId, String exceptionMessage);
    
    public void recordApi(MethodDescriptor methodDescriptor) {
        if (methodDescriptor == null) {
            return;
        }
        if (methodDescriptor.getApiId() == 0) {
            recordAttribute(AnnotationKey.API, methodDescriptor.getFullName());
        } else {
            recordApiId(methodDescriptor.getApiId());
        }
    }
    
    public void recordApi(MethodDescriptor methodDescriptor, Object[] args) {
        recordApi(methodDescriptor);
        recordArgs(args);
    }

    public void recordApi(MethodDescriptor methodDescriptor, Object args, int index) {
        recordApi(methodDescriptor);
        recordSingleArg(args, index);
    }

    public void recordApi(MethodDescriptor methodDescriptor, Object[] args, int start, int end) {
        recordApi(methodDescriptor);
        recordArgs(args, start, end);
    }

    public void recordApiCachedString(MethodDescriptor methodDescriptor, String args, int index) {
        recordApi(methodDescriptor);
        recordSingleCachedString(args, index);
    }

    abstract void recordApiId(final int apiId);
    
    private void recordArgs(Object[] args, int start, int end) {
        if (args != null) {
            int max = Math.min(Math.min(args.length, AnnotationKey.MAX_ARGS_SIZE), end);
            for (int i = start; i < max; i++) {
                recordAttribute(AnnotationKey.getArgs(i), args[i]);
            }
            // TODO How to handle if args length is greater than MAX_ARGS_SIZE?
        }
    }

    private void recordSingleArg(Object args, int index) {
        if (args != null) {
            recordAttribute(AnnotationKey.getArgs(index), args);
        }
    }

    private void recordSingleCachedString(String args, int index) {
        if (args != null) {
            int cacheId = traceContext.cacheString(args);
            recordAttribute(AnnotationKey.getCachedArgs(index), cacheId);
        }
    }

    private void recordArgs(Object[] args) {
        if (args != null) {
            int max = Math.min(args.length, AnnotationKey.MAX_ARGS_SIZE);
            for (int i = 0; i < max; i++) {
                recordAttribute(AnnotationKey.getArgs(i), args[i]);
            }
         // TODO How to handle if args length is greater than MAX_ARGS_SIZE?                                                                  
        }
    }
    
    public void recordAttribute(AnnotationKey key, String value) {
        addAnnotation(new Annotation(key.getCode(), value));
    }

    public void recordAttribute(AnnotationKey key, int value) {
        addAnnotation(new Annotation(key.getCode(), value));
    }

    public void recordAttribute(AnnotationKey key, Object value) {
        addAnnotation(new Annotation(key.getCode(), value));
    }

    abstract void addAnnotation(Annotation annotation);
}
