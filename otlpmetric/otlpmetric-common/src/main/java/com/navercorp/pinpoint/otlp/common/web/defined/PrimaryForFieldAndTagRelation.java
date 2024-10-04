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

package com.navercorp.pinpoint.otlp.common.web.defined;

import com.navercorp.pinpoint.common.server.util.EnumGetter;
import com.navercorp.pinpoint.otlp.common.web.definition.property.ChartType;

import java.util.EnumSet;

/**
 * @author minwoo-jung
 */
public enum PrimaryForFieldAndTagRelation {

    TAG(1, "tag"),
    FIELD(2, "field");

    private final int code;
    private final String name;

    PrimaryForFieldAndTagRelation(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    private static final EnumSet<PrimaryForFieldAndTagRelation> ENUM_SET = EnumSet.allOf(PrimaryForFieldAndTagRelation.class);
    private static final EnumGetter<PrimaryForFieldAndTagRelation> GETTER = new EnumGetter<>(ENUM_SET);

    public static PrimaryForFieldAndTagRelation fromName(String name) {
        return GETTER.fromValueIgnoreCase(PrimaryForFieldAndTagRelation::getName, name);
    }
}
