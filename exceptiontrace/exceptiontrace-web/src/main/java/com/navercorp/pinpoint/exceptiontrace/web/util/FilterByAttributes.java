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

import java.util.EnumMap;
import java.util.Map;

/**
 * @author intr3p1d
 */
public class FilterByAttributes {

    public enum FilterByColumn {
        URI_TEMPLATE(PinotColumns.URI_TEMPLATE),
        ERROR_MESSAGE_LOG_TYPE(PinotColumns.ERROR_MESSAGE_LOG_TYPE),
        ERROR_CLASS_NAME(PinotColumns.ERROR_CLASS_NAME),
        STACK_TRACE(PinotColumns.STACK_TRACE_HASH);

        private static final EnumGetter<FilterByColumn> GETTER = new EnumGetter<>(FilterByColumn.class);

        private final PinotColumns column;

        FilterByColumn(PinotColumns column) {
            this.column = column;
        }

        public PinotColumns getColumn() {
            return column;
        }

        public static FilterByColumn fromValue(String column) {
            return GETTER.fromValue((FilterByColumn x) -> x.getColumn().getName(), column);
        }
    }


    private final Map<FilterByColumn, String> map = new EnumMap<>(FilterByColumn.class);

    public void put(String column, String value) {
        this.map.put(FilterByColumn.fromValue(column), value);
    }

    public Map<FilterByColumn, String> getMap() {
        return map;
    }

    @Override
    public String toString() {
        return "FilterByAttributes{" +
                "map=" + map +
                '}';
    }
}
