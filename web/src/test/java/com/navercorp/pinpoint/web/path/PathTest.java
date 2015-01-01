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

package com.navercorp.pinpoint.web.path;

import java.text.SimpleDateFormat;

import org.junit.Test;

import com.navercorp.pinpoint.thrift.dto.TSpan;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class PathTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    int index;
    SimpleDateFormat format = new SimpleDateFormat("ss.SSS");

    @Test
    public void testPath() {
        // root -> next1 -> next2
        // > - -> next3

        // Span root = root();
        // printSpan("root", root);
        //
        // Span next1 = createNextSpan(root);
        // printSpan("next1", next1);
        //
        // Span next2 = createNextSpan(next1);
        // printSpan("next2", next2);
        //
        // Span next3 = createNextSpan(next1);
        // printSpan("next3", next3);

    }

    public void printSpan(String msg, TSpan span) {
        // System.out.println(msg + " id:" + span.getSpanID() + " pid:" +
        // span.getParentSpanId() + " time:" + format.format(new
        // Date(span.getTimestamp())));
        logger.debug(msg + " id:" + span.getSpanId() + " pid:" + span.getParentSpanId() + " time:" + span.getStartTime());
    }
    // private Span root() {
    // TraceID traceID = TraceID.newTraceId();
    // UUID uuid = traceID.getTransactionSequence();
    // Span root = new Span("test", System.currentTimeMillis(),
    // uuid.getMostSignificantBits(), uuid.getLeastSignificantBits(),
    // Integer.toString(index++), "serviceName", traceID.getSpanId(), null,
    // null, "http:ip:23");
    // root.setParentSpanId(traceID.getParentSpanId());
    // return root;
    // }

    // private Span createNextSpan(Span span) {
    // UUID uuid = new UUID(span.getMostTraceID(), span.getLeastTraceID());
    // TraceID traceID = new TraceID(uuid, span.getParentSpanId(),
    // span.getSpanID(), true, 0);
    // TraceID nextTraceId = traceID.getNextTraceId();
    // Span next = new Span("test", System.currentTimeMillis(),
    // span.getMostTraceID(), span.getLeastTraceID(), Integer.toString(index++),
    // "serviceName", nextTraceId.getSpanId(), null, null, "http:ip:23");
    // next.setParentSpanId(nextTraceId.getParentSpanId());
    // return next;
    // }
}
