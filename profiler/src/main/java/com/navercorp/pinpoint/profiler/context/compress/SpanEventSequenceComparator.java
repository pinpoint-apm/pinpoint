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

import java.util.Comparator;

/**
 * @author Woonduk Kang(emeroad)
 */
public class SpanEventSequenceComparator implements Comparator<SpanEvent> {

    public static final Comparator<SpanEvent> INSTANCE = new SpanEventSequenceComparator();

    @Override
    public int compare(SpanEvent o1, SpanEvent o2) {
        return compareShort(o1.getSequence(), o2.getSequence());
    }

    private static int compareShort(short x, short y) {
        return x - y;
    }
}
