/*
 * Copyright 2017 NAVER Corp.
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
import com.navercorp.pinpoint.profiler.context.SpanChunkFactory;
import com.navercorp.pinpoint.profiler.context.module.SpanDataSender;
import com.navercorp.pinpoint.profiler.context.storage.BufferedStorageFactory;
import com.navercorp.pinpoint.profiler.context.storage.SpanStorageFactory;
import com.navercorp.pinpoint.profiler.context.storage.StorageFactory;
import com.navercorp.pinpoint.profiler.sender.DataSender;

/**
 * @author Woonduk Kang(emeroad)
 */
public class StorageFactoryProvider implements Provider<StorageFactory> {

    private final ProfilerConfig profilerConfig;
    private final DataSender spanDataSender;
    private final SpanChunkFactory spanChunkFactory;

    @Inject
    public StorageFactoryProvider(ProfilerConfig profilerConfig, @SpanDataSender DataSender spanDataSender, SpanChunkFactory spanChunkFactory) {
        if (profilerConfig == null) {
            throw new NullPointerException("profilerConfig must not be null");
        }
        if (spanDataSender == null) {
            throw new NullPointerException("spanDataSender must not be null");
        }
        if (spanChunkFactory == null) {
            throw new NullPointerException("spanChunkFactory must not be null");
        }

        this.profilerConfig = profilerConfig;
        this.spanDataSender = spanDataSender;
        this.spanChunkFactory = spanChunkFactory;
    }

    @Override
    public StorageFactory get() {
        if (profilerConfig.isIoBufferingEnable()) {
            int ioBufferingBufferSize = this.profilerConfig.getIoBufferingBufferSize();
            return new BufferedStorageFactory(ioBufferingBufferSize, this.spanDataSender, this.spanChunkFactory);
        } else {
            return new SpanStorageFactory(spanDataSender);
        }
    }

    @Override
    public String toString() {
        return "StorageFactoryProvider{" +
                "profilerConfig=" + profilerConfig +
                ", spanDataSender=" + spanDataSender +
                ", spanChunkFactory=" + spanChunkFactory +
                '}';
    }
}
