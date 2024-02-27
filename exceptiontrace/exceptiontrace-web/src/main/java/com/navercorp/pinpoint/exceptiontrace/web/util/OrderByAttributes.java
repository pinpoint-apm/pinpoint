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

import java.util.EnumSet;

/**
 * @author intr3p1d
 */
public enum OrderByAttributes {
    ERROR_CLASS_NAME("errorClassName", PinotColumns.ERROR_CLASS_NAME),
    ERROR_MESSAGE("errorMessage", PinotColumns.ERROR_MESSAGE),
    TIMESTAMP("timestamp", PinotColumns.TIMESTAMP),
    URI_TEMPLATE("path", PinotColumns.URI_TEMPLATE);

    private static final EnumGetter<OrderByAttributes> GETTER = new EnumGetter<>(OrderByAttributes.class);

    private final String name;
    private final PinotColumns column;

    OrderByAttributes(String name, PinotColumns column) {
        this.name = name;
        this.column = column;
    }

    public String getAttributeName() {
        return column.getName();
    }

    public String getName() {
        return name;
    }

    public static OrderByAttributes fromValue(String name) {
        return GETTER.fromValue(OrderByAttributes::getName, name);
    }
}
