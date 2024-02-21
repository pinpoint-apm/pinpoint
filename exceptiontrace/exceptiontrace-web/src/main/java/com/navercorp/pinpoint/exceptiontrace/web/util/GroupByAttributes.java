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

import java.util.Arrays;

/**
 * @author intr3p1d
 */
public enum GroupByAttributes {
    URI_TEMPLATE("path", PinotColumns.URI_TEMPLATE),
    ERROR_MESSAGE_LOG_TYPE("errorMessage_logtype", PinotColumns.ERROR_MESSAGE_LOG_TYPE),
    ERROR_CLASS_NAME("errorClassName", PinotColumns.ERROR_CLASS_NAME),
    STACK_TRACE("stackTrace", PinotColumns.STACK_TRACE_HASH);

    private static final EnumGetter<GroupByAttributes> GETTER = new EnumGetter<>(GroupByAttributes.class);
    private final String name;
    private final PinotColumns representativeColumn;
    private final PinotColumns[] groupByColumns;

    GroupByAttributes(String name, PinotColumns... groupByColumns) {
        this.name = name;
        this.representativeColumn = groupByColumns[0];
        this.groupByColumns = groupByColumns;
    }

    public String getName() {
        return name;
    }

    public String getRepresentativeColumn() {
        return representativeColumn.getName();
    }

    public String[] getGroupByColumns() {
        return Arrays.stream(groupByColumns).map(PinotColumns::getName).toArray(String[]::new);
    }

    public static GroupByAttributes fromValue(String name) {
        return GETTER.fromValue(GroupByAttributes::getName, name);
    }
}
