/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.common.trace;

/**
 * @author emeroad
 */
public class HistogramSlot {

    private final short slotTime;
    private final SlotType slotType;
    private final String slotName;

    public HistogramSlot(short slotTime, SlotType slotType, String slotName) {
        if (slotType == null) {
            throw new NullPointerException("slotType must not be null");
        }
        if (slotName == null) {
            throw new NullPointerException("slotName must not be null");
        }
        this.slotTime = slotTime;
        this.slotType = slotType;
        this.slotName = slotName;
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
        final StringBuilder sb = new StringBuilder("HistogramSlot{");
        sb.append("slotTime=").append(slotTime);
        sb.append(", slotType=").append(slotType);
        sb.append(", slotName='").append(slotName).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
