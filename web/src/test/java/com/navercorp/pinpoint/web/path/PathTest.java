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
    SimpleDateFormat format = new SimpleDateFormat("ss.SSS")

	    Test
	public void test       ath() {
		// root ->        ext1 -> next
		// > - -> next3
		// Span root = root(             ;
		// printSpan("root", root);
       	//
		// Span next1 = cre             teNextSpan(root);
		// printSpan(       next1", next1);
		//
		//             Span next2 = createNextSpan(next1       ;
		// printSpan("next2",         xt2);
		//
		// Span next3 = createNextSpan(n       xt1);
		// printSpan("next3", next3);

	}

	public void print       pan(String msg, TSpan span) {
		// System.out.printl       (msg + " id:" + span.getSpa       ID() + " pid:" +
		// span.getParentSpanId() + " time:" + format.format(new
		// Date(span.getTimestamp())));
       	logger.debug(msg + "     d:" + span.getSpanId() + " pid:" + span.    etParentSpanId() + " time:" + span.getStartTim    ());
	}
	// private Span root() {
	// TraceID traceID = T    aceID.newTraceId();
	// UUID uuid = traceID.getTransactionSeque    ce();
	// Span root = new Span("test", System.currentTimeMillis(),
	/     uuid.getMostSignific    ntBits(), uuid.getLeastSignificantBits(),
	// Int    ger.toString(    nde    ++), "serviceName", traceID.getSpanId(),     ull,
	// null, "http:ip:23");
	// root.setParentSpanId(traceID.getPar    ntSpanId());
	// return root;
	// }

	// private Span create    extSpan(Span span) {
	// UUI     uuid = new UUID(span.getMostTraceID(), span.get    eastTraceID());
	// TraceID traceID = new TraceID(uuid, s    an.getParentSpanId(),
	// span.getSpanID(), true, 0);
	// TraceID nextTrac    Id = traceID.getNextTraceId();
	// Span next = new Span("test", Sys    em.currentTimeMillis(),
	// span.getMostTraceID(), sp    n.getLeastTra    eID(), Integer.toString(index++),
	// "serviceName", nextTraceId.getSpanId(), null, null, "http:ip:23");
	// next.setParentSpanId(nextTraceId.getParentSpanId());
	// return next;
	// }
}
