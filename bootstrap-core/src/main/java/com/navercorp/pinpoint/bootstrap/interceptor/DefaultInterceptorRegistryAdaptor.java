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

    private final WeakAtomicReferenceArray<InterceptorInstance> index;
    private final WeakAtomicReferenceArray<StaticAroundInterceptor> staticIndex;
    private final WeakAtomicReferenceArray<SimpleAroundInterceptor> simpleIndex;

//    private final ConcurrentMap<String, Integer> nameIndex = new ConcurrentHashMap<String, Integer>();

    public DefaultInterceptorRegistryAdaptor() {
        this(DEFAULT_MAX);
    }

    public DefaultInterceptorRegistryAdaptor(int maxRegistrySize) {
        if (maxRegistrySize < 0) {
            throw new IllegalArgumentException("negative maxRegistrySize:" + maxRegistrySize);
        }
        this.registrySize = maxRegistrySize;
        this.index = new WeakAtomicReferenceArray<InterceptorInstance>(maxRegistrySize, InterceptorInstance.class);
        this.staticIndex = new WeakAtomicReferenceArray<StaticAroundInterceptor>(maxRegistrySize, StaticAroundInterceptor.class);
        this.simpleIndex = new WeakAtomicReferenceArray<SimpleAroundInterceptor>(maxRegistrySize, SimpleAroundInterceptor.class);
    }


    public int addStaticInterceptor(StaticAroundInterceptor interceptor) {
        if (interceptor == null) {
            return -1;
        }
        return addInterceptor(interceptor, staticIndex);
    }
    
    @Override
    public int addInterceptor(InterceptorInstance interceptor) {
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

    private <T extends Interceptor> int addInterceptor(T interceptor, WeakAtomicReferenceArray<T> index) {
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

    public int addSimpleInterceptor(SimpleAroundInterceptor interceptor) {
        if (interceptor == null) {
            return -1;
        }
        final int newId = nextId();
        if (newId >= registrySize) {
            throw new IndexOutOfBoundsException("size=" + staticIndex.length() + " id=" + id);
        }

        this.simpleIndex.set(newId, interceptor);
        return newId;
    }

    public StaticAroundInterceptor getStaticInterceptor(int key) {
        final StaticAroundInterceptor interceptor = staticIndex.get(key);
        if (interceptor == null) {
            // return LOGGING_INTERCEPTOR upon wrong logic
            return LOGGING_INTERCEPTOR;
        }
        return interceptor;
    }

    public InterceptorInstance findInterceptor(int key) {
        return this.index.get(key);
    }

    public SimpleAroundInterceptor getSimpleInterceptor(int key) {
        final SimpleAroundInterceptor interceptor = simpleIndex.get(key);
        if (interceptor == null) {
            // return LOGGING_INTERCEPTOR upon wrong logic
            return LOGGING_INTERCEPTOR;
        }
        return interceptor;
    }
}
