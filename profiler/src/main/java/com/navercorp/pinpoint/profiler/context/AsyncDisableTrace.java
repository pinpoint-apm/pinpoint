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

package com.navercorp.pinpoint.profiler.context;

import com.navercorp.pinpoint.bootstrap.context.AsyncState;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.profiler.context.id.LocalTraceRoot;

import java.util.Objects;


/**
 * @author emeroad
 * @author jaehong.kim
 */
public class AsyncDisableTrace extends DisableTrace {

    private final AsyncState asyncState;

    public AsyncDisableTrace(LocalTraceRoot traceRoot,
                             SpanRecorder spanRecorder, SpanEventRecorder spanEventRecorder,
                             AsyncState asyncState) {
        super(traceRoot, spanRecorder, spanEventRecorder, CloseListener.EMPTY);
        this.asyncState = Objects.requireNonNull(asyncState, "asyncState");
    }

    @Override
    public void close() {
        if (isClosed()) {
            logger.debug("Already closed");
            return;
        }
        if (asyncState.await()) {
            // flush.
            super.flush();
            if (isDebug) {
                logger.debug("Await trace={}, asyncState={}", this, this.asyncState);
            }
        } else {
            // close.
            super.close();
            if (isDebug) {
                logger.debug("Close trace={}. asyncState={}", this, this.asyncState);
            }
        }
    }

    @Override
    public String toString() {
        return "AsyncDisableTrace{" +
                "asyncState=" + asyncState +
                "} " + super.toString();
    }
}