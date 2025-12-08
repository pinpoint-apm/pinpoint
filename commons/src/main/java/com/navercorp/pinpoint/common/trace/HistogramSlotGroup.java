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

import java.util.Objects;

public class HistogramSlotGroup {
    private final HistogramSlot fast;
    private final HistogramSlot normal;
    private final HistogramSlot slow;
    private final HistogramSlot verySlow;


    public static HistogramSlot entry(SlotCode code, int slotTime, String slotName, SlotType slotType) {
        return new HistogramSlot(code, (short) slotTime, slotType, slotName);
    }

    public HistogramSlotGroup(HistogramSlot fast,
                              HistogramSlot normal,
                              HistogramSlot slow,
                              HistogramSlot verySlow) {
        this.fast = Objects.requireNonNull(fast, "fast");
        this.normal = Objects.requireNonNull(normal, "normal");
        this.slow = Objects.requireNonNull(slow, "slow");
        this.verySlow = Objects.requireNonNull(verySlow, "verySlow");
    }

    public HistogramSlot getFastSlot() {
        return this.fast;
    }

    public HistogramSlot getNormalSlot() {
        return this.normal;
    }

    public HistogramSlot getSlowSlot() {
        return this.slow;
    }

    public HistogramSlot getVerySlowSlot() {
        return this.verySlow;
    }

    @Override
    public String toString() {
        return "SlotGroup{" +
                "fast=" + fast +
                ", normal=" + normal +
                ", slow=" + slow +
                ", verySlow=" + verySlow +
                '}';
    }
}
