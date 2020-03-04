/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.common.util;


/**
 * @author Taejin Koo
 */
public enum ByteSizeUnit {

    BYTES('B', 'b', ByteSizeUnit.BYTES_SIZE),
    KILO_BYTES('K', 'k', ByteSizeUnit.KILO_SIZE),
    MEGA_BYTES('M', 'm', ByteSizeUnit.MEGA_SIZE),
    GIGA_BYTES('G', 'g', ByteSizeUnit.GIGA_SIZE),
    TERA_BYTES('T', 't', ByteSizeUnit.TERA_SIZE);

    private static final long BYTES_SIZE = 1;
    private static final long KILO_SIZE = 1024;
    private static final long MEGA_SIZE = KILO_SIZE * KILO_SIZE;
    private static final long GIGA_SIZE = MEGA_SIZE * KILO_SIZE;
    private static final long TERA_SIZE = GIGA_SIZE * KILO_SIZE;


    private final char unitChar1;
    private final char unitChar2;
    private final long unitSize;
    private final long maxSize;
    private final long intMaxSize;

    ByteSizeUnit(char unitChar1, char unitChar2, long unitSize) {
        this.unitChar1 = unitChar1;
        this.unitChar2 = unitChar2;
        this.unitSize = unitSize;
        this.maxSize = Long.MAX_VALUE / unitSize;
        this.intMaxSize = Integer.MAX_VALUE / unitSize;
    }

    public long toBytesSize(long value) {
        if (value < 0) {
            throw new IllegalArgumentException("value < 0");
        }
        if (value > maxSize) {
            throw new IllegalArgumentException("value > " + maxSize);
        }

        return value * unitSize;
    }

    public int toBytesSizeAsInt(long value) {
        if (value < 0) {
            throw new IllegalArgumentException("value < 0");
        }
        if (value > intMaxSize) {
            throw new IllegalArgumentException("value > " + intMaxSize);
        }

        return (int) (value * unitSize);
    }

    char getUnitChar1() {
        return unitChar1;
    }

    char getUnitChar2() {
        return unitChar2;
    }

    public long getUnitSize() {
        return unitSize;
    }

    public long getMaxSize() {
        return maxSize;
    }


    public static long getByteSize(String value, long defaultValue) {
        try {
            return getByteSize(value);
        } catch (Exception e) {
        }
        return defaultValue;
    }

    public static long getByteSize(String value) {
        if (StringUtils.isEmpty(value)) {
            throw new IllegalArgumentException("size must not be empty");
        }

        final char sizeUnitChar = value.charAt(value.length() - 1);
        if (sizeUnitChar == BYTES.unitChar1 || sizeUnitChar == BYTES.unitChar2) {
            return getByteSize0(value.substring(0, value.length() - 1));
        } else {
            return getByteSize0(value);
        }
    }

    private static long getByteSize0(String value) {
        if (StringUtils.isEmpty(value)) {
            throw new IllegalArgumentException("size must not be empty");
        }

        final char sizeUnitChar = value.charAt(value.length() - 1);
        for (ByteSizeUnit byteSizeUnit : values()) {
            if (byteSizeUnit == ByteSizeUnit.BYTES) {
                continue;
            }

            if (sizeUnitChar == byteSizeUnit.unitChar1 || sizeUnitChar == byteSizeUnit.unitChar2) {
                long numberValue = getLong(value.substring(0, value.length() - 1));
                return byteSizeUnit.toBytesSize(numberValue);
            }
        }

        long numberValue = getLong(value);
        return ByteSizeUnit.BYTES.toBytesSize(numberValue);
    }

    private static long getLong(String value) {
        try {
            return Long.parseLong(value, 10);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

}
