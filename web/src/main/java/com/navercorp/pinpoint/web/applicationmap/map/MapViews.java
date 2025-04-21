/*
 * Copyright 2024 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.web.applicationmap.map;

import com.navercorp.pinpoint.common.server.util.BitFieldUtils;

public class MapViews {

    private static final int SIMPLIFIED = 0;
    private static final int BASIC = 1;
    private static final int DETAILED = 2;

    @SuppressWarnings("FieldMayBeFinal")
    private int bitSet;

    private MapViews(int bitSet) {
        this.bitSet = bitSet;
    }

    MapViews() {
        this.bitSet = 0;
    }


    public static MapViews ofSimpled() {
        int bitSet = BitFieldUtils.setBit(0, SIMPLIFIED, true);
        return new MapViews(bitSet);
    }

    public static MapViews ofBasic() {
        int bitSet = BitFieldUtils.setBit(0, BASIC, true);
        return new MapViews(bitSet);
    }

    public static MapViews ofDetailed() {
        int bitSet = BitFieldUtils.setBit(0, DETAILED, true);
        return new MapViews(bitSet);
    }

    public MapViews withSimplified() {
        final int mask = BitFieldUtils.setBit(bitSet, SIMPLIFIED, true);
        return new MapViews(mask);
    }

    public MapViews withBasic() {
        final int mask = BitFieldUtils.setBit(bitSet, BASIC, true);
        return new MapViews(mask);
    }

    public MapViews withDetailed() {
        int copy = BitFieldUtils.setBit(bitSet, DETAILED, true);
        return new MapViews(copy);
    }

    public boolean isSimplified() {
        return BitFieldUtils.testBit(bitSet, SIMPLIFIED);
    }

    public boolean isBasic() {
        return BitFieldUtils.testBit(bitSet, BASIC);
    }

    public boolean isDetailed() {
        return BitFieldUtils.testBit(bitSet, DETAILED);
    }

}
