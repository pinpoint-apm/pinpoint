package com.profiler.context;

/**
 * @author netspider
 */
public class CallStack {
    // CallStack을 동시성 환경에서 복사해서 볼수 있는 방법이 필요함.
    private StackFrame[] stack = new StackFrame[4];

    // TODO 개별변수에 volatile을해도 동시성이 해결되지 않으므로 일단 제거.
    private int index = 0;


    public StackFrame getCurrentStackFrame() {
        return stack[index];
    }

    public StackFrame getParentStackFrame() {
        if (index > 0) {
            return stack[index - 1];
        }
        return null;
    }


    public void setStackFrame(StackFrame stackFrame) {
        stack[index] = stackFrame;
    }

    public void push() {
        index++;
        if (index > stack.length - 1) {
            StackFrame[] old = stack;
            stack = new StackFrame[index + 4];
            System.arraycopy(old, 0, stack, 0, old.length);
        }
    }

    public int getStackFrameIndex() {
        return index;
    }

    public void pop() {
        if (index > 0) {
//            TODO 이전 reference를 제거해야 되나?
            index--;
        }
    }

    public void currentStackFrameClear() {
        stack[index] = null;
    }
}
