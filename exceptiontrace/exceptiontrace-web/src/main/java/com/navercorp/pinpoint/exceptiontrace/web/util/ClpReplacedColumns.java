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
package com.navercorp.pinpoint.exceptiontrace.web.util;

import com.navercorp.pinpoint.common.server.util.EnumGetter;
import com.navercorp.pinpoint.exceptiontrace.common.pinot.PinotColumns;
import com.navercorp.pinpoint.exceptiontrace.common.pinot.PinotFunctions;

/**
 * @author intr3p1d
 */
public enum ClpReplacedColumns {

    ERROR_MESSAGE_ENCODED_VARS("non-dict", PinotColumns.ERROR_MESSAGE_ENCODED_VARS, PinotFunctions.ARRAY_SLICE_INT),
    ERROR_MESSAGE_DICTIONARY_VARS("dict", PinotColumns.ERROR_MESSAGE_DICTIONARY_VARS, PinotFunctions.ARRAY_SLICE_STRING);

    private static final EnumGetter<ClpReplacedColumns> GETTER = new EnumGetter<>(ClpReplacedColumns.class);

    private final String name;
    private final PinotColumns columns;
    private final PinotFunctions sliceFunction;

    ClpReplacedColumns(String name, PinotColumns columns, PinotFunctions sliceFunction) {
        this.name = name;
        this.columns = columns;
        this.sliceFunction = sliceFunction;
    }

    public String getName() {
        return name;
    }

    public PinotColumns getColumns() {
        return columns;
    }

    public PinotFunctions getSliceFunction() {
        return sliceFunction;
    }

    public static ClpReplacedColumns fromValue(String name) {
        return GETTER.fromValue(ClpReplacedColumns::getName, name);
    }
}
