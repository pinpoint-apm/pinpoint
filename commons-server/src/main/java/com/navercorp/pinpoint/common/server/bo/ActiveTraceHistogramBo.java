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
import com.navercorp.pinpoint.common.server.bo.stat.ActiveTraceHistogram;

import java.util.ArrayList;
import java.util.List;

/**
 * @author HyunGil Jeong
 */
@Deprecated
public class ActiveTraceHistogramBo {

    private final byte version;
    private final int histogramSchemaType;
    private final ActiveTraceHistogram activeTraceHistogram;

    public ActiveTraceHistogramBo(int version, int histogramSchemaType, List<Integer> activeTraceCounts) {
        if (version < 0 || version > 255) {
            throw new IllegalArgumentException("version out of range (0~255)");
        }
        this.version = (byte) (version & 0xFF);
        this.histogramSchemaType = histogramSchemaType;
        this.activeTraceHistogram = createActiveTraceHistogram(version, activeTraceCounts);
    }

    private ActiveTraceHistogram createActiveTraceHistogram(int version, List<Integer> activeTraceCounts) {
        if (activeTraceCounts == null) {
            return createUnknownActiveTraceCountMap(version);
        }
        switch (version) {
            case 0:
                if (activeTraceCounts.size() != 4) {
                    throw new IllegalArgumentException("activeTraceCounts does not match specification. Version : " + version + ", activeTraceCounts : " + activeTraceCounts);
                }
                final int fastCount = getCount(activeTraceCounts, 0);
                final int normalCount = getCount(activeTraceCounts, 1);
                final int slowCount = getCount(activeTraceCounts, 2);
                final int verySlowCount = getCount(activeTraceCounts, 3);

                return new ActiveTraceHistogram(fastCount, normalCount, slowCount, verySlowCount);
            default:
                return ActiveTraceHistogram.UNCOLLECTED;
        }
    }

    private int getCount(List<Integer> activeTraceCounts, int index) {
        final Integer value = activeTraceCounts.get(index);
        return value == null ? 0 : value;
    }

    private ActiveTraceHistogram createUnknownActiveTraceCountMap(int version) {
        switch (version) {
            case 0:
                return ActiveTraceHistogram.EMPTY;
            default:
                return ActiveTraceHistogram.UNCOLLECTED;
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
                for (int i = 0; i < numActiveTraceCounts; i++) {
                    activeTraceCounts.add(buffer.readVInt());
                }
                this.activeTraceHistogram = createActiveTraceHistogram(version, activeTraceCounts);
                break;
            default:
                this.activeTraceHistogram = ActiveTraceHistogram.UNCOLLECTED;
                break;
        }
    }

    public int getVersion() {
        return version & 0xFF;
    }

    public int getHistogramSchemaType() {
        return histogramSchemaType;
    }

    public ActiveTraceHistogram getActiveTraceHistogram() {
        return activeTraceHistogram;
    }

    public byte[] writeValue() {
        final Buffer buffer = new AutomaticBuffer();
        buffer.putByte(this.version);
        buffer.putVInt(this.histogramSchemaType);
        int version = this.version & 0xFF;
        switch (version) {
            case 0:
                final int slotSize = 4;
                buffer.putVInt(slotSize);
                buffer.putVInt(this.activeTraceHistogram.getFastCount());
                buffer.putVInt(this.activeTraceHistogram.getNormalCount());
                buffer.putVInt(this.activeTraceHistogram.getSlowCount());
                buffer.putVInt(this.activeTraceHistogram.getVerySlowCount());
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
        return activeTraceHistogram != null ? activeTraceHistogram.equals(that.activeTraceHistogram) : that.activeTraceHistogram == null;

    }

    @Override
    public int hashCode() {
        int result = (int) version;
        result = 31 * result + histogramSchemaType;
        result = 31 * result + (activeTraceHistogram != null ? activeTraceHistogram.hashCode() : 0);
        return result;
    }
}
