/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.context.provider.metadata;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.bootstrap.config.TransportModule;
import com.navercorp.pinpoint.profiler.metadata.SimpleCache;

/**
 * @author Woonduk Kang(emeroad)
 */
public class SimpleCacheFactoryProvider implements Provider<SimpleCacheFactory> {

    private final SimpleCache.IdTransformer idTransformer;

    @Inject
    public SimpleCacheFactoryProvider(TransportModule transportModule) {
        if (TransportModule.THRIFT == transportModule) {
            this.idTransformer = new SimpleCache.ZigZagTransformer();
        } else if (TransportModule.GRPC == transportModule) {
            this.idTransformer = new SimpleCache.BypassTransformer();
        } else {
            throw new IllegalStateException("unsupported transportModule:" + transportModule);
        }
    }

    @Override
    public SimpleCacheFactory get() {
        return new SimpleCacheFactory(idTransformer);
    }
}
