/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.plugin.request;

/**
 * @author Woonduk Kang(emeroad)
 */
public class NewTraceHeader implements TraceHeader {

    public static final TraceHeader INSTANCE = new NewTraceHeader();

    @Override
    public TraceHeaderState getState() {
        return TraceHeaderState.NEW_TRACE;
    }

    @Override
    public String getTransactionId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getParentSpanId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getSpanId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public short getFlags() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return "NewTraceHeader";
    }
}
