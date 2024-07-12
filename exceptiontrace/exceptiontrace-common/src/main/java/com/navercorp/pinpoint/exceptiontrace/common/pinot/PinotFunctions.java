/*
 * Copyright 2024 NAVER Corp.
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
package com.navercorp.pinpoint.exceptiontrace.common.pinot;

/**
 * @author intr3p1d
 */
public enum PinotFunctions {
    ARRAY_SLICE_INT("arraySliceInt"),
    ARRAY_SLICE_STRING("arraySliceString");

    private final String name;

    PinotFunctions(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
