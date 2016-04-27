/*
 * Copyright 2016 NAVER Corp.
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
package com.navercorp.pinpoint.web.calltree.span;

import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;

/**
 * @author jaehong.kim
 */
public class CallStackMock {
    private static final int STACK_SIZE = 8;
    private static final int DEFAULT_INDEX = 0;
    private static long currentTime = 1;

    private SpanEventBo[] stack = new SpanEventBo[STACK_SIZE];

    private final SpanBo spanBo;
    private int index = DEFAULT_INDEX;
    private short sequence;
    private int latestStackIndex = 0;

    private boolean async = false;
    private int asyncId = 0;
    private CallTree callTree;

    public CallStackMock() {
        this(false, -1);
    }

    public CallStackMock(boolean async, int asyncId) {
        this.spanBo = new SpanBo();
        this.spanBo.setStartTime(currentTime);
        currentTime++;

        this.async = async;
        if (this.async) {
            callTree = new SpanAsyncCallTree(new SpanAlign(spanBo));
        } else {
            callTree = new SpanCallTree(new SpanAlign(spanBo));
        }
        this.asyncId = asyncId;
    }

    public void push() {
        final SpanEventBo spanEvent = new SpanEventBo();
        final int startElapsed = (int) (currentTime - spanBo.getStartTime());
        currentTime++;
        spanEvent.setStartElapsed(startElapsed);
        spanEvent.setSequence(sequence++);

        if (this.async) {
            spanEvent.setAsyncId(asyncId);
        }

        checkExtend(index + 1);
        stack[index++] = spanEvent;
        if (this.latestStackIndex != this.index) {
            this.latestStackIndex = this.index;
            spanEvent.setDepth(this.latestStackIndex);
        }

        callTree.add(spanEvent.getDepth(), new SpanAlign(spanBo, spanEvent));
    }

    private void checkExtend(final int size) {
        final SpanEventBo[] originalStack = this.stack;
        if (size >= originalStack.length) {
            final int copyStackSize = originalStack.length << 1;
            final SpanEventBo[] copyStack = new SpanEventBo[copyStackSize];
            System.arraycopy(originalStack, 0, copyStack, 0, originalStack.length);
            this.stack = copyStack;
        }
    }

    public SpanEventBo pop() {
        final SpanEventBo spanEvent = peek();
        if (spanEvent != null) {
            stack[index - 1] = null;
            index--;
            final int endElapsed = (int) (currentTime - (spanBo.getStartTime() + spanEvent.getStartElapsed()));
            spanEvent.setEndElapsed(endElapsed);
        }

        return spanEvent;
    }

    public SpanEventBo peek() {
        if (index == DEFAULT_INDEX) {
            return null;
        }

        return stack[index - 1];
    }

    public boolean empty() {
        if (this.async) {
            return index == DEFAULT_INDEX + 1;
        }

        return index == DEFAULT_INDEX;
    }

    public int getIndex() {
        return index;
    }

    public void append(CallTree subCallTree) {
        if (peek() == null) {
            callTree.add(0, subCallTree);
        } else {
            callTree.add(peek().getDepth(), subCallTree);
        }
    }

    public CallTree close() {
        if (this.async) {
            pop();
        }
        final int after = (int) (currentTime - spanBo.getStartTime());
        spanBo.setElapsed(after);
        return callTree;
    }
}