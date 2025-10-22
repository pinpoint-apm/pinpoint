/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.common.trace;

import com.navercorp.pinpoint.common.util.apache.IntHashMap;
import com.navercorp.pinpoint.common.util.apache.IntHashMapUtils;

import java.util.HashMap;
import java.util.Map;

public class HistogramSlotMapBuilder {

    private final Map<Integer, HistogramSlot> map;

    public HistogramSlotMapBuilder() {
        this.map = new HashMap<>();
    }

    public HistogramSlotMapBuilder addSlot(HistogramSlot slot) {
        HistogramSlot exist = map.put((int) slot.getSlotCode(), slot);
        if (exist != null) {
            throw new IllegalArgumentException("Duplicate slot:" + slot);
        }
        return this;
    }

    public HistogramSlotMapBuilder addSchema(HistogramSchema schema) {
        addSlot(schema.getFastSlot());
        addSlot(schema.getNormalSlot());
        addSlot(schema.getSlowSlot());
        addSlot(schema.getVerySlowSlot());
        return this;
    }

    public IntHashMap<HistogramSlot> build() {
        return IntHashMapUtils.copy(map);
    }
}
