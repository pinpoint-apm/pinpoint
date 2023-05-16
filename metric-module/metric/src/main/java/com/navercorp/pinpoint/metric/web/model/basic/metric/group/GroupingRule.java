/*
 * Copyright 2021 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.metric.web.model.basic.metric.group;

import java.util.Objects;

/**
 * @author minwoo.jung
 */
public enum GroupingRule {
    TAG(1, "tag"),
    UNKNOWN(100, "unknown");

    private final int code;
    private final String value;

    GroupingRule(int code, String value) {
        this.code = code;
        this.value = Objects.requireNonNull(value, "value");
    }

    public static GroupingRule getByCode(int code) {
        for (GroupingRule groupingRule : GroupingRule.values()) {
            if (groupingRule.code == code) {
                return groupingRule;
            }
        }
        throw new IllegalArgumentException("Unknown code : " + code);
    }

    public static GroupingRule getByValue(String value) {
        for (GroupingRule groupingRule : GroupingRule.values()) {
            if (groupingRule.value.equalsIgnoreCase(value)) {
                return groupingRule;
            }
        }
        throw new IllegalArgumentException("Unknown value : " + value);
    }
}
