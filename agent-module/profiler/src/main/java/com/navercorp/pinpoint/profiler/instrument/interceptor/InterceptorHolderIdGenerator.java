/*
 * Copyright 2025 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.navercorp.pinpoint.profiler.instrument.interceptor;

import java.util.concurrent.atomic.AtomicInteger;

public class InterceptorHolderIdGenerator {
    private final static int DEFAULT_MAX_ID_SIZE = 10000;
    private final static int DEFAULT_MAX_BOOTSTRAP_ID_SIZE = 100;

    private final int maxIdSize;
    private final int maxBootstrapIdSize;
    private final AtomicInteger idCounter;
    private final AtomicInteger bootstrapIdCounter;

    public InterceptorHolderIdGenerator(int maxIdSize, int maxBootstrapIdSize) {
        this.maxIdSize = maxIdSize > 0 ? maxIdSize : DEFAULT_MAX_ID_SIZE;
        this.maxBootstrapIdSize = maxBootstrapIdSize > 0 ? maxBootstrapIdSize : DEFAULT_MAX_BOOTSTRAP_ID_SIZE;
        this.idCounter = new AtomicInteger(maxBootstrapIdSize);
        this.bootstrapIdCounter = new AtomicInteger(0);
    }

    // application classloader interceptor id
    public int getId() {
        return checkMaxIdSize(idCounter.getAndIncrement());
    }

    int checkMaxIdSize(int id) {
        if (id >= maxIdSize) {
            throw new IndexOutOfBoundsException("interceptor registry size exceeded. check the \"profiler.interceptorregistry.size\" setting. size=" + maxIdSize + " id=" + id);
        }
        return id;
    }

    // bootstrap classloader interceptor id
    public int getBootstrapId() {
        return checkMaxBootstrapIdSize(bootstrapIdCounter.getAndIncrement());
    }

    int checkMaxBootstrapIdSize(int id) {
        if (id >= maxBootstrapIdSize) {
            throw new IndexOutOfBoundsException("interceptor registry bootstrap size exceeded. check the \"profiler.interceptorregistry.bootstrap.size\" setting. size=" + maxBootstrapIdSize + " id=" + id);
        }
        return id;
    }

    public boolean isBootstrapInterceptorHolder(int interceptorId) {
        return interceptorId >= 0 && interceptorId < maxBootstrapIdSize;
    }
}
