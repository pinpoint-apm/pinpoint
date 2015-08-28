package com.navercorp.pinpoint.bootstrap.interceptor;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author emeroad
 */
public final class DefaultInterceptorRegistryAdaptor implements InterceptorRegistryAdaptor {
    private static final LoggingInterceptor LOGGING_INTERCEPTOR = new LoggingInterceptor("com.navercorp.pinpoint.profiler.interceptor.LOGGING_INTERCEPTOR");

    public static final InterceptorRegistry REGISTRY = new InterceptorRegistry();

    private final static int DEFAULT_MAX = 4096;
    private final int registrySize;

    private final AtomicInteger id = new AtomicInteger(0);

    private final WeakAtomicReferenceArray<Interceptor> index;

    public DefaultInterceptorRegistryAdaptor() {
        this(DEFAULT_MAX);
    }

    public DefaultInterceptorRegistryAdaptor(int maxRegistrySize) {
        if (maxRegistrySize < 0) {
            throw new IllegalArgumentException("negative maxRegistrySize:" + maxRegistrySize);
        }
        this.registrySize = maxRegistrySize;
        this.index = new WeakAtomicReferenceArray<Interceptor>(maxRegistrySize, Interceptor.class);
    }


    @Override
    public int addInterceptor(Interceptor interceptor) {
        if (interceptor == null) {
            return -1;
        }
        
        final int newId = nextId();
        if (newId >= registrySize) {
            throw new IndexOutOfBoundsException("size=" + index.length() + " id=" + id);
        }
        index.set(newId, interceptor);
        return newId;
    }

    private int nextId() {
        return id.getAndIncrement();
    }

    public Interceptor getInterceptor(int key) {
        Interceptor interceptor = this.index.get(key);
        return interceptor == null ? LOGGING_INTERCEPTOR : interceptor;
    }
}
