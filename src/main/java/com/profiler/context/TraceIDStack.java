package com.profiler.context;

/**
 * @author netspider
 */
public class TraceIDStack {

    private TraceID[] stack = new TraceID[4];

    // TODO 개별변수에 volatile 을 건다고 해서 전체의 동시성이 해결되지 않으므로 일단 제거.
    private int index = 0;


    public TraceID getTraceId() {
        return stack[index];
    }

    public TraceID getParentTraceId() {
        if (index > 0) {
            return stack[index - 1];
        }
        return null;
    }

    public void setTraceId(TraceID traceId) {
        stack[index] = traceId;
    }

    public void push() {
        index++;
        if (index > stack.length - 1) {
            TraceID[] old = stack;
            stack = new TraceID[index + 4];
            System.arraycopy(old, 0, stack, 0, old.length);
        }
    }

    public void pop() {
        if (index > 0) {
//            TODO 이전 reference를 제거해야 될거 같음.
//            stack[index] = null;
            index--;
        }
    }

    public void clear() {
        stack[index] = null;
    }
}
