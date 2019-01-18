/*
 * Copyright 2018 NAVER Corp.
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
 * @author Roy Kim
 */
public class StringStringValue {
    private String stringValue1;
    private String stringValue2;

    public StringStringValue(String stringValue1, String stringValue2) {
        this.stringValue1 = stringValue1;
        this.stringValue2 = stringValue2;
    }

    public String getStringValue1() {
        return stringValue1;
    }

    public String getStringValue2() {
        return stringValue2;
    }

    @Override
    public String toString() {
        return "StringStringValue{" +
                "stringValue1='" + stringValue1 + '\'' +
                ", stringValue2='" + stringValue2 + '\'' +
                '}';
    }

}
