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

import static com.navercorp.pinpoint.common.trace.HistogramSlotGroup.entry;

public final class HistogramSchemas {

    private HistogramSchemas() {
    }

    private static final short VERY_SLOW_SLOT_TIME = 0;
    // All negative numbers are included in error count
    private static final short ERROR_SLOT_TIME = -1;
    // Do not use negative numbers.
    private static final short PING_SLOT_TIME = Short.MAX_VALUE - 1;
    private static final short STAT_SLOT_TIME_TOTAL = Short.MAX_VALUE - 2;
    private static final short STAT_SLOT_TIME_MAX = Short.MAX_VALUE - 3;


    @Deprecated
    public static final HistogramSlot ERROR_SLOT = new HistogramSlot((byte) -128, ERROR_SLOT_TIME, SlotType.ERROR, "Error");

    public static final HistogramSlot SUM_STAT_SLOT = new HistogramSlot((byte) 112, STAT_SLOT_TIME_TOTAL, SlotType.SUM_STAT, "SumTime");
    public static final HistogramSlot MAX_STAT_SLOT = new HistogramSlot((byte) 114, STAT_SLOT_TIME_MAX, SlotType.MAX_STAT, "Max");
    public static final HistogramSlot PING_SLOT = new HistogramSlot((byte) 126, PING_SLOT_TIME, SlotType.PING, "Ping");


    public static final HistogramSchema FAST_SCHEMA = new BaseHistogramSchema(Schema.FAST,
            new HistogramSlotGroup(
                    entry((byte) 1, 100, "100ms", SlotType.FAST),
                    entry((byte) 2, 300, "300ms", SlotType.NORMAL),
                    entry((byte) 3, 500, "500ms", SlotType.SLOW),
                    entry((byte) 4, VERY_SLOW_SLOT_TIME, "Slow", SlotType.VERY_SLOW)),
            new HistogramSlotGroup(
                    entry((byte) -1, -100, "100ms", SlotType.FAST_ERROR),
                    entry((byte) -2, -300, "300ms", SlotType.NORMAL_ERROR),
                    entry((byte) -3, -500, "500ms", SlotType.SLOW_ERROR),
                    entry((byte) -4, -999, "Slow", SlotType.VERY_SLOW_ERROR)),
            ERROR_SLOT, SUM_STAT_SLOT, MAX_STAT_SLOT, PING_SLOT
    );

    public static final HistogramSchema NORMAL_SCHEMA = new BaseHistogramSchema(Schema.NORMAL,
            new HistogramSlotGroup(
                    entry((byte) 11, 1000, "1s", SlotType.FAST),
                    entry((byte) 12, 3000, "3s", SlotType.NORMAL),
                    entry((byte) 13, 5000, "5s", SlotType.SLOW),
                    entry((byte) 14, VERY_SLOW_SLOT_TIME, "Slow", SlotType.VERY_SLOW)),
            new HistogramSlotGroup(
                    entry((byte) -11, -1000, "1s", SlotType.FAST_ERROR),
                    entry((byte) -12, -3000, "3s", SlotType.NORMAL_ERROR),
                    entry((byte) -13, -5000, "5s", SlotType.SLOW_ERROR),
                    entry((byte) -14, -9999, "Slow", SlotType.VERY_SLOW_ERROR)),
            ERROR_SLOT, SUM_STAT_SLOT, MAX_STAT_SLOT, PING_SLOT
    );

    private static final IntHashMap<HistogramSlot> SLOT_MAP = toSlotCodeMap(FAST_SCHEMA, NORMAL_SCHEMA);


    private static IntHashMap<HistogramSlot> toSlotCodeMap(HistogramSchema... schemas) {
        HistogramSlotMapBuilder builder = new HistogramSlotMapBuilder();
        for (HistogramSchema schema : schemas) {
            builder.addSchema(schema);
        }
        builder.addSlot(SUM_STAT_SLOT);
        builder.addSlot(MAX_STAT_SLOT);
        builder.addSlot(PING_SLOT);
        builder.addSlot(ERROR_SLOT);

        return builder.build();
    }

    public static HistogramSlot getSlotFromCode(int code) {
        return SLOT_MAP.get(code);
    }

    static class HistogramSlotMapBuilder {

        private final IntHashMap<HistogramSlot> map = new IntHashMap<>();

        public HistogramSlotMapBuilder() {
        }

        public HistogramSlotMapBuilder addSlot(HistogramSlot slot) {
            Object exist = map.put((int) slot.getSlotCode(), slot);
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
            return map;
        }
    }

}
