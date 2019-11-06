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
public interface CallStack<T> {
    int getIndex();

    int push(T element);

    T pop();

    T peek();

    boolean empty();

    T[] copyStackFrame();

    int getMaxDepth();

    Factory<T> getFactory();

    interface Factory<T> {
        Class<T> getType();

        T newInstance();

        T dummyInstance();

        boolean isDummy(T element);

        void markDepth(T element, int index);

        void setSequence(T element, short sequence);
    }
}
