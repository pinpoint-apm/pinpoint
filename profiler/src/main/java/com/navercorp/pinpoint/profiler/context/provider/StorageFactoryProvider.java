/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.provider;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import java.util.Objects;
import com.navercorp.pinpoint.profiler.context.module.SpanDataSender;
import com.navercorp.pinpoint.profiler.context.storage.BufferedStorageFactory;
import com.navercorp.pinpoint.profiler.context.storage.StorageFactory;
import com.navercorp.pinpoint.profiler.context.storage.TraceLogDelegateStorage;
import com.navercorp.pinpoint.profiler.context.storage.TraceLogDelegateStorageFactory;
import com.navercorp.pinpoint.profiler.sender.DataSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Woonduk Kang(emeroad)
 */
public class StorageFactoryProvider implements Provider<StorageFactory> {

    private final ProfilerConfig profilerConfig;
    private final DataSender spanDataSender;

    @Inject
    public StorageFactoryProvider(ProfilerConfig profilerConfig, @SpanDataSender DataSender spanDataSender) {
        this.profilerConfig = Objects.requireNonNull(profilerConfig, "profilerConfig");
        this.spanDataSender = Objects.requireNonNull(spanDataSender, "spanDataSender");
    }

    @Override
    public StorageFactory get() {
        StorageFactory storageFactory = newStorageFactory();
        if (isTraceLogEnabled()) {
            storageFactory = new TraceLogDelegateStorageFactory(storageFactory);
        }
        return storageFactory;
    }

    private StorageFactory newStorageFactory() {
        if (profilerConfig.isIoBufferingEnable()) {
            int ioBufferingBufferSize = this.profilerConfig.getIoBufferingBufferSize();
            return new BufferedStorageFactory(ioBufferingBufferSize, this.spanDataSender);
        } else {
            return new BufferedStorageFactory(Integer.MAX_VALUE, this.spanDataSender);
        }
    }

    @Override
    public String toString() {
        return "StorageFactoryProvider{" +
                "profilerConfig=" + profilerConfig +
                ", spanDataSender=" + spanDataSender +
                '}';
    }

    public boolean isTraceLogEnabled() {
        final Logger logger = LoggerFactory.getLogger(TraceLogDelegateStorage.class.getName());
        return logger.isTraceEnabled();
    }
}
