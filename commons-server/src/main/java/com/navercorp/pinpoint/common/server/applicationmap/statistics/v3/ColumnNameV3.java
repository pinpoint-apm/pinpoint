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

package com.navercorp.pinpoint.common.server.applicationmap.statistics.v3;

import com.navercorp.pinpoint.common.server.applicationmap.statistics.ColumnName;
import com.navercorp.pinpoint.common.trace.SlotCode;


/**
 * @author emeroad
 */
public class ColumnNameV3 implements ColumnName {

    private static final ColumnNameV3 F_NORMAL = new ColumnNameV3(SlotCode.F_NORMAL.code());
    private static final ColumnNameV3 F_FAST = new ColumnNameV3(SlotCode.F_FAST.code());
    private static final ColumnNameV3 F_SLOW = new ColumnNameV3(SlotCode.F_SLOW.code());
    private static final ColumnNameV3 F_VERY_SLOW = new ColumnNameV3(SlotCode.F_VERY_SLOW.code());

    private static final ColumnNameV3 F_FAST_ERROR = new ColumnNameV3(SlotCode.F_FAST_ERROR.code());
    private static final ColumnNameV3 F_NORMAL_ERROR = new ColumnNameV3(SlotCode.F_NORMAL_ERROR.code());
    private static final ColumnNameV3 F_SLOW_ERROR = new ColumnNameV3(SlotCode.F_SLOW_ERROR.code());
    private static final ColumnNameV3 F_VERY_SLOW_ERROR = new ColumnNameV3(SlotCode.F_VERY_SLOW_ERROR.code());

    private static final ColumnNameV3 N_FAST = new ColumnNameV3(SlotCode.N_FAST.code());
    private static final ColumnNameV3 N_NORMAL = new ColumnNameV3(SlotCode.N_NORMAL.code());
    private static final ColumnNameV3 N_SLOW = new ColumnNameV3(SlotCode.N_SLOW.code());
    private static final ColumnNameV3 N_VERY_SLOW = new ColumnNameV3(SlotCode.N_VERY_SLOW.code());

    private static final ColumnNameV3 N_FAST_ERROR = new ColumnNameV3(SlotCode.N_FAST_ERROR.code());
    private static final ColumnNameV3 N_NORMAL_ERROR = new ColumnNameV3(SlotCode.N_NORMAL_ERROR.code());
    private static final ColumnNameV3 N_SLOW_ERROR = new ColumnNameV3(SlotCode.N_SLOW_ERROR.code());
    private static final ColumnNameV3 N_VERY_SLOW_ERROR = new ColumnNameV3(SlotCode.N_VERY_SLOW_ERROR.code());

    private static final ColumnNameV3 ERROR = new ColumnNameV3(SlotCode.ERROR.code());
    private static final ColumnNameV3 SUM_STAT = new ColumnNameV3(SlotCode.SUM_STAT.code());
    private static final ColumnNameV3 MAX_STAT = new ColumnNameV3(SlotCode.MAX_STAT.code());
    private static final ColumnNameV3 PING = new ColumnNameV3(SlotCode.PING.code());

    private final byte slotCode;

    public static ColumnName histogram(SlotCode slotCode) {
        return valueOf(slotCode);
    }

    private static ColumnNameV3 valueOf(SlotCode slotCode) {
        return switch (slotCode) {
            case F_NORMAL -> F_NORMAL;
            case F_FAST -> F_FAST;
            case F_SLOW -> F_SLOW;
            case F_VERY_SLOW -> F_VERY_SLOW;
            case F_FAST_ERROR -> F_FAST_ERROR;
            case F_NORMAL_ERROR -> F_NORMAL_ERROR;
            case F_SLOW_ERROR -> F_SLOW_ERROR;
            case F_VERY_SLOW_ERROR -> F_VERY_SLOW_ERROR;
            case N_FAST -> N_FAST;
            case N_NORMAL -> N_NORMAL;
            case N_SLOW -> N_SLOW;
            case N_VERY_SLOW -> N_VERY_SLOW;
            case N_FAST_ERROR -> N_FAST_ERROR;
            case N_NORMAL_ERROR -> N_NORMAL_ERROR;
            case N_SLOW_ERROR -> N_SLOW_ERROR;
            case N_VERY_SLOW_ERROR -> N_VERY_SLOW_ERROR;
            case ERROR -> ERROR;
            case SUM_STAT -> SUM_STAT;
            case MAX_STAT -> MAX_STAT;
            case PING -> PING;
        };
    }

    public ColumnNameV3(byte slotCode) {
        this.slotCode = slotCode;
    }

    public byte getSlotCode() {
        return slotCode;
    }

    public byte[] getColumnName() {
        return makeColumnName(slotCode);
    }

    public static byte[] makeColumnName(byte slotCode) {
        return new byte[] { slotCode };
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        ColumnNameV3 that = (ColumnNameV3) o;
        return slotCode == that.slotCode;
    }

    @Override
    public int hashCode() {
        return slotCode;
    }

    @Override
    public String toString() {
        return "ColumnNameV3{" +
               "slotCode=" + slotCode +
               '}';
    }
}
