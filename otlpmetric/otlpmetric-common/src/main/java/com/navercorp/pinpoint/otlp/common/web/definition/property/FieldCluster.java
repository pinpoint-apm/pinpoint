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
public class FieldCluster {

    private final String fieldName;
    private final String unit;
    private final List<String> tagGroupList;

    public FieldCluster(String fieldName, String unit) {
        this.fieldName = fieldName;
        this.unit = unit;
        this.tagGroupList = new ArrayList<>();
    }

    public String getUnit() {
        return unit;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void addTagGroup(String tagGroup) {
        tagGroupList.add(tagGroup);
        tagGroupList.sort(Comparator.naturalOrder());
    }

    public List<String> getTagGroupList() {
        return tagGroupList;
    }
}
