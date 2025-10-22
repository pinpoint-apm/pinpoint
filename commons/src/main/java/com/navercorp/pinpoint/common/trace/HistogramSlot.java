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

/**
 * @author emeroad
 */
public class HistogramSlot {

    private final byte slotCode;
    private final short slotTime;
    private final SlotType slotType;
    private final String slotName;

    public HistogramSlot(byte slotCode, short slotTime, SlotType slotType, String slotName) {
        this.slotCode = slotCode;
        this.slotTime = slotTime;
        this.slotType = Objects.requireNonNull(slotType, "slotType");
        this.slotName = Objects.requireNonNull(slotName, "slotName");
    }

    public byte getSlotCode() {
        return slotCode;
    }

    public short getSlotTime() {
        return slotTime;
    }

    public SlotType getSlotType() {
        return slotType;
    }

    public String getSlotName() {
        return slotName;
    }

    @Override
    public String toString() {
        return "HistogramSlot{" +
               "slotCode=" + slotCode +
               "slotTime=" + slotTime +
               ", slotType=" + slotType +
               ", slotName='" + slotName + '\'' +
               '}';
    }
}
