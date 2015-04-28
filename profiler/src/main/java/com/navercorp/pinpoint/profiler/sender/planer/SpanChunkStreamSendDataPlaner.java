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

package com.navercorp.pinpoint.profiler.sender.planer;

import java.nio.ByteBuffer;

import com.navercorp.pinpoint.profiler.sender.CompositeSpanStreamData;
import com.navercorp.pinpoint.profiler.sender.SpanStreamSendDataFactory;

/**
 * @author Taejin Koo
 */
public class SpanChunkStreamSendDataPlaner extends AbstractSpanStreamSendDataPlaner {

    private final int componentsLastIndex;

    public SpanChunkStreamSendDataPlaner(CompositeSpanStreamData compositeSpanStreamData, SpanStreamSendDataFactory spanStreamSendDataFactory) {
        super(compositeSpanStreamData, spanStreamSendDataFactory);

        int componentsCount = compositeSpanStreamData.getComponentsCount();

        if (componentsCount <= 0) {
            throw new IllegalArgumentException("compositeSpanStreamData.getComponentsCount()=" + componentsCount);
        } else {
            componentsLastIndex = componentsCount - 1;
        }
    }

    @Override
    protected int getSpanChunkLength() {
        return compositeSpanStreamData.getComponentBufferLength(componentsLastIndex);
    }

    @Override
    protected ByteBuffer getSpanChunkBuffer() {
        return compositeSpanStreamData.getByteBuffer(componentsLastIndex);
    }

}
