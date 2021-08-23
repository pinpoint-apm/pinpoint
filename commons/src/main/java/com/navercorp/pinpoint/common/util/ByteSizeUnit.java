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


import java.util.EnumSet;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
public enum ByteSizeUnit {

    BYTES(new String[]{"B", "b"}, ByteSizeUnit.BYTES_SIZE),
    KILO_BYTES(new String[]{"K", "k", "KB"}, ByteSizeUnit.KILO_SIZE),
    MEGA_BYTES(new String[]{"M", "m", "MB"}, ByteSizeUnit.MEGA_SIZE),
    GIGA_BYTES(new String[]{"G", "g", "GB"}, ByteSizeUnit.GIGA_SIZE),
    TERA_BYTES(new String[]{"T", "t", "TB"}, ByteSizeUnit.TERA_SIZE);

    private static final EnumSet<ByteSizeUnit> ALL = EnumSet.allOf(ByteSizeUnit.class);

    private static final long BYTES_SIZE = 1;
    private static final long KILO_SIZE = 1024;
    private static final long MEGA_SIZE = KILO_SIZE * KILO_SIZE;
    private static final long GIGA_SIZE = MEGA_SIZE * KILO_SIZE;
    private static final long TERA_SIZE = GIGA_SIZE * KILO_SIZE;


    private final String[] units;
    private final long unitSize;
    private final long maxSize;
    private final long intMaxSize;

    ByteSizeUnit(String[] units, long unitSize) {
        this.units = Objects.requireNonNull(units, "units");
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

    String getUnitChar1() {
        return units[0];
    }

    String getUnitChar2() {
        return units[1];
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
        } catch (Exception ignore) {
        }
        return defaultValue;
    }

    public static long getByteSize(String value) {
        if (StringUtils.isEmpty(value)) {
            throw new IllegalArgumentException("size must not be empty");
        }

        if (endWithDataUnit(BYTES.units, value)) {
            return getByteSize0(value.substring(0, value.length() - 1));
        } else {
            return getByteSize0(value);
        }
    }

    private static boolean endWithDataUnit(String[] units, String value) {
        for (String unit : units) {
            if (value.endsWith(unit)) {
                return true;
            }
        }
        return false;
    }

    private static long getByteSize0(String value) {
        if (StringUtils.isEmpty(value)) {
            throw new IllegalArgumentException("size must not be empty");
        }

        for (ByteSizeUnit byteSizeUnit : ALL) {
            if (byteSizeUnit == ByteSizeUnit.BYTES) {
                continue;
            }

            if (endWithDataUnit(byteSizeUnit.units, value)) {
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
