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

import static com.navercorp.pinpoint.common.trace.HistogramSlotGroup.entry;
import static com.navercorp.pinpoint.common.trace.SlotCode.ERROR;
import static com.navercorp.pinpoint.common.trace.SlotCode.F_FAST;
import static com.navercorp.pinpoint.common.trace.SlotCode.F_FAST_ERROR;
import static com.navercorp.pinpoint.common.trace.SlotCode.F_NORMAL;
import static com.navercorp.pinpoint.common.trace.SlotCode.F_NORMAL_ERROR;
import static com.navercorp.pinpoint.common.trace.SlotCode.F_SLOW;
import static com.navercorp.pinpoint.common.trace.SlotCode.F_SLOW_ERROR;
import static com.navercorp.pinpoint.common.trace.SlotCode.F_VERY_SLOW;
import static com.navercorp.pinpoint.common.trace.SlotCode.F_VERY_SLOW_ERROR;
import static com.navercorp.pinpoint.common.trace.SlotCode.MAX_STAT;
import static com.navercorp.pinpoint.common.trace.SlotCode.N_FAST;
import static com.navercorp.pinpoint.common.trace.SlotCode.N_FAST_ERROR;
import static com.navercorp.pinpoint.common.trace.SlotCode.N_NORMAL;
import static com.navercorp.pinpoint.common.trace.SlotCode.N_NORMAL_ERROR;
import static com.navercorp.pinpoint.common.trace.SlotCode.N_SLOW;
import static com.navercorp.pinpoint.common.trace.SlotCode.N_SLOW_ERROR;
import static com.navercorp.pinpoint.common.trace.SlotCode.N_VERY_SLOW;
import static com.navercorp.pinpoint.common.trace.SlotCode.N_VERY_SLOW_ERROR;
import static com.navercorp.pinpoint.common.trace.SlotCode.PING;
import static com.navercorp.pinpoint.common.trace.SlotCode.SUM_STAT;

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
    public static final HistogramSlot ERROR_SLOT = new HistogramSlot(ERROR, ERROR_SLOT_TIME, SlotType.ERROR, "Error");

    public static final HistogramSlot SUM_STAT_SLOT = new HistogramSlot(SUM_STAT, STAT_SLOT_TIME_TOTAL, SlotType.SUM_STAT, "SumTime");
    public static final HistogramSlot MAX_STAT_SLOT = new HistogramSlot(MAX_STAT, STAT_SLOT_TIME_MAX, SlotType.MAX_STAT, "Max");
    public static final HistogramSlot PING_SLOT = new HistogramSlot(PING, PING_SLOT_TIME, SlotType.PING, "Ping");



    public static final HistogramSchema FAST_SCHEMA = new BaseHistogramSchema(Schema.FAST,
            new HistogramSlotGroup(
                    entry(F_FAST, 100, "100ms", SlotType.FAST),
                    entry(F_NORMAL, 300, "300ms", SlotType.NORMAL),
                    entry(F_SLOW, 500, "500ms", SlotType.SLOW),
                    entry(F_VERY_SLOW, VERY_SLOW_SLOT_TIME, "Slow", SlotType.VERY_SLOW)),
            new HistogramSlotGroup(
                    entry(F_FAST_ERROR, -100, "100ms", SlotType.FAST_ERROR),
                    entry(F_NORMAL_ERROR, -300, "300ms", SlotType.NORMAL_ERROR),
                    entry(F_SLOW_ERROR, -500, "500ms", SlotType.SLOW_ERROR),
                    entry(F_VERY_SLOW_ERROR, -999, "Slow", SlotType.VERY_SLOW_ERROR)),
            ERROR_SLOT, SUM_STAT_SLOT, MAX_STAT_SLOT, PING_SLOT
    );

    public static final HistogramSchema NORMAL_SCHEMA = new BaseHistogramSchema(Schema.NORMAL,
            new HistogramSlotGroup(
                    entry(N_FAST, 1000, "1s", SlotType.FAST),
                    entry(N_NORMAL, 3000, "3s", SlotType.NORMAL),
                    entry(N_SLOW, 5000, "5s", SlotType.SLOW),
                    entry(N_VERY_SLOW, VERY_SLOW_SLOT_TIME, "Slow", SlotType.VERY_SLOW)),
            new HistogramSlotGroup(
                    entry(N_FAST_ERROR, -1000, "1s", SlotType.FAST_ERROR),
                    entry(N_NORMAL_ERROR, -3000, "3s", SlotType.NORMAL_ERROR),
                    entry(N_SLOW_ERROR, -5000, "5s", SlotType.SLOW_ERROR),
                    entry(N_VERY_SLOW_ERROR, -9999, "Slow", SlotType.VERY_SLOW_ERROR)),
            ERROR_SLOT, SUM_STAT_SLOT, MAX_STAT_SLOT, PING_SLOT
    );

}
