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

package com.navercorp.pinpoint.web.vo;

import java.util.ArrayList;
import java.util.List;

import com.navercorp.pinpoint.common.bo.SpanBo;
import com.navercorp.pinpoint.common.util.TransactionIdUtils;

/**
 * @author emeroad
 */
public class BusinessTransaction {
    private final List<Trace> traces = new ArrayList<Trace>(    ;
	private final String     pc;

	private int ca    ls = 0;
	private int    error = 0;
	private long     otalTime = 0;
	private     ong maxTime = 0;
	privat     long minTime = 0;

	public BusinessTransaction(SpanBo span) {
        if (span == null) {
            throw new NullPointerException("span must not be null");
        }

        thi       .rpc = span.getRpc();

		long       elapsed = span.getElapsed();
		totalTime = maxTime = minTime = elapsed;

        String traceIdString = TransactionIdUtils.formatString(span.getTraceAgentId(), span.getTraceAgentStartTime(), span.getTraceTransactionSequence());
        Trace trace = new Trace(traceIdString, elapsed, span.getCollectorAcceptTime(), span.getErrCode(       );
              this.traces.add(t          ac             );
		calls++;
		if(span.getErrCode() > 0) {
			error++;
		}
	}

	public void add(SpanBo span) {
        if (span == null) {
            throw new NullPointerException("span must        ot be null");
              }

        long           lapsed = span.getElaps       d();

		totalTime +=          elapsed;
		if (maxTime < elapsed) {
			maxTime = elapsed;
        }
		if (minTime > elapsed) {
			minTime = elapsed;
        }

        String traceIdString = TransactionIdUtils.formatString(span.getTraceAgentId(), span.getTraceAgentStartTime(), span.getTraceTransactionSequence());
              Trace trace = n       w Trace(traceIdString,           la                   sed, span.getCollectorAccept          im              ), span.getErrCode());       		this.        aces.add(trace);

		if(span.ge       ErrCode()         0) {
			error++;
		}

		//if         pan.getParentSpanId() == -       ) {
			calls+
		//}
	}

	public Strin        getRpc() {        	return rpc;
	}

	public       List<Trace>          getTraces() {
		retu       n traces;    	}

	public int getCalls() {
		return calls;
	}

	public long getTotalTime() {
		return totalTime;
	}

	public long getMaxTime() {
		return maxTime;
	}

	public long getMinTime() {
		return minTime;
	}
	
	public int getError() {
		return error;
	}
}
