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
 * @author jaehong.kim
 */
public class BaseHistogramSchema implements HistogramSchema {

    private final Schema schema;

    public final HistogramSlotGroup success;
    public final HistogramSlotGroup failed;

    @Deprecated
    private final HistogramSlot errorSlot;

    private final HistogramSlot sumStatSlot;
    private final HistogramSlot maxStatSlot;
    private final HistogramSlot pingSlot;

    BaseHistogramSchema(Schema schema,
                        HistogramSlotGroup success,
                        HistogramSlotGroup failed,
                        HistogramSlot errorSlot,
                        HistogramSlot sumStatSlot,
                        HistogramSlot maxStatSlot,
                        HistogramSlot pingSlot) {
        this.schema = Objects.requireNonNull(schema, "schema");

        this.success = Objects.requireNonNull(success, "success");
        this.failed = Objects.requireNonNull(failed, "failed");

        this.errorSlot = Objects.requireNonNull(errorSlot, "errorSlot");

        this.sumStatSlot = Objects.requireNonNull(sumStatSlot, "sumStatSlot");
        this.maxStatSlot = Objects.requireNonNull(maxStatSlot, "maxStatSlot");

        this.pingSlot = Objects.requireNonNull(pingSlot, "pingSlot");

    }


    @Override
    public Schema getSchema() {
        return schema;
    }

    @Override
    public int getTypeCode() {
        return getSchema().type();
    }

    public HistogramSlot findHistogramSlot(int elapsedTime, boolean error) {
        final HistogramSlotGroup slotGroup = this.success;

        if (elapsedTime <= slotGroup.getFastSlot().getSlotTime()) {
            return getGroup(error).getFastSlot();
        }
        if (elapsedTime <= slotGroup.getNormalSlot().getSlotTime()) {
            return getGroup(error).getNormalSlot();
        }
        if (elapsedTime <= slotGroup.getSlowSlot().getSlotTime()) {
            return getGroup(error).getSlowSlot();
        }
        return getGroup(error).getVerySlowSlot();
    }

    private HistogramSlotGroup getGroup(boolean error) {
        return error ? failed : success;
    }

    public HistogramSlot getFastSlot() {
        return success.getFastSlot();
    }

    public HistogramSlot getNormalSlot() {
        return success.getNormalSlot();
    }

    public HistogramSlot getSlowSlot() {
        return success.getSlowSlot();
    }

    public HistogramSlot getVerySlowSlot() {
        return success.getVerySlowSlot();
    }

    public HistogramSlot getTotalErrorView() {
        return errorSlot;
    }

    public HistogramSlot getFastErrorSlot() {
        return failed.getFastSlot();
    }

    public HistogramSlot getNormalErrorSlot() {
        return failed.getNormalSlot();
    }

    public HistogramSlot getSlowErrorSlot() {
        return failed.getSlowSlot();
    }

    public HistogramSlot getVerySlowErrorSlot() {
        return failed.getVerySlowSlot();
    }

    @Override
    public HistogramSlot getSumStatSlot() {
        return sumStatSlot;
    }

    @Override
    public HistogramSlot getMaxStatSlot() {
        return maxStatSlot;
    }

    @Override
    public HistogramSlot getPingSlot() {
        return pingSlot;
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        BaseHistogramSchema that = (BaseHistogramSchema) o;
        return schema == that.schema;
    }

    @Override
    public int hashCode() {
        return schema.hashCode();
    }

    @Override
    public String toString() {
        return "BaseHistogramSchema{" +
               "schema=" + schema +
               ", success=" + success +
               ", failed=" + failed +
               ", errorSlot=" + errorSlot +
               ", pingSlot=" + pingSlot +
               '}';
    }

}