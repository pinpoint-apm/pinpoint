package com.navercorp.pinpoint.bootstrap.interceptor;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author emeroad
 */
public final class DefaultInterceptorRegistryAdaptor implements InterceptorRegistryAdaptor {
    private static final LoggingInterceptor LOGGING_INTERCEPTOR = new LoggingInterceptor("com.navercorp.pinpoint.profiler.interceptor.LOGGING_INTERCEPTOR");

    public static final InterceptorRegistry REGISTRY = new InterceptorRegistry();

    private final static int DEFAULT_MAX = 4096;
    private final int registrySize;

    private final AtomicInteger id = new AtomicInteger(0);

    private final WeakAtomicReferenceArray<Interceptor> simpleIndex;

//    private final ConcurrentMap<String, Integer> nameIndex = new ConcurrentHashMap<String, Integer>();

    public DefaultInterceptorRegistryAdaptor() {
        this(DEFAULT_MAX);
    }

    public DefaultInterceptorRegistryAdaptor(int maxRegistrySize) {
        if (maxRegistrySize < 0) {
            throw new IllegalArgumentException("negative maxRegistrySize:" + maxRegistrySize);
        }
        this.registrySize = maxRegistrySize;
        this.simpleIndex = new WeakAtomicReferenceArray<Interceptor>(maxRegistrySize, Interceptor.class);
    }


    public int addStaticInterceptor(StaticAroundInterceptor interceptor) {
        if (interceptor == null) {
            return -1;
        }
        return addInterceptor(interceptor, simpleIndex);
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
            throw new IndexOutOfBoundsException("size=" + simpleIndex.length() + " id=" + id);
        }

        this.simpleIndex.set(newId, interceptor);
        return newId;
    }

    public StaticAroundInterceptor getStaticInterceptor(int key) {
        final StaticAroundInterceptor interceptor = (StaticAroundInterceptor)simpleIndex.get(key);
        if (interceptor == null) {
            // return LOGGING_INTERCEPTOR upon wrong logic
            return LOGGING_INTERCEPTOR;
        }
        return interceptor;
    }

    public Interceptor findInterceptor(int key) {
        final Interceptor simpleInterceptor = this.simpleIndex.get(key);
        if (simpleInterceptor != null) {
            return simpleInterceptor;
        }
        Logger logger = Logger.getLogger(InterceptorRegistry.class.getName());
        if (logger.isLoggable(Level.WARNING)) {
            logger.warning("interceptor not found. id:" + key);
        }
        return LOGGING_INTERCEPTOR;
    }

    public SimpleAroundInterceptor getSimpleInterceptor(int key) {
        final SimpleAroundInterceptor interceptor = (SimpleAroundInterceptor)simpleIndex.get(key);
        if (interceptor == null) {
            // return LOGGING_INTERCEPTOR upon wrong logic
            return LOGGING_INTERCEPTOR;
        }
        return interceptor;
    }

    @Override
    public int addInterceptor(Interceptor interceptor) {
        if (interceptor == null) {
            return -1;
        }
        final int newId = nextId();
        if (newId >= registrySize) {
            throw new IndexOutOfBoundsException("size=" + simpleIndex.length() + " id=" + id);
        }

        this.simpleIndex.set(newId, interceptor);
        return newId;
    }
}
