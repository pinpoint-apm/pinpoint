/*
 * Copyright 2023 NAVER Corp.
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
package com.navercorp.pinpoint.profiler.context.provider.exception;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.common.profiler.message.EnhancedDataSender;
import com.navercorp.pinpoint.io.ResponseMessage;
import com.navercorp.pinpoint.profiler.context.exception.storage.ExceptionStorageFactory;
import com.navercorp.pinpoint.profiler.context.module.MetadataDataSender;
import com.navercorp.pinpoint.profiler.context.monitor.config.ExceptionTraceConfig;
import com.navercorp.pinpoint.profiler.metadata.MetaDataType;

import java.util.Objects;

/**
 * @author intr3p1d
 */
public class ExceptionStorageFactoryProvider implements Provider<ExceptionStorageFactory> {

    private final ExceptionTraceConfig exceptionTraceConfig;
    private final EnhancedDataSender<MetaDataType, ResponseMessage> spanTypeDataSender;

    @Inject
    public ExceptionStorageFactoryProvider(
            ExceptionTraceConfig exceptionTraceConfig,
            @MetadataDataSender EnhancedDataSender<MetaDataType, ResponseMessage> metadataDataSender
    ) {
        this.exceptionTraceConfig = Objects.requireNonNull(exceptionTraceConfig, "exceptionTraceConfig");
        this.spanTypeDataSender = metadataDataSender;
    }

    @Override
    public ExceptionStorageFactory get() {
        return newStorageFactory();
    }

    private ExceptionStorageFactory newStorageFactory() {
        return new ExceptionStorageFactory(spanTypeDataSender, exceptionTraceConfig.getIoBufferingBufferSize());
    }
}
