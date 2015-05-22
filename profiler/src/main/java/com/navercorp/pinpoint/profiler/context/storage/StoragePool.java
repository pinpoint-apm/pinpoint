/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.context.storage;

import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.bootstrap.context.TraceId;

/**
 * 
 * @author jaehong.kim
 *
 */
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