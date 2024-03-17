/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.annotation;

import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.profiler.context.Annotation;

/**
 * @author emeroad
 */
public class ObjectAnnotation implements Annotation<Object> {

    private final int key;
    private final String value;

    ObjectAnnotation(int key, Object value) {
        if (value == null) {
            throw new NullPointerException("value");
        }
        this.key = key;
        // Not recommended Lazy evaluation for Thread safety
        this.value = StringUtils.abbreviate(value.toString());
    }

    @Override
    public int getKey() {
        return key;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "ObjectAnnotation{" +
                key + "=" + value +
                '}';
    }
}
