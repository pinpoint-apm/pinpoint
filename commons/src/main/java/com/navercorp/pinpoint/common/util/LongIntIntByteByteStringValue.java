/*
 * Copyright 2017 NAVER Corp.
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
 * @author jaehong.kim
 */
public class LongIntIntByteByteStringValue implements DataType {
    private final long longValue;
    private final int intValue1;
    private final int intValue2;
    private final byte byteValue1;
    private final byte byteValue2;
    private final String stringValue;


    public LongIntIntByteByteStringValue(final long longValue, final int intValue1, final int intValue2, final byte byteValue1, final byte byteValue2, final String stringValue) {
        this.longValue = longValue;
        this.intValue1 = intValue1;
        this.intValue2 = intValue2;
        this.byteValue1 = byteValue1;
        this.byteValue2 = byteValue2;
        this.stringValue = stringValue;
    }

    public long getLongValue() {
        return longValue;
    }

    public int getIntValue1() {
        return intValue1;
    }

    public int getIntValue2() {
        return intValue2;
    }

    public byte getByteValue1() {
        return byteValue1;
    }

    public byte getByteValue2() {
        return byteValue2;
    }

    public String getStringValue() {
        return stringValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LongIntIntByteByteStringValue that = (LongIntIntByteByteStringValue) o;

        if (longValue != that.longValue) return false;
        if (intValue1 != that.intValue1) return false;
        if (intValue2 != that.intValue2) return false;
        if (byteValue1 != that.byteValue1) return false;
        if (byteValue2 != that.byteValue2) return false;
        return stringValue != null ? stringValue.equals(that.stringValue) : that.stringValue == null;
    }

    @Override
    public int hashCode() {
        int result = (int) (longValue ^ (longValue >>> 32));
        result = 31 * result + intValue1;
        result = 31 * result + intValue2;
        result = 31 * result + (int) byteValue1;
        result = 31 * result + (int) byteValue2;
        result = 31 * result + (stringValue != null ? stringValue.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "LongIntIntByteByteStringValue{" +
                "longValue=" + longValue +
                ", intValue1=" + intValue1 +
                ", intValue2=" + intValue2 +
                ", byteValue1=" + byteValue1 +
                ", byteValue2=" + byteValue2 +
                ", stringValue='" + stringValue + '\'' +
                '}';
    }
}
