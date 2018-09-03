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

import com.navercorp.pinpoint.common.util.IntStringStringValue;
import com.navercorp.pinpoint.common.util.IntStringValue;
import com.navercorp.pinpoint.common.util.LongIntIntByteByteStringValue;
import com.navercorp.pinpoint.common.util.StringStringValue;
import com.navercorp.pinpoint.profiler.util.AnnotationValueMapper;

/**
 * @author netspider
 * @author emeroad
 */
public class Annotation {

    private int key;
    private Object value;

    public Annotation(int key) {
        this.key = key;
    }

    public Annotation(int key, Object value) {
        this.key = key;
        this.value = AnnotationValueMapper.checkValueType(value);
    }


    public Annotation(int key, IntStringValue value) {
        this.key = key;
        this.value = value;
    }

    public Annotation(int key, IntStringStringValue value) {
        this.key = key;
        this.value = value;
    }

    public Annotation(int key, StringStringValue value) {
        this.key = key;
        this.value = value;
    }

    public Annotation(int key, String value) {
        this.key = key;
        this.value = value;
    }


    public Annotation(int key, int value) {
        this.key = key;
        this.value = value;
    }

    public Annotation(int key, long value) {
        this.key = key;
        this.value = value;
    }

    public Annotation(int key, LongIntIntByteByteStringValue value) {
        this.key = key;
        this.value = value;
    }

    public int getAnnotationKey() {
        return key;
    }

    public Object getValue() {
        return value;
    }
}
