/*
 * Copyright 2016 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.profiler.context.storage.flush;

import com.navercorp.pinpoint.profiler.context.Span;
import com.navercorp.pinpoint.profiler.context.SpanChunk;
import com.navercorp.pinpoint.profiler.sender.DataSender;

/**
 * @author Taejin Koo
 */
public class RemoteFlusher implements StorageFlusher {

    private final DataSender dataSender;

    public RemoteFlusher(DataSender dataSender) {
        if (dataSender == null) {
            throw new NullPointerException("dataSender may not be null");
        }

        this.dataSender = dataSender;
    }

    @Override
    public void flush(SpanChunk spanChunk) {
        dataSender.send(spanChunk);
    }

    @Override
    public void flush(Span span) {
        dataSender.send(span);
    }

    @Override
    public void stop() {
        // do nothing
    }

    @Override
    public String toString() {
        return "RemoteFlusher{" +
                "dataSender=" + dataSender +
                '}';
    }

}
