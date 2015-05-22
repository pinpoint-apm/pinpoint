package com.navercorp.pinpoint.profiler.context.storage;

import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.bootstrap.context.TraceId;

public class StoragePool {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final StorageFactory storageFactory;
    private final ConcurrentHashMap<TraceId, Storage> pool = new ConcurrentHashMap<TraceId, Storage>();

    public StoragePool(final StorageFactory storageFactory) {
        this.storageFactory = storageFactory;
    }

    public Storage getStorage(final TraceId traceId) {
        Storage storage = pool.get(traceId);
        if (storage == null) {
            final Storage newStorage = storageFactory.createStorage();
            newStorage.setCloseHandler(new StorageCloseHandler() {
                @Override
                public void handle() {
                    if (traceId.getTraceCount() == 0) {
                        if(isDebug) {
                            logger.debug("Remove {}", traceId);
                        }
                        pool.remove(traceId);
                    }
                    if(isDebug) {
                        logger.debug("Close {}, pool={}", traceId, pool);
                    }
                }
            });
            storage = pool.putIfAbsent(traceId, newStorage);
            if (storage == null) {
                storage = newStorage;
            }
        }
        
        if(isDebug) {
            logger.debug("Get {}, pool={}", traceId, pool);
        }

        return storage;
    }
}