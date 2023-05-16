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

package com.navercorp.pinpoint.profiler.context;

/**
 * @author Woonduk Kang(emeroad)
 */
public class CallStackFactoryV2 implements CallStackFactory<SpanEvent> {

    private final CallStack.Factory<SpanEvent> factory = new SpanEventFactory();
    private final int maxDepth;
    private final int maxSequence;
    private final CallStackOverflowListener overflowListener;

    public CallStackFactoryV2(int maxDepth, int maxSequence, int overflowLogRation) {
        this.maxDepth = maxDepth;
        this.maxSequence = maxSequence;
        if (overflowLogRation > 1) {
            this.overflowListener = new ThrottledLogCallStackOverflowListener(maxDepth, maxSequence, overflowLogRation);
        } else {
            this.overflowListener = new DefaultCallStackOverflowListener(maxDepth, maxSequence);
        }
    }

    @Override
    public CallStack<SpanEvent> newCallStack() {
        final CallStack<SpanEvent> callStack = new DefaultCallStack<>(factory, maxDepth, maxSequence);
        callStack.setOverflowListener(overflowListener);
        return callStack;
    }
}
