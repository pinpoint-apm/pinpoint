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
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.io.ResponseMessage;
import com.navercorp.pinpoint.profiler.context.exception.model.ExceptionMetaData;
import com.navercorp.pinpoint.profiler.context.exception.model.ExceptionMetaDataFactory;
import com.navercorp.pinpoint.profiler.context.exception.model.ExceptionWrapper;
import com.navercorp.pinpoint.profiler.metadata.MetaDataType;
import com.navercorp.pinpoint.profiler.util.queue.ArrayBuffer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Objects;

/**
 * @author intr3p1d
 */
public class BufferedExceptionStorage implements ExceptionStorage {

    private static final Logger logger = LogManager.getLogger(BufferedExceptionStorage.class);
    private static final boolean isDebug = logger.isDebugEnabled();

    private final ArrayBuffer<ExceptionWrapper> buffer;
    private final EnhancedDataSender<MetaDataType, ResponseMessage> dataSender;
    private final ExceptionMetaDataFactory factory;

    public BufferedExceptionStorage(
            int bufferSize,
            EnhancedDataSender<MetaDataType, ResponseMessage> dataSender,
            ExceptionMetaDataFactory exceptionMetaDataFactory
    ) {
        this.dataSender = Objects.requireNonNull(dataSender, "dataSender");
        this.buffer = new ArrayBuffer<>(bufferSize);
        this.factory = Objects.requireNonNull(exceptionMetaDataFactory, "exceptionMetaDataFactory");
    }

    @Override
    public void store(List<ExceptionWrapper> wrappers) {
        this.buffer.put(wrappers);
        if (buffer.isOverflow()) {
            final List<ExceptionWrapper> flushData = buffer.drain();
            sendExceptionMetaData(flushData);
        }
    }

    @Override
    public void flush() {
        final List<ExceptionWrapper> copy = buffer.drain();
        if (CollectionUtils.hasLength(copy)) {
            sendExceptionMetaData(copy);
        }
    }

    @Override
    public void close() {
    }


    private void sendExceptionMetaData(List<ExceptionWrapper> exceptionWrappers) {
        final ExceptionMetaData exceptionMetaData = this.factory.newExceptionMetaData(exceptionWrappers);

        if (isDebug) {
            logger.debug("Flush {}", exceptionMetaData);
        }
        final boolean success = this.dataSender.request(exceptionMetaData);
        if (!success) {
            // Do not call exceptionMetaData.toString()
            logger.debug("send fail");
        }
    }

    @Override
    public String toString() {
        return "ExceptionTraceStorage{" +
                "buffer=" + buffer +
                ", dataSender=" + dataSender +
                '}';
    }
}
