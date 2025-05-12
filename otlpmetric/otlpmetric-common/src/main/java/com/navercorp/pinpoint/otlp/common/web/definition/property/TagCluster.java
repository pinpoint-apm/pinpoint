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

package com.navercorp.pinpoint.otlp.common.web.definition.property;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author minwoo-jung
 */
public class TagCluster {
    private static final Comparator<FieldAndUnit> FIELD_NAME_COMPARING = Comparator.comparing(FieldAndUnit::fieldName);

    private final String tagGroup;
    private final List<FieldAndUnit> fieldAndUnitList;

    public TagCluster(String tag) {
        this.tagGroup = tag;
        this.fieldAndUnitList = new ArrayList<>();
    }

    public void addFieldAndUnit(String fieldName, String unit) {
        FieldAndUnit fieldAndUnit = new FieldAndUnit(fieldName, unit);
        fieldAndUnitList.add(fieldAndUnit);
        fieldAndUnitList.sort(FIELD_NAME_COMPARING);
    }

    public List<FieldAndUnit> getFieldAndUnitList() {
        return fieldAndUnitList;
    }

    public String getTagGroup() {
        return tagGroup;
    }
}
