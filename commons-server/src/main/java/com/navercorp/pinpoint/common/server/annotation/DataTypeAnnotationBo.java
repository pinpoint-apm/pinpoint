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

package com.navercorp.pinpoint.common.server.annotation;

import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.util.DataType;

import java.util.Objects;

/**
 * @author emeroad
 */
public class DataTypeAnnotationBo implements AnnotationBo {

    private final int key;
    private final DataType value;
    private final boolean isAuthorized;

    public DataTypeAnnotationBo(int key, DataType value) {
        this(key, value, true);
    }

    public DataTypeAnnotationBo(int key, DataType value, boolean isAuthorized) {
        this.key = key;
        this.value = Objects.requireNonNull(value, "value");
        this.isAuthorized = isAuthorized;
    }

    @Override
    public int getKey() {
        return key;
    }

    @Override
    public Object getValue() {
        return value;
    }

    public DataType getDataType() {
        return value;
    }

    @Override
    public boolean isAuthorized() {
        return isAuthorized;
    }

    @Override
    public String toString() {
        return "AnnotationBo{" +
                "key=" + key +
                ", value=" + value +
                ", isAuthorized=" + isAuthorized +
                '}';
    }
}
