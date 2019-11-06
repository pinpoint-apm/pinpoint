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

package com.navercorp.pinpoint.bootstrap.plugin.test;

/**
 * @author Taejin Koo
 */
public class ExpectedTraceField {

    public static final ExpectedTraceField ALWAYS_TRUE = new ExpectedTraceField(ExpectedTraceFieldType.ALWAYS_TRUE);
    public static final ExpectedTraceField EMPTY = new ExpectedTraceField(ExpectedTraceFieldType.EMPTY);
    public static final ExpectedTraceField NOT_EMPTY = new ExpectedTraceField(ExpectedTraceFieldType.NOT_EMPTY);

    private final String expected;
    private final ExpectedTraceFieldType expectedType;

    public ExpectedTraceField(ExpectedTraceFieldType expectedType) {
        this(null, expectedType);
    }

    public ExpectedTraceField(String expected, ExpectedTraceFieldType expectedType) {
        if (expectedType == null) {
            throw new NullPointerException("expectedType");
        }
        this.expected = expected;
        this.expectedType = expectedType;
    }

    public static ExpectedTraceField create(String value) {
        if (value == null) {
            return createAlwaysTrue();
        } else {
            return createEquals(value);
        }
    }

    public static ExpectedTraceField createEquals(String value) {
        return new ExpectedTraceField(value, ExpectedTraceFieldType.EQUALS);
    }

    public static ExpectedTraceField createAlwaysTrue() {
        return ALWAYS_TRUE;
    }

    public static ExpectedTraceField createNotEmpty() {
        return NOT_EMPTY;
    }

    public static ExpectedTraceField createEmpty() {
        return EMPTY;
    }

    public static ExpectedTraceField createStartWith(String value) {
        return new ExpectedTraceField(value, ExpectedTraceFieldType.START_WITH);
    }

    public static ExpectedTraceField createContains(String value) {
        return new ExpectedTraceField(value, ExpectedTraceFieldType.CONTAINS);
    }

    public boolean isEquals(String value) {
        return expectedType.isEquals(expected, value);
    }

    @Override
    public String toString() {
        return "ExpectedTraceField{" +
                "expectedType=" + expectedType +
                ", expected='" + expected + '\'' +
                '}';
    }
}
