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

package com.navercorp.pinpoint.web.vo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.profiler.util.TransactionIdUtils;

/**
 * @author emeroad
 */
public class BusinessTransaction {
    private final List<Trace> traces = new ArrayList<>();
    private final String rpc;

    private int calls = 0;
    private int error = 0;
    private long totalTime = 0;
    private long maxTime = 0;
    private long minTime = 0;

    public BusinessTransaction(SpanBo span) {
        Objects.requireNonNull(span, "span");

        this.rpc = span.getRpc();

        long elapsed = span.getElapsed();
        totalTime = maxTime = minTime = elapsed;

        String transactionIdString = TransactionIdUtils.formatString(span.getTransactionId());
        Trace trace = new Trace(transactionIdString, elapsed, span.getCollectorAcceptTime(), span.getErrCode());
        this.traces.add(trace);
        calls++;
        if (span.getErrCode() > 0) {
            error++;
        }
    }

    public void add(SpanBo span) {
        Objects.requireNonNull(span, "span");

        long elapsed = span.getElapsed();

        this.totalTime += elapsed;
        this.maxTime = Math.max(this.maxTime, elapsed);
        this.minTime = Math.min(this.minTime, elapsed);

        String transactionIdString = TransactionIdUtils.formatString(span.getTransactionId());
        Trace trace = new Trace(transactionIdString, elapsed, span.getCollectorAcceptTime(), span.getErrCode());
        this.traces.add(trace);

        if (span.getErrCode() > 0) {
            error++;
        }

        //if (span.getParentSpanId() == -1) {
            calls++;
        //}
    }

    public String getRpc() {
        return rpc;
    }

    public List<Trace> getTraces() {
        return traces;
    }

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
