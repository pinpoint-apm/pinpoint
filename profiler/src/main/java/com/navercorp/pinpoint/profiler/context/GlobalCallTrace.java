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

import com.navercorp.pinpoint.bootstrap.context.AsyncTrace;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 뭔가 복잡한 비동기 call trace시 객체 등록용으로 쓰자.
 * 근데 손좀봐야 될듯.뭔가 좀 구림.
 * @author emeroad
 */
@Deprecated
public class GlobalCallTrace {

    private static final long FLUSH_TIMEOUT = 120000L; // 2 minutes

    private static final AtomicInteger timerId = new AtomicInteger(0);

    private ConcurrentMap<Integer, AsyncTrace> trace = new ConcurrentHashMap<Integer, AsyncTrace>(32);
    private AtomicInteger idGenerator = new AtomicInteger(0);
    // stop을 해줘야 할듯.
    private Timer timer = new Timer("Pinpoint-GlobalCallTrace-Timer-" + timerId.getAndIncrement(), true);

    public int registerTraceObject(AsyncTrace asyncTrace) {
        // TODO 연관관계가 전달부분이 영 별로임.

        TimeoutTask timeoutTask = new TimeoutTask(trace, asyncTrace.getAsyncId());
        asyncTrace.setTimeoutTask(timeoutTask);

        int id = put(asyncTrace);
        asyncTrace.setAsyncId(id);
        timer.schedule(timeoutTask, FLUSH_TIMEOUT);
        return id;
    }

    private int put(AsyncTrace asyncTrace) {
        int id = idGenerator.getAndIncrement();
        trace.put(id, (DefaultAsyncTrace)asyncTrace);
        return id;
    }

    public AsyncTrace getTraceObject(int asyncId) {
        return trace.get(asyncId);
    }

    public AsyncTrace removeTraceObject(int asyncId) {
        AsyncTrace asyncTrace = trace.remove(asyncId);
        if (asyncTrace != null) {
            boolean result = ((DefaultAsyncTrace)asyncTrace).fire();
            if (!result) {
                // 이미 timeout된 asyncTrace임.
                return null;
            }
        }
        return asyncTrace;
    }


    private static class TimeoutTask extends TimerTask {
        private ConcurrentMap<Integer, AsyncTrace> trace;
        private int id;
//        private final AsyncTrace asyncTrace;

        public TimeoutTask(ConcurrentMap<Integer, AsyncTrace> trace, int id) {
            this.trace = trace;
            this.id = id;
        }

        @Override
        public void run() {
            DefaultAsyncTrace asyncTrace = (DefaultAsyncTrace) trace.remove(id);
            if (asyncTrace != null) {
                asyncTrace.timeout();
            }
        }
    }
}
