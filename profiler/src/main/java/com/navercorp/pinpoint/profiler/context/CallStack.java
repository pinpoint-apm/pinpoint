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

import java.util.Arrays;

/**
 * @author netspider
 * @author emeroad
 * @author jaehong.kim
 */
public class CallStack {
    private static final int STACK_SIZE = 8;
    private static final int DEFAULT_INDEX = 0;

    private SpanEvent[] stack = new SpanEvent[STACK_SIZE];

    private final Span span;
    private final int maxDepth;
    private int index = DEFAULT_INDEX;
    private int overflowIndex = 0;
    private short sequence;
    private int latestStackIndex = 0;

    public CallStack(Span span) {
        this(span, -1);
    }
    
    public CallStack(Span span, int maxDepth) {
        this.span = span;
        this.maxDepth = maxDepth;
    }
    
    public Span getSpan() {
        return span;
    }
    
    public int getIndex() {
        if(isOverflow()) {
            return index + overflowIndex;
        }
        
        return index;
    }

    public int push(final SpanEvent spanEvent) {
        if (isOverflow()) {
            overflowIndex++;
            return index + overflowIndex;
        }

        checkExtend(index + 1);
        spanEvent.setSequence(sequence++);
        stack[index++] = spanEvent;
        if(latestStackIndex != index) {
            latestStackIndex = index;
            spanEvent.setDepth(latestStackIndex);
        }

        return index;
    }

    private void checkExtend(final int size) {
        final SpanEvent[] originalStack = this.stack;
        if (size >= originalStack.length) {
            final int copyStackSize = originalStack.length << 1;
            final SpanEvent[] copyStack = new SpanEvent[copyStackSize];
            System.arraycopy(originalStack, 0, copyStack, 0, originalStack.length);
            this.stack = copyStack;
        }
    }

    public SpanEvent pop() {
        if(isOverflow() && overflowIndex > 0) {
            overflowIndex--;
            return new SpanEvent(span);
        }
        
        final SpanEvent spanEvent = peek();
        if (spanEvent != null) {
            stack[index - 1] = null;
            index--;
        }

        return spanEvent;
    }

    public SpanEvent peek() {
        if (index == DEFAULT_INDEX) {
            return null;
        }
        
        if(isOverflow() && overflowIndex > 0) {
            return new SpanEvent(span);
        }

        return stack[index - 1];
    }

    public boolean empty() {
        return index == DEFAULT_INDEX;
    }

    public SpanEvent[] copyStackFrame() {
        // without synchronization arraycopy, last index is null reference
        final SpanEvent[] currentStack = this.stack;
        final SpanEvent[] copyStack = new SpanEvent[currentStack.length];
        System.arraycopy(currentStack, 0, copyStack, 0, currentStack.length);
        return copyStack;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    boolean isOverflow() {
        return maxDepth != -1 && maxDepth < index;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{stack=");
        builder.append(Arrays.toString(stack));
        builder.append(", index=");
        builder.append(index);
        builder.append("}");
        return builder.toString();
    }
}