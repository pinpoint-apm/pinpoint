package com.nhn.pinpoint.profiler.context;

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
