/*
 * Copyright 2017 NAVER Corp.
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
public class DefaultReference<V> implements Reference<V> {
    private V value;

    @Override
    public V get() {
        return value;
    }


    @Override
    public void set(V value) {
        this.value = value;
    }

    @Override
    public V clear() {
        final V copy = this.value;
        this.value = null;
        return copy;
    }



    private static final Reference<Object> EMPTY = new DefaultReference<Object>() {
        @Override
        public void set(Object value) {
            throw new IllegalStateException("unsupported set:" + value);
        }
    };

    @SuppressWarnings("unchecked")
    public static <V> Reference<V> emptyReference() {
        return (Reference<V>) EMPTY;
    }
}
