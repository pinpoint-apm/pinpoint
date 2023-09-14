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
package com.navercorp.pinpoint.exceptiontrace.web.util;

import com.navercorp.pinpoint.common.server.util.EnumGetter;
import com.navercorp.pinpoint.exceptiontrace.common.pinot.PinotColumns;

/**
 * @author intr3p1d
 */
public enum GroupByAttributes {
    ERROR_MESSAGE("errorMessage", PinotColumns.ERROR_MESSAGE),
    TIMESTMAP("timestamp", PinotColumns.TIMESTAMP),
    STACK_TRACE("stackTrace", PinotColumns.STACK_TRACE_HASH),
    URI_TEMPLATE("uriTemplate", PinotColumns.URI_TEMPLATE);

    private static final EnumGetter<GroupByAttributes> GETTER = new EnumGetter<>(GroupByAttributes.class);
    private final String name;
    private final PinotColumns column;

    GroupByAttributes(String name, PinotColumns column) {
        this.name = name;
        this.column = column;
    }

    public String getName() {
        return name;
    }

    public String getAttributeName() {
        return column.getName();
    }

    public static GroupByAttributes fromValue(String name) {
        return GETTER.fromValue(GroupByAttributes::getName, name);
    }
}
