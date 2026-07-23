/*
 * Copyright 2026 NAVER Corp.
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

package com.navercorp.pinpoint.common.buffer;

/**
 * Creates a {@link StringAllocator} per decode scope. Callers must call
 * {@link #create()} once per scope (e.g. per row) and not share the returned
 * allocator across scopes: a caching allocator keys on buffers over the
 * decoded data's backing array, so it must not outlive that array.
 */
public interface StringAllocatorFactory {

    StringAllocator create();

    /**
     * No caching; returns the shared stateless allocator.
     */
    StringAllocatorFactory DEFAULT = new DefaultStringAllocatorFactory();

    /**
     * A fresh LRU-caching allocator per {@link #create()} call.
     */
    static StringAllocatorFactory cached(int cacheSize) {
        return new CachedStringAllocatorFactory(cacheSize);
    }


    class DefaultStringAllocatorFactory implements StringAllocatorFactory {

        @Override
        public StringAllocator create() {
            // stateless
            return StringAllocator.DEFAULT_ALLOCATOR;
        }

        @Override
        public String toString() {
            return "DefaultStringAllocatorFactory";
        }
    }


    class CachedStringAllocatorFactory implements StringAllocatorFactory {

        private final int cacheSize;

        public CachedStringAllocatorFactory(int cacheSize) {
            this.cacheSize = cacheSize;
        }

        @Override
        public StringAllocator create() {
            return new CachedStringAllocator(cacheSize);
        }

        @Override
        public String toString() {
            return "CachedStringAllocatorFactory{cacheSize=" + cacheSize + '}';
        }
    }
}
