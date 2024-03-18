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
package com.navercorp.pinpoint.profiler.context.exception.storage;

import com.navercorp.pinpoint.common.profiler.message.EnhancedDataSender;
import com.navercorp.pinpoint.io.ResponseMessage;
import com.navercorp.pinpoint.profiler.context.exception.model.ExceptionMetaDataFactory;
import com.navercorp.pinpoint.profiler.metadata.MetaDataType;

import java.util.Objects;

/**
 * @author intr3p1d
 */
public class ExceptionStorageFactory {

    private final EnhancedDataSender<MetaDataType, ResponseMessage> dataSender;
    private final int bufferSize;

    public ExceptionStorageFactory(EnhancedDataSender<MetaDataType, ResponseMessage> dataSender, int bufferSize) {
        this.dataSender = Objects.requireNonNull(dataSender, "dataSender");
        this.bufferSize = bufferSize;
    }

    public ExceptionStorage createStorage(ExceptionMetaDataFactory factory) {
        return new BufferedExceptionStorage(bufferSize, dataSender, factory);
    }
}
