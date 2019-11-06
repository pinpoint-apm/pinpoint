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

package com.navercorp.pinpoint.profiler.context.storage;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.context.SpanChunkFactory;

/**
 * @author Woonduk Kang(emeroad)
 */
public class TraceLogDelegateStorageFactory implements StorageFactory {

    private final StorageFactory delegate;

    public TraceLogDelegateStorageFactory(StorageFactory delegate) {
        this.delegate = Assert.requireNonNull(delegate, "delegate");
    }


    @Override
    public Storage createStorage(SpanChunkFactory spanChunkFactory) {
        Storage storage = delegate.createStorage(spanChunkFactory);
        return new TraceLogDelegateStorage(storage);
    }
}
