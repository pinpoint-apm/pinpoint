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

import com.navercorp.pinpoint.profiler.util.AnnotationValueMapper;
import com.navercorp.pinpoint.thrift.dto.TAnnotation;
import com.navercorp.pinpoint.thrift.dto.TAnnotationValue;
import com.navercorp.pinpoint.thrift.dto.TIntStringStringValue;
import com.navercorp.pinpoint.thrift.dto.TIntStringValue;
import com.navercorp.pinpoint.thrift.dto.TLongIntIntByteByteStringValue;

/**
 * @author netspider
 * @author emeroad
 */
public class Annotation extends TAnnotation {

    public Annotation(int key) {
        super(key);
    }

    public Annotation(int key, Object value) {
        super(key);
        AnnotationValueMapper.mappingValue(this, value);
    }

    public Annotation(int key, TIntStringValue value) {
        super(key);
        this.setValue(TAnnotationValue.intStringValue(value));
    }

    public Annotation(int key, TIntStringStringValue value) {
        super(key);
        this.setValue(TAnnotationValue.intStringStringValue(value));
    }

    public Annotation(int key, String value) {
        super(key);
        this.setValue(TAnnotationValue.stringValue(value));
    }

    public Annotation(int key, int value) {
        super(key);
        this.setValue(TAnnotationValue.intValue(value));
    }

    public Annotation(int key, TLongIntIntByteByteStringValue value) {
        super(key);
        this.setValue(TAnnotationValue.longIntIntByteByteStringValue(value));
    }

    public int getAnnotationKey() {
        return this.getKey();
    }

}
