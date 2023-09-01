/*
 * Copyright 2023 NAVER Corp.
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
package com.navercorp.pinpoint.exceptiontrace.web.model;

import com.navercorp.pinpoint.exceptiontrace.common.pinot.PinotColumns;

/**
 * @author intr3p1d
 */
public enum GroupByAttributes {
    URI_TEMPLATE(PinotColumns.URI_TEMPLATE),
    ERROR_CLASS_NAME(PinotColumns.ERROR_CLASS_NAME),
    ERROR_MESSAGE(PinotColumns.ERROR_MESSAGE),
    STACK_TRACE(PinotColumns.STACK_TRACE_HASH);

    private final PinotColumns column;

    GroupByAttributes(PinotColumns column) {
        this.column = column;
    }

    public String getAttributeName() {
        return column.getName();
    }
}
