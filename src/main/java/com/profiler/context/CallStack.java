package com.profiler.context;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author netspider
 */
public class CallStack {

    // CallStack을 동시성 환경에서 복사해서 볼수 있는 방법이 필요함.
    private StackFrame[] stack = new StackFrame[4];

    // 추적 depth크기 제한을 위해서 필요. 해당 사이즈를 넘어갈경우 부드럽게 트레이스를 무시하는 로직이 필요함.
    private final int TRACE_STACK_MAX_SIZE = 64;

    private int index = -1;

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
        stack[index] = stackFrame;
    }

    public synchronized void push() {
        index++;
        if (index > stack.length - 1) {
            StackFrame[] old = stack;
            stack = new StackFrame[index + 4];
            System.arraycopy(old, 0, stack, 0, old.length);
        }
    }

    public synchronized int getStackFrameIndex() {
        return index;
    }

    public synchronized void pop() {
        if (index >= 0) {
            stack[index] = null;
            index--;
        } else {
            Logger logger = Logger.getLogger(this.getClass().getName());
            if (logger.isLoggable(Level.WARNING)) {
                // 자체 stack dump 필요.
                Exception ex = new Exception("Profiler CallStack check. index:" + index);
                logger.log(Level.WARNING, "invalid callStack found", ex);
            }
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
}
