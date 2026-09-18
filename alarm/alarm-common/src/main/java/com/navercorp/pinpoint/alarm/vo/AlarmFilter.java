/*
 * Copyright 2026 NAVER Corp.
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
package com.navercorp.pinpoint.alarm.vo;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.navercorp.pinpoint.alarm.validation.AlarmValidationConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class AlarmFilter {

    /**
     * Filter match operator, serialized as its name.
     */
    public enum Op {
        EQ, NEQ, CONTAINS, NOT_CONTAINS
    }

    @NotBlank(message = "filter key must not be blank")
    private String key;

    @NotNull(message = "filter op must not be null")
    private Op op;

    @NotBlank(message = "filter value must not be blank")
    @Size(max = AlarmValidationConstants.MAX_FILTER_VALUE_LENGTH, message = "filter value is too long")
    private String value;

    public AlarmFilter() {
    }

    public AlarmFilter(String key, Op op, String value) {
        this.key = key;
        this.op = op;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Op getOp() {
        return op;
    }

    public void setOp(Op op) {
        this.op = op;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @JsonAnySetter
    public void rejectUnknown(String fieldName, Object value) {
        throw new IllegalArgumentException("Unknown alarm filter field: " + fieldName);
    }
}
