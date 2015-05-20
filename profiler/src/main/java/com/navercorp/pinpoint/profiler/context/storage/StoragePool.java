package com.navercorp.pinpoint.profiler.context.storage;

import java.util.concurrent.ConcurrentHashMap;

import com.navercorp.pinpoint.bootstrap.context.TraceId;

public class StoragePool {
    private final StorageFactory storageFactory;
    private final ConcurrentHashMap<TraceId, Storage> pool = new ConcurrentHashMap<TraceId, Storage>();

    public StoragePool(final StorageFactory storageFactory) {
        this.storageFactory = storageFactory;
    }

    public Storage getStorage(final TraceId traceId) {
        Storage storage = pool.get(traceId);
        if (storage == null) {
            final Storage newStorage = storageFactory.createStorage();
            storage = pool.putIfAbsent(traceId, newStorage);
            if (storage == null) {
                storage = newStorage;
            }
        }

        return storage;
    }

    public void returnStorage(final TraceId traceId) {
        if (traceId.getTraceCount() == 0) {
            pool.remove(traceId);
        }
    }
}