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

package com.navercorp.pinpoint.rpc.util;

import java.util.List;

public final class ListUtils {

    private ListUtils() {
    }

    public static <V> boolean addIfValueNotNull(List<V> list, V value) {
        if (value == null) {
            return false;
        }

        return list.add(value);
    }
    
    public static <V> boolean addAllIfAllValuesNotNull(List<V> list, V[] values) {
        if (values == null) {
            return false;
        }
        
        for (V value : values) {
            if (value == null) {
                return false;
            }
        }
        
        for (V value : values) {
            list.add(value);
        }
        
        return true;
    }
    
    public static <V> void addAllExceptNullValue(List<V> list, V[] values) {
        if (values == null) {
            return;
        }
        
        for (V value : values) {
            addIfValueNotNull(list, value);
        }
    }
    
}
