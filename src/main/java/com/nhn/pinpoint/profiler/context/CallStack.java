package com.nhn.pinpoint.profiler.context;

import com.nhn.pinpoint.exception.PinpointException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * @author netspider
 * @author emeroad
 */
public class CallStack {

    private static final Logger logger = LoggerFactory.getLogger(CallStack.class);
    // 추적 depth크기 제한을 위해서 필요. 해당 사이즈를 넘어갈경우 부드럽게 트레이스를 무시하는 로직이 필요함.
    private static final int TRACE_STACK_MAX_SIZE = 64;

    private final Span span;
    // CallStack을 동시성 환경에서 복사해서 볼수 있는 방법이 필요함.
    private StackFrame[] stack = new StackFrame[8];

    private int index = -1;

    public CallStack(Span span) {
        if (span == null) {
            throw new NullPointerException("span  must not be null");
        }
        this.span = span;
    }

    public Span getSpan() {
        return span;
    }

//    public synchronized int getIndex() {
    public int getIndex() {
        // 일단 락 안잡는 코드로 함.
       return index;
    }

    // copy시의 락 생각할 경우 좀더 정교하게 잡을수 있을듯.
    // push, pop, copy만 락을 잡아도 될거 같은 생각이 듬.
    public synchronized StackFrame getCurrentStackFrame() {
        return stack[index];
    }

    public synchronized StackFrame getParentStackFrame() {
        if (index > 0) {
            return stack[index - 1];
        }
        return null;
    }

    public synchronized void setStackFrame(StackFrame stackFrame) {
        if (stackFrame == null) {
            throw new NullPointerException("stackFrame must not be null");
        }
        stack[index] = stackFrame;
    }

    public synchronized int push() {
        index++;
        if (index > stack.length - 1) {
            StackFrame[] old = stack;
            stack = new StackFrame[index + 4];
            System.arraycopy(old, 0, stack, 0, old.length);
        }
        return index;
    }

    public synchronized int getStackFrameIndex() {
        return index;
    }

    public synchronized void popRoot() {
        if (index >= 0) {
            // stack 전체를 정리하는게 더 좋은가?
            stack[index] = null;
            index--;
        } else {
            PinpointException ex = new PinpointTraceException("Profiler CallStack check. index:" + index + "");
            if (logger.isWarnEnabled()) {
                // 자체 stack dump 필요.
                logger.warn("invalid callStack found stack dump:{}", this, ex);
            }
            throw ex;
        }
    }

    public synchronized StackFrame pop() {
        if (index >= 0) {
            stack[index] = null;
            index--;
            return stack[index];
        } else {
            PinpointException ex = new PinpointException("Profiler CallStack check. index:" + index + "");
            if (logger.isWarnEnabled()) {
                // 자체 stack dump 필요.
                logger.warn("invalid callStack found stack dump:{}", this, ex);
            }
            throw ex;
        }
    }

    public synchronized void currentStackFrameClear() {
        stack[index] = null;
    }

    public StackFrame[] copyStackFrame() {
        int currentIndex;
        StackFrame[] currentStack;
        synchronized (this) {
            // copy reference
            currentIndex = this.index;
            currentStack = this.stack;
        }
        StackFrame[] copy = new StackFrame[currentIndex];
        System.arraycopy(currentStack, 0, copy, 0, currentIndex);
        return copy;
    }

    @Override
    public String toString() {
        return "CallStack{" +
                "stack=" + (stack == null ? null : Arrays.asList(stack)) +
                ", index=" + index +
                '}';
    }
}
