/*
 * Copyright 2014 NAVER Corp.
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
package com.navercorp.pinpoint.profiler.util;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jongho Moon
 *
 */
public class NameValueList<T> {
    private final List<NameValue<T>> list;
    
    public NameValueList() {
        this(0);
    }

    public NameValueList(int size) {
        this.list = new ArrayList<NameValue<T>>(size);
    }
    
    public T add(String name, T value) {
        for (NameValue<T> e : list) {
            if (name.equals(e.name)) {
                T old = e.value;
                e.value = value;
                
                return old;
            }
        }
        
        list.add(new NameValue<T>(name, value));
        return null;
    }

    public T get(String name) {
        for (NameValue<T> e : list) {
            if (name.equals(e.name)) {
                return e.value;
            }
        }

        return null;
    }
    
    public T remove(String name) {
        int len = list.size();
        
        for (int i = len - 1; i >= 0; i--) {
            NameValue<T> e = list.get(i);
            
            if (name.equals(e.name)) {
                list.remove(i);
                return e.value;
            }
        }
        
        return null;
    }
    
    public void clear() {
        list.clear();
    }
    
    private static final class NameValue<T> {
        private final String name;
        private T value;
        
        public NameValue(String name, T value) {
            this.name = name;
            this.value = value;
        }
    }
}
