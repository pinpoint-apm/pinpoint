/*
 * Copyright 2016 NAVER Corp.
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

package com.navercorp.pinpoint.common.server.bo;

import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.FixedBuffer;
import com.navercorp.pinpoint.common.trace.SlotType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author HyunGil Jeong
 */
@Deprecated
public class ActiveTraceHistogramBo {

    private final byte version;
    private final int histogramSchemaType;
    private final Map<SlotType, Integer> activeTraceCountMap;

    public ActiveTraceHistogramBo(int version, int histogramSchemaType, List<Integer> activeTraceCounts) {
        if (version < 0 || version > 255) {
            throw new IllegalArgumentException("version out of range (0~255)");
        }
        this.version = (byte) (version & 0xFF);
        this.histogramSchemaType = histogramSchemaType;
        this.activeTraceCountMap = createActiveTraceCountMap(version, activeTraceCounts);
    }

    private Map<SlotType, Integer> createActiveTraceCountMap(int version, List<Integer> activeTraceCounts) {
        if (activeTraceCounts == null) {
            return createUnknownActiveTraceCountMap(version);
        }
        switch (version) {
            case 0:
                if (activeTraceCounts.size() != 4) {
                    throw new IllegalArgumentException("activeTraceCounts does not match specification. Version : " + version + ", activeTraceCounts : " + activeTraceCounts);
                } else {
                    int fastCount = activeTraceCounts.get(0) == null ? 0 : activeTraceCounts.get(0);
                    int normalCount = activeTraceCounts.get(1) == null ? 0 : activeTraceCounts.get(1);
                    int slowCount = activeTraceCounts.get(2) == null ? 0 : activeTraceCounts.get(2);
                    int verySlowCount = activeTraceCounts.get(3) == null ? 0 : activeTraceCounts.get(3);
                    Map<SlotType, Integer> activeTraceCountMap = new HashMap<SlotType, Integer>();
                    activeTraceCountMap.put(SlotType.FAST, fastCount);
                    activeTraceCountMap.put(SlotType.NORMAL, normalCount);
                    activeTraceCountMap.put(SlotType.SLOW, slowCount);
                    activeTraceCountMap.put(SlotType.VERY_SLOW, verySlowCount);
                    return activeTraceCountMap;
                }
            default:
                return Collections.emptyMap();
        }
    }

    private Map<SlotType, Integer> createUnknownActiveTraceCountMap(int version) {
        switch (version) {
            case 0:
                Map<SlotType, Integer> map = new HashMap<SlotType, Integer>();
                map.put(SlotType.FAST, 0);
                map.put(SlotType.NORMAL, 0);
                map.put(SlotType.SLOW, 0);
                map.put(SlotType.VERY_SLOW, 0);
                return map;
            default:
                return Collections.emptyMap();
        }
    }

    public ActiveTraceHistogramBo(byte[] serializedActiveTraceHistogramBo) {
        final Buffer buffer = new FixedBuffer(serializedActiveTraceHistogramBo);
        this.version = buffer.readByte();
        this.histogramSchemaType = buffer.readVInt();
        int version = this.version & 0xFF;
        switch (version) {
            case 0:
                int numActiveTraceCounts = buffer.readVInt();
                List<Integer> activeTraceCounts = new ArrayList<Integer>(numActiveTraceCounts);
                for (int i = 0; i < numActiveTraceCounts; ++i) {
                    activeTraceCounts.add(buffer.readVInt());
                }
                this.activeTraceCountMap = createActiveTraceCountMap(version, activeTraceCounts);
                break;
            default:
                this.activeTraceCountMap = Collections.emptyMap();
                break;
        }
    }

    public int getVersion() {
        return version & 0xFF;
    }

    public int getHistogramSchemaType() {
        return histogramSchemaType;
    }

    public Map<SlotType, Integer> getActiveTraceCountMap() {
        return activeTraceCountMap;
    }

    public byte[] writeValue() {
        final Buffer buffer = new AutomaticBuffer();
        buffer.putByte(this.version);
        buffer.putVInt(this.histogramSchemaType);
        int version = this.version & 0xFF;
        switch (version) {
            case 0:
                buffer.putVInt(this.activeTraceCountMap.size());
                buffer.putVInt(this.activeTraceCountMap.get(SlotType.FAST));
                buffer.putVInt(this.activeTraceCountMap.get(SlotType.NORMAL));
                buffer.putVInt(this.activeTraceCountMap.get(SlotType.SLOW));
                buffer.putVInt(this.activeTraceCountMap.get(SlotType.VERY_SLOW));
                break;
            default:
                break;
        }
        return buffer.getBuffer();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ActiveTraceHistogramBo that = (ActiveTraceHistogramBo) o;

        if (version != that.version) return false;
        if (histogramSchemaType != that.histogramSchemaType) return false;
        return activeTraceCountMap != null ? activeTraceCountMap.equals(that.activeTraceCountMap) : that.activeTraceCountMap == null;

    }

    @Override
    public int hashCode() {
        int result = (int) version;
        result = 31 * result + histogramSchemaType;
        result = 31 * result + (activeTraceCountMap != null ? activeTraceCountMap.hashCode() : 0);
        return result;
    }
}
