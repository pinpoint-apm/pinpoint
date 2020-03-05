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

package com.navercorp.pinpoint.profiler.context.storage;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.context.SpanChunkFactory;
import com.navercorp.pinpoint.profiler.sender.DataSender;

/**
 * @author emeroad
 */
public class BufferedStorageFactory implements StorageFactory {

    private final DataSender dataSender;
    private final int ioBufferingBufferSize;

    public BufferedStorageFactory(int ioBufferingBufferSize, DataSender dataSender) {
        this.dataSender = Assert.requireNonNull(dataSender, "dataSender");
        this.ioBufferingBufferSize = ioBufferingBufferSize;
    }


    @Override
    public Storage createStorage(SpanChunkFactory spanChunkFactory) {
        Storage storage = new BufferedStorage(spanChunkFactory, this.dataSender, this.ioBufferingBufferSize);
        return storage;
    }

    @Override
    public String toString() {
        return "BufferedStorageFactory{" +
                "dataSender=" + dataSender +
                ", ioBufferingBufferSize=" + ioBufferingBufferSize +
                '}';
    }
}
