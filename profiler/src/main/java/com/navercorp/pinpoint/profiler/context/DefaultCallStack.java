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

import com.navercorp.pinpoint.common.annotations.VisibleForTesting;

import java.lang.reflect.Array;
import java.util.Arrays;

/**
 * @author netspider
 * @author Woonduk Kang(emeroad)
 * @author jaehong.kim
 */
public class DefaultCallStack<T> implements CallStack<T> {

    protected static final int STACK_SIZE = 8;
    protected static final int DEFAULT_INDEX = 0;

    protected final Factory<T> factory;
    protected T[] stack;

    protected final int maxDepth;
    protected int index = DEFAULT_INDEX;
    protected int overflowIndex = 0;
    protected short sequence;

    public DefaultCallStack(Factory<T> factory) {
        this(factory, -1);
    }

    @SuppressWarnings("unchecked")
    public DefaultCallStack(Factory<T> factory, int maxDepth) {
        this.factory = factory;
        this.maxDepth = maxDepth;

        this.stack = newStack(factory.getType(), STACK_SIZE);
    }

    @SuppressWarnings("unchecked")
    private T[] newStack(Class<T> type, int size) {
        return (T[]) Array.newInstance(type, size);
    }


    @Override
    public int getIndex() {
        if (isOverflow()) {
            return index + overflowIndex;
        }

        return index;
    }

    @Override
    public int push(final T element) {
        if (isOverflow()) {
            overflowIndex++;
            return index + overflowIndex;
        }

        checkExtend(index + 1);
        factory.setSequence(element, sequence++);
        stack[index++] = element;
        markDepth(element, index);
        return index;
    }

    protected void markDepth(T element, int index) {
        factory.markDepth(element, index);
    }


    private void checkExtend(final int size) {
        final T[] originalStack = this.stack;
        if (size >= originalStack.length) {
            final int copyStackSize = originalStack.length << 1;
            final T[] copyStack = newStack(factory.getType(), copyStackSize);
            System.arraycopy(originalStack, 0, copyStack, 0, originalStack.length);
            this.stack = copyStack;
        }
    }

    @Override
    public T pop() {
        if (isOverflow() && overflowIndex > 0) {
            overflowIndex--;
            return factory.dummyInstance();
        }

        final T spanEvent = peek();
        if (spanEvent != null) {
            stack[index - 1] = null;
            index--;
        }
        return spanEvent;
    }

    @Override
    public T peek() {
        if (index == DEFAULT_INDEX) {
            return null;
        }

        if (isOverflow() && overflowIndex > 0) {
            return factory.dummyInstance();
        }
        return stack[index - 1];
    }

    @Override
    public boolean empty() {
        return index == DEFAULT_INDEX;
    }

    @Override
    public T[] copyStackFrame() {
        // without synchronization arraycopy, last index is null reference
        final T[] currentStack = this.stack;
        final T[] copyStack = newStack(factory.getType(), currentStack.length);
        System.arraycopy(currentStack, 0, copyStack, 0, currentStack.length);
        return copyStack;
    }

    @Override
    public int getMaxDepth() {
        return maxDepth;
    }

    @VisibleForTesting
    boolean isOverflow() {
        return maxDepth != -1 && maxDepth < index;
    }

    @Override
    public Factory<T> getFactory() {
        return factory;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{stack=");
        builder.append(Arrays.toString(stack));
        builder.append(", index=");
        builder.append(index);
        builder.append(", factory=");
        builder.append(factory);
        builder.append("}");
        return builder.toString();
    }
}