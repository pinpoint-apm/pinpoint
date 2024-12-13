package com.navercorp.pinpoint.bootstrap.interceptor.registry;

import com.navercorp.pinpoint.bootstrap.interceptor.EmptyInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author emeroad
 */
public final class DefaultInterceptorRegistryAdaptor implements InterceptorRegistryAdaptor {

    private final static int DEFAULT_MAX = 8192;
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
        this.index = new WeakAtomicReferenceArray<>(maxRegistrySize, Interceptor.class);
    }

    @Override
    public int addInterceptor(Interceptor interceptor) {
        if (interceptor == null) {
            return -1;
        }

        final int newId = checkMaxSize(nextId());
        index.set(newId, interceptor);
        return newId;
    }

    @Override
    public int addInterceptor() {
        return checkMaxSize(nextId());
    }

    private int checkMaxSize(int id) {
        if (id >= registrySize) {
            throw new IndexOutOfBoundsException("Interceptor registry size exceeded. Check the \"profiler.interceptorregistry.size\" setting. size=" + index.length() + " id=" + id);
        }
        return id;
    }

    private int nextId() {
        return id.getAndIncrement();
    }

    public Interceptor getInterceptor(int key) {
        final Interceptor interceptor = this.index.get(key);
        if (interceptor == null) {
            return EmptyInterceptor.empty();
        } else {
            return interceptor;
        }
    }

    @Override
    public void clear() {
        if (this.index != null) {
            int length = index.length();
            for (int i = 0; i < length; i++) {
                Interceptor interceptor = index.get(0);
                interceptor = null;
            }
        }
    }

    @Override
    public boolean contains(int key) {
        return this.index.get(key) != null;
    }
}
