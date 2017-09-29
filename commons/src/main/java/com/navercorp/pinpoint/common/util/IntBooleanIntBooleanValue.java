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
public class IntBooleanIntBooleanValue {
    private final int intValue1;
    private final boolean booleanValue1;
    private final int intValue2;
    private final boolean booleanValue2;

    public IntBooleanIntBooleanValue(int intValue1, boolean booleanValue1, int intValue2, boolean booleanValue2) {
        this.intValue1 = intValue1;
        this.booleanValue1 = booleanValue1;
        this.intValue2 = intValue2;
        this.booleanValue2 = booleanValue2;
    }

    public int getIntValue1() {
        return intValue1;
    }

    public boolean isBooleanValue1() {
        return booleanValue1;
    }

    public int getIntValue2() {
        return intValue2;
    }

    public boolean isBooleanValue2() {
        return booleanValue2;
    }
}
