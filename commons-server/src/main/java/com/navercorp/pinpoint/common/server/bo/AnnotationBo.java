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

package com.navercorp.pinpoint.common.server.bo;

import com.navercorp.pinpoint.common.server.annotation.BinaryAnnotationBo;
import com.navercorp.pinpoint.common.server.annotation.BooleanAnnotationBo;
import com.navercorp.pinpoint.common.server.annotation.ByteAnnotationBo;
import com.navercorp.pinpoint.common.server.annotation.DataTypeAnnotationBo;
import com.navercorp.pinpoint.common.server.annotation.DoubleAnnotationBo;
import com.navercorp.pinpoint.common.server.annotation.FloatAnnotationBo;
import com.navercorp.pinpoint.common.server.annotation.IntAnnotationBo;
import com.navercorp.pinpoint.common.server.annotation.LongAnnotationBo;
import com.navercorp.pinpoint.common.server.annotation.NullAnnotationBo;
import com.navercorp.pinpoint.common.server.annotation.ObjectAnnotationBo;
import com.navercorp.pinpoint.common.server.annotation.ShortAnnotationBo;
import com.navercorp.pinpoint.common.server.annotation.StringAnnotationBo;
import com.navercorp.pinpoint.common.util.DataType;

/**
 * @author emeroad
 */
public interface AnnotationBo {
    static AnnotationBo of(int key, Object value) {
        return of(key, value, true);
    }

    static AnnotationBo of(int key, Object value, boolean isAuthorized) {
        if (value == null) {
            return new NullAnnotationBo(key, isAuthorized);
        } else if (value instanceof String strValue) {
            return new StringAnnotationBo(key, strValue, isAuthorized);
        }
        if (value instanceof Number) {
            if (value instanceof Long longValue) {
                return new LongAnnotationBo(key, longValue, isAuthorized);
            } else if (value instanceof Integer intValue) {
                return new IntAnnotationBo(key, intValue, isAuthorized);
            } else if (value instanceof Double doubleValue) {
                return new DoubleAnnotationBo(key, doubleValue, isAuthorized);
            } else if (value instanceof Short shortValue) {
                return new ShortAnnotationBo(key, shortValue, isAuthorized);
            } else if (value instanceof Byte byteValue){
                return new ByteAnnotationBo(key, byteValue, isAuthorized);
            } else if (value instanceof Float floatValue){
                return new FloatAnnotationBo(key, floatValue, isAuthorized);
            }
        } else if (value instanceof Boolean boolValue) {
            return new BooleanAnnotationBo(key, boolValue, isAuthorized);
        } else if (value instanceof byte[] byteArray){
            return new BinaryAnnotationBo(key, byteArray, isAuthorized);
        } else if (value instanceof DataType dataTypeValue) {
            return new DataTypeAnnotationBo(key, dataTypeValue, isAuthorized);
        }
        return new ObjectAnnotationBo(key, value, isAuthorized);
    }

    static AnnotationBo of(int key, String value) {
        return  new StringAnnotationBo(key, value);
    }

    static AnnotationBo of(int key, long value) {
        return new LongAnnotationBo(key, value);
    }

    static AnnotationBo of(int key, int value) {
        return new IntAnnotationBo(key, value);
    }

    static AnnotationBo unauthorized(int key, Object value) {
        return of(key, value, false);
    }

    static AnnotationBo unauthorized(int key, String value) {
        return new StringAnnotationBo(key, value, false);
    }

    static AnnotationBo unauthorized(int key, long value) {
        return new LongAnnotationBo(key, value, false);
    }

    static AnnotationBo unauthorized(int key, int value) {
        return new IntAnnotationBo(key, value, false);
    }


    int getKey();


    Object getValue();


    boolean isAuthorized();

}
