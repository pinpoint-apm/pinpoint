/*
 * Copyright 2019 NAVER Corp.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class SpanEventBuilder {
    private int sequence = 0;
    private long starttime = 0;
    private long starttimeMultiplier = 1;

    private List<SpanEvent> spanEventList = new ArrayList<SpanEvent>();

    public void addSpanEvent() {
        SpanEvent spanEvent = new SpanEvent();
        spanEvent.setSequence((short) nextSequence());
        spanEvent.setStartTime(nextStartTime());
        spanEventList.add(spanEvent);

    }

    public long nextStartTime() {
        return starttime += starttimeMultiplier;
    }

    public int nextSequence() {
        return sequence++;
    }

    public void shuffle() {
        Collections.shuffle(spanEventList);
    }

    public List<SpanEvent> getSpanEventList() {
        return new ArrayList<SpanEvent>(spanEventList);
    }
}
