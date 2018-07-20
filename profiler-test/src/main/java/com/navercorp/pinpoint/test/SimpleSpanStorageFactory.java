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

package com.navercorp.pinpoint.test;

import com.navercorp.pinpoint.profiler.context.compress.SpanEventCompressor;
import com.navercorp.pinpoint.profiler.context.id.TraceRoot;
import com.navercorp.pinpoint.profiler.context.storage.Storage;
import com.navercorp.pinpoint.profiler.context.storage.StorageFactory;
import com.navercorp.pinpoint.profiler.sender.DataSender;

/**
 * @author hyungil.jeong
 * @author emeroad
 */
public class SimpleSpanStorageFactory implements StorageFactory {

    private final DataSender dataSender;
    private final SpanEventCompressor<Long> spanEventCompressor;

    public SimpleSpanStorageFactory(DataSender dataSender) {
        this(dataSender, null);
    }

    public SimpleSpanStorageFactory(DataSender dataSender, SpanEventCompressor<Long> spanEventCompressor) {
        if (dataSender == null) {
            throw new NullPointerException("dataSender must not be null");
        }
        this.dataSender = dataSender;
        this.spanEventCompressor = spanEventCompressor;
    }

    @Override
    public Storage createStorage(TraceRoot traceRoot) {
        if (spanEventCompressor == null) {
            return new SimpleSpanStorage(this.dataSender);
        } else {
            Storage storage = new SimpleSpanStorage(this.dataSender);
            return new CompressingStorageDecorator(storage, spanEventCompressor);
        }
    }
}
