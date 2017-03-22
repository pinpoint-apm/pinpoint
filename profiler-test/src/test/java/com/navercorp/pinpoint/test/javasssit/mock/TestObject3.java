/*
 * Copyright 2014 NAVER Corp.
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
package com.navercorp.pinpoint.test.javasssit.mock;

/**
 * @author Jongho Moon
 *
 */
public class TestObject3 {
    private String value;
    private int intValue;
    private int[] intValues;
    private Integer[] integerValues;

    public void setValue(String value) {
        this.value = value;
    }
    
    public void setIntValue(int value) {
        this.intValue = value;
    }

    public void setIntValues(int[] values) {
        this.intValues = values;
    }

    public void setIntegerValues(Integer[] values) {
        this.integerValues = values;
    }

    @Override
    public String toString() {
        return value;
    }
}
