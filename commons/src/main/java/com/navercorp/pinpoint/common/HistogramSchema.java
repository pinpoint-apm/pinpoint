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

package com.navercorp.pinpoint.common;

/**
 * @author emeroad
 */
public class HistogramSchema {

    public static final short VERY_SLOW_SLOT_TIME = 0;

    public static final short ERROR_SLOT_TIME = -1;

    public static final HistogramSchema FAST_SCHEMA;
    public static final HistogramSchema NORMAL_SCHEMA;

    static {
        FAST_SCHEMA = new HistogramSchema(1, (short)100, "100ms", (short)300, "300ms", (short)500, "500ms", "Slow", "Error");

        NORMAL_SCHEMA = new HistogramSchema(2, (short)1000, "1s",  (short)3000, "3s", (short)5000, "5s", "Slow", "Error");
    }

    private final int typeCode;

    private final HistogramSlot fastSlot;
    private final HistogramSlot normalSlot;
    private final HistogramSlot slowSlot;
    private final HistogramSlot verySlowSlot;
    private final HistogramSlot errorSlot;

    // Should use the reference of FAST_SCHEMA, NORMAL created internally
    private HistogramSchema(int typeCode, short fast, String fastName, short normal, String normalName, short slow, String slowName, String verySlowName, String errorName) {
        this.typeCode = typeCode;
        this.fastSlot = new HistogramSlot(fast, SlotType.FAST, fastName);
        this.normalSlot = new HistogramSlot(normal, SlotType.NORMAL, normalName);
        this.slowSlot = new HistogramSlot(slow, SlotType.SLOW, slowName);
        this.verySlowSlot = new HistogramSlot(VERY_SLOW_SLOT_TIME, SlotType.VERY_SLOW, verySlowName);
        this.errorSlot = new HistogramSlot(ERROR_SLOT_TIME, SlotType.ERROR, errorName);
    }

    public int getTypeCode() {
        return typeCode;
    }

    /**
     * find the most appropriate slot based on elapsedTime
     * @param elapsedTime
     * @return
     */
    public HistogramSlot findHistogramSlot(int elapsedTime) {
        if (elapsedTime == ERROR_SLOT_TIME) {
            return errorSlot;
        }
        if (elapsedTime <= this.fastSlot.getSlotTime()) {
            return fastSlot;
        }
        if (elapsedTime <= this.normalSlot.getSlotTime()) {
            return normalSlot;
        }
        if (elapsedTime <= this.slowSlot.getSlotTime()) {
            return slowSlot;
        }
        return verySlowSlot;
    }

    public HistogramSlot getHistogramSlot(final short slotTime) {
        if (slotTime == ERROR_SLOT_TIME) {
            return errorSlot;
        }
        if (slotTime == this.fastSlot.getSlotTime()) {
            return fastSlot;
        }
        if (slotTime == this.normalSlot.getSlotTime()) {
            return normalSlot;
        }
        if (slotTime == this.slowSlot.getSlotTime()) {
            return slowSlot;
        }
        if (slotTime == this.verySlowSlot.getSlotTime()) {
            return slowSlot;
        }
        throw new IllegalArgumentException("HistogramSlot not found. slotTime:" + slotTime);
    }

    public HistogramSlot getFastSlot() {
        return fastSlot;
    }

    public HistogramSlot getNormalSlot() {
        return normalSlot;
    }

    public HistogramSlot getSlowSlot() {
        return slowSlot;
    }

    public HistogramSlot getVerySlowSlot() {
        return verySlowSlot;
    }

    public HistogramSlot getErrorSlot() {
        return errorSlot;
    }

    @Overri    e
	public int hashCod       () {
		final int        rime = 31;
       	int result = 1;
		result = pri       e * result         typeCo    e;
		return result;
	}

	@Overrid
	public boo          ean eq       als(Object o          j) {
		       f (this == obj)
			return tru          ;
		if        obj == null)
			return false;
		if (getCla       s() != obj.getClass())
			r          turn fa       se;
		Hi    togramSchema other = (HistogramSchema) obj;
		if (typeCode != other.typeCode)
			return false;
		return true;
	}

    @Override
    public String toString() {
        return "HistogramSchema{" +
                "typeCode=" + typeCode +
                ", fastSlot=" + fastSlot +
                ", normalSlot=" + normalSlot +
                ", slowSlot=" + slowSlot +
                ", verySlowSlot=" + verySlowSlot +
                ", errorSlot=" + errorSlot +
                '}';
    }
}
