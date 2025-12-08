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


public enum SlotCode {

    F_NORMAL(2),
    F_FAST(1),
    F_SLOW(3),
    F_VERY_SLOW(4),

    F_FAST_ERROR(-1),
    F_NORMAL_ERROR(-2),
    F_SLOW_ERROR(-3),
    F_VERY_SLOW_ERROR(-4),

    N_FAST(11),
    N_NORMAL(12),
    N_SLOW(13),
    N_VERY_SLOW(14),

    N_FAST_ERROR(-11),
    N_NORMAL_ERROR(-12),
    N_SLOW_ERROR(-13),
    N_VERY_SLOW_ERROR(-14),


    ERROR(-128),
    SUM_STAT(112),
    MAX_STAT(114),
    PING(126);

    private static final IntHashMap<SlotCode> CODE_MAP = mapping();

    private static IntHashMap<SlotCode> mapping() {
        IntHashMap<SlotCode> map = new IntHashMap<>();

        for (SlotCode slotCode : values()) {
            final SlotCode exist = map.put(slotCode.code, slotCode);
            if (exist != null) {
                throw new IllegalStateException("Duplicate slot code:" + slotCode);
            }
        }
        return map;
    }

    final byte code;

    SlotCode(int code) {
        this.code = (byte) code;
    }

    public byte code() {
        return code;
    }

    public static SlotCode valueOf(byte code) {
        final SlotCode slotCode = CODE_MAP.get(code);
        if (slotCode == null) {
            throw new IllegalStateException("Unknown slot code:" + code);
        }
        return slotCode;
    }

}
