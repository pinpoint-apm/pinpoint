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

import com.navercorp.pinpoint.common.util.DataType;
import com.navercorp.pinpoint.profiler.context.Annotation;

/**
 * @author emeroad
 */
public final class Annotations {
    public Annotations() {
    }

    public static Annotation<Boolean> of(int key, boolean value) {
        return new BooleanAnnotation(key, value);
    }

    public static Annotation<Byte> of(int key, byte value) {
        return new ByteAnnotation(key, value);
    }

    public static Annotation<Short> of(int key, short value) {
        return new ShortAnnotation(key, value);
    }

    public static Annotation<Integer> of(int key, int value) {
        return new IntAnnotation(key, value);
    }

    public static Annotation<Integer> of(int key, Integer value) {
        if (value == null) {
            return newNullAnnotation(key);
        }
        return new IntAnnotation(key, value);
    }

    public static Annotation<Long> of(int key, long value) {
        return new LongAnnotation(key, value);
    }

    public static Annotation<Long> of(int key, Long value) {
        if (value == null) {
            return newNullAnnotation(key);
        }
        return new LongAnnotation(key, value);
    }

    private static <T> Annotation<T> newNullAnnotation(int key) {
        return new NullAnnotation<>(key);
    }

    public static Annotation<Void> of(int key) {
        return newNullAnnotation(key);
    }

    public static Annotation<Double> of(int key, double value) {
        return new DoubleAnnotation(key, value);
    }

    public static Annotation<String> of(int key, String value) {
        if (value == null) {
            return newNullAnnotation(key);
        }
        return new StringAnnotation(key, value);
    }

    public static Annotation<DataType> of(int key, DataType value) {
        if (value == null) {
            return newNullAnnotation(key);
        }
        return new DataTypeAnnotation(key, value);
    }

    public static Annotation<byte[]> of(int key, byte[] value) {
        if (value == null) {
            return newNullAnnotation(key);
        }
        return new BytesAnnotation(key, value);
    }

    public static Annotation<?> of(int key, Object value) {
        if (value == null) {
            return newNullAnnotation(key);
        }
        if (value instanceof String) {
            return new StringAnnotation(key, (String) value);
        }
        if (value instanceof DataType) {
            return new DataTypeAnnotation(key, (DataType) value);
        }
        if (value instanceof Integer) {
            return new IntAnnotation(key, (Integer) value);
        }
        if (value instanceof Long) {
            return new LongAnnotation(key, (Long) value);
        }
        if (value instanceof Double) {
            return new DoubleAnnotation(key, (Double) value);
        }
        if (value instanceof Boolean) {
            return new BooleanAnnotation(key, (Boolean) value);
        }
        if (value instanceof byte[]) {
            return new BytesAnnotation(key, (byte[]) value);
        }
        if (value instanceof Byte) {
            return new ByteAnnotation(key, (byte) value);
        }

        if (value instanceof Float) {
            return new DoubleAnnotation(key, (Float) value);
        }
        if (value instanceof Short) {
            return new ShortAnnotation(key, (Short) value);
        }

        return new ObjectAnnotation(key, value);
    }


}
