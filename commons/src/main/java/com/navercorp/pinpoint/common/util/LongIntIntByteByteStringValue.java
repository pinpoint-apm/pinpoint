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
public class LongIntIntByteByteStringValue {
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
}
