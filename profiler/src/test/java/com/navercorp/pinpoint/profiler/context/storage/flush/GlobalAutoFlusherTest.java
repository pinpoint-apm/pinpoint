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

import org.junit.Assert;
import org.junit.Test;

import com.navercorp.pinpoint.profiler.context.RandomTSpan;
import com.navercorp.pinpoint.profiler.context.Span;
import com.navercorp.pinpoint.profiler.context.SpanChunk;
import com.navercorp.pinpoint.profiler.sender.CountingDataSender;

/**
 * @author Taejin Koo
 */
public class GlobalAutoFlusherTest {

    private final int maxBufferSize = 20;

    private RandomTSpan randomSpanFactory = new RandomTSpan();

    @Test
    public void defaultTest() throws Exception {
        CountingDataSender countingDataSender = new CountingDataSender();
        GlobalAutoFlusher globalAutoFlusher = new GlobalAutoFlusher(countingDataSender, maxBufferSize);

        globalAutoFlusher.start(500);

        try {
            int eachSpanEventSize = getValue(maxBufferSize, 30);

            Span span = randomSpanFactory.createSpan(eachSpanEventSize);
            globalAutoFlusher.flush(span);
            SpanChunk spanChunk = randomSpanFactory.createSpanChunk(eachSpanEventSize);
            globalAutoFlusher.flush(spanChunk);
            span = randomSpanFactory.createSpan(eachSpanEventSize);
            globalAutoFlusher.flush(span);
            spanChunk = randomSpanFactory.createSpanChunk(eachSpanEventSize);
            globalAutoFlusher.flush(spanChunk);

            Thread.sleep(2000);

            Assert.assertEquals(2, countingDataSender.getSenderCounter());
            Assert.assertEquals(2, countingDataSender.getSpanChunkCounter() + countingDataSender.getSpanCounter() + countingDataSender.getSpanAndSpanChunkListCounter());
        } finally {
            globalAutoFlusher.stop();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentExceptionTest1() throws Exception {
        CountingDataSender countingDataSender = new CountingDataSender();
        GlobalAutoFlusher globalAutoFlusher = new GlobalAutoFlusher(countingDataSender, maxBufferSize);

        Span span = randomSpanFactory.createSpan(maxBufferSize + 1);
        globalAutoFlusher.flush(span);
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentExceptionTest2() throws Exception {
        CountingDataSender countingDataSender = new CountingDataSender();
        GlobalAutoFlusher globalAutoFlusher = new GlobalAutoFlusher(countingDataSender, maxBufferSize);

        SpanChunk spanChunk = randomSpanFactory.createSpanChunk(maxBufferSize + 1);
        globalAutoFlusher.flush(spanChunk);
    }

    private int getValue(int value, int percentage) {

        if (percentage < 0 || percentage > 100) {
            throw new IllegalArgumentException("percentage number must be between 0 and 100");
        }


        return (int) (value * (percentage / 100.0f));
    }

}
