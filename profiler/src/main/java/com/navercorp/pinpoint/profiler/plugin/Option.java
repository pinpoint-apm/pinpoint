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

package com.navercorp.pinpoint.profiler.plugin;

// TODO move package
public abstract class Option<T> {
    public abstract T getValue();
    public abstract boolean hasValue();
    
    public static <U> Option<U> withValue(U value) {
        return new WithValue<U>(value);
    }
    
    @SuppressWarnings("unchecked")
    public static <U> Option<U> empty() {
        return (Option<U>)EMPTY;
    }
    
    private static final class WithValue<T> extends Option<T> {
        private final T value;
        
        private WithValue(T value) {
            this.value = value;
        }

        @Override
        public T getValue() {
            return value;
        }

        @Override
        public boolean hasValue() {
            return true;
        }
        
    }
 
    private static final Option<Object> EMPTY = new Option<Object>() {

        @Override
        public Object getValue() {
            return null;
        }

        @Override
        public boolean hasValue() {
            return false;
        }
    };
    
}
