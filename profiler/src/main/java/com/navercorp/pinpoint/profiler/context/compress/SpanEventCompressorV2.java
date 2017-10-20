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

package com.navercorp.pinpoint.profiler.context.compress;

import com.navercorp.pinpoint.profiler.context.SpanEvent;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class SpanEventCompressorV2 implements SpanEventCompressor<Long> {

    private final static Comparator<SpanEvent> SEQUENCE_COMPARATOR = SpanEventSequenceComparator.INSTANCE;

    @Override
    public void compress(List<SpanEvent> spanEventList, final Long keyTime) {
        // sort list for data compression
        Collections.sort(spanEventList, SEQUENCE_COMPARATOR);

        compressTime(spanEventList, keyTime);
        compressDepth(spanEventList);
    }

    private void compressTime(List<SpanEvent> spanEventList, Long keyTime) {
        long prevKeyTime = keyTime;


        final int size = spanEventList.size();
        for (int i = 0; i < size; i++) {
            final SpanEvent spanEvent = spanEventList.get(i);

            final long startTime = spanEvent.getStartTime();
            final long startElapsedTime = startTime - prevKeyTime;
            spanEvent.setStartElapsed((int) startElapsedTime);


            final long endElapsedTime = spanEvent.getAfterTime() - startTime;
            if (endElapsedTime != 0) {
                spanEvent.setEndElapsed((int) endElapsedTime);
            }

            // save next KeyFrame;
            prevKeyTime = startTime;
        }
    }


    /**
     * Skip depth to Span or SpanChunk scope
     * @param spanEventList
     */
    private void compressDepth(List<SpanEvent> spanEventList) {
        boolean first = true;
        int prevDepth = 0;

        final int size = spanEventList.size();
        for (int i = 0; i < size; i++) {
            final SpanEvent spanEvent = spanEventList.get(i);

            if (first) {
                first = false;
                prevDepth = spanEvent.getDepth();
            } else {
                final int currentDepth = spanEvent.getDepth();
                if (currentDepth == prevDepth) {
                    // skip
                    spanEvent.unsetDepth();
                }
                prevDepth = currentDepth;
            }

        }
    }
}
