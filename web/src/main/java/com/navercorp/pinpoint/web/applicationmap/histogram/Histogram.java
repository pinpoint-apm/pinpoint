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

package com.navercorp.pinpoint.web.applicationmap.histogram;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.navercorp.pinpoint.common.trace.HistogramSchema;
import com.navercorp.pinpoint.common.trace.HistogramSlot;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.view.HistogramSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.Objects;

/**
 * @author emeroad
 * @author netspider
 * @author jaehong.kim
 */
@JsonSerialize(using = HistogramSerializer.class)
public class Histogram implements StatisticsHistogram {

    private static final Logger LOGGER = LogManager.getLogger(Histogram.class);

    private final HistogramSchema schema;

    private long fastCount;
    private long normalCount;
    private long slowCount;
    private long verySlowCount;


    private long fastErrorCount;
    private long normalErrorCount;
    private long slowErrorCount;
    private long verySlowErrorCount;

    private long sumElapsed;
    private long maxElapsed;

    private long pingCount; // for internal

    public static Histogram sumOf(ServiceType serviceType, Collection<? extends Histogram> histograms) {
        Histogram result = new Histogram(serviceType);
        result.addAll(histograms);
        return result;
    }

    public Histogram(ServiceType serviceType) {
        Objects.requireNonNull(serviceType, "serviceType");
        this.schema = serviceType.getHistogramSchema();
    }

    public Histogram(HistogramSchema schema) {
        this.schema = Objects.requireNonNull(schema, "schema");
    }

    public void addCallCountByElapsedTime(int elapsedTime, boolean error) {
        final HistogramSlot histogramSlot = this.schema.findHistogramSlot(elapsedTime, error);
        short slotTime = histogramSlot.getSlotTime();
        this.sumElapsed += elapsedTime;
        updateMaxElapsed(elapsedTime);
        addCallCount(slotTime, 1);
    }

    public void addCallCountByCode(final byte code, final long count) {
        final HistogramSchema schema = this.schema;

        if (code == schema.getSumStatSlot().getSlotCode()) {
            this.sumElapsed += count;
            return;
        }
        if (code == schema.getMaxStatSlot().getSlotCode()) {
            updateMaxElapsed(count);
            return;
        }
        if (code == schema.getPingSlot().getSlotCode()) { // ping
            this.pingCount += count;
            return;
        }

        if (code == schema.getVerySlowErrorSlot().getSlotCode()) {
            this.verySlowErrorCount += count;
            return;
        }

        if (code == schema.getSlowErrorSlot().getSlotCode()) {
            this.slowErrorCount += count;
            return;
        }


        if (code == schema.getNormalErrorSlot().getSlotCode()) {
            this.normalErrorCount += count;
            return;
        }

        if (code == schema.getFastErrorSlot().getSlotCode()) {
            this.fastErrorCount += count;
            return;
        }

        // @Deprecated
//        if (code == schema.getTotalErrorView().getSlotCode()) {
//            LOGGER.info("Backward compatibility ErrorView schema={}", schema);
//            // this is for backward compatibility
//            this.fastErrorCount += count;
//            return;
//        }

        if (code == schema.getVerySlowSlot().getSlotCode()) { // 0 is slow slotTime
            this.verySlowCount += count;
            return;
        }

        if (code == schema.getFastSlot().getSlotCode()) {
            this.fastCount += count;
            return;
        }

        if (code == schema.getNormalSlot().getSlotCode()) {
            this.normalCount += count;
            return;
        }

        if (code == schema.getSlowSlot().getSlotCode()) {
            this.slowCount += count;
            return;
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("slot not found code={} count={}, schema={}", code, count, schema);
        }
    }

    // TODO one may extract slot number from this class
    public void addCallCount(final short slotTime, final long count) {
        final HistogramSchema schema = this.schema;

        if (slotTime == schema.getSumStatSlot().getSlotTime()) {
            this.sumElapsed += count;
            return;
        }
        if (slotTime == schema.getMaxStatSlot().getSlotTime()) {
            updateMaxElapsed(count);
            return;
        }
        if (slotTime == schema.getPingSlot().getSlotTime()) { // ping
            this.pingCount += count;
            return;
        }

        if (slotTime <= schema.getVerySlowErrorSlot().getSlotTime()) {
            this.verySlowErrorCount += count;
            return;
        }

        if (slotTime <= schema.getSlowErrorSlot().getSlotTime()) {
            this.slowErrorCount += count;
            return;
        }


        if (slotTime <= schema.getNormalErrorSlot().getSlotTime()) {
            this.normalErrorCount += count;
            return;
        }

        if (slotTime <= schema.getFastErrorSlot().getSlotTime()) {
            this.fastErrorCount += count;
            return;
        }

        if (slotTime <= schema.getTotalErrorView().getSlotTime()) { // -1 is error
            LOGGER.info("Backward compatibility ErrorView schema={}", schema);
            // this is for backward compatibility
            this.fastErrorCount += count;
            return;
        }

        if (slotTime == schema.getVerySlowSlot().getSlotTime()) { // 0 is slow slotTime
            this.verySlowCount += count;
            return;
        }

        if (slotTime <= schema.getFastSlot().getSlotTime()) {
            this.fastCount += count;
            return;
        }

        if (slotTime <= schema.getNormalSlot().getSlotTime()) {
            this.normalCount += count;
            return;
        }

        if (slotTime <= schema.getSlowSlot().getSlotTime()) {
            this.slowCount += count;
            return;
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("slot not found slotTime={} count={}, schema={}", slotTime, count, schema);
        }
    }

    private void updateMaxElapsed(long elapsedTime) {
        this.maxElapsed = Math.max(this.maxElapsed, elapsedTime);
    }

    public HistogramSchema getHistogramSchema() {
        return this.schema;
    }

    public long getTotalErrorCount() {
        // Skip ping count
        return fastErrorCount + normalErrorCount + slowErrorCount + verySlowErrorCount;
    }

    public long getFastCount() {
        return fastCount;
    }

    public long getFastErrorCount() {
        return fastErrorCount;
    }

    public long getNormalCount() {
        return normalCount;
    }

    public long getNormalErrorCount() {
        return normalErrorCount;
    }

    public long getSlowCount() {
        return slowCount;
    }

    public long getSlowErrorCount() {
        return slowErrorCount;
    }

    public long getVerySlowCount() {
        return verySlowCount;
    }

    public long getVerySlowErrorCount() {
        return verySlowErrorCount;
    }

    public long getPingCount() {
        return pingCount;
    }

    public long getTotalCount() {
        return getSuccessCount() + getTotalErrorCount();
    }

    public long getSuccessCount() {
        return fastCount + normalCount + slowCount + verySlowCount;
    }

    public long getSumElapsed() {
        return sumElapsed;
    }

    public long getMaxElapsed() {
        return maxElapsed;
    }

    public long getAvgElapsed() {
        final long totalCount = getTotalCount();
        return totalCount > 0 ? sumElapsed / totalCount : 0L;
    }

    public void addAll(final Collection<? extends Histogram> histograms) {
        Objects.requireNonNull(histograms, "histograms");
        for (Histogram histogram : histograms) {
            add(histogram);
        }
    }

    public void add(final Histogram histogram) {
        Objects.requireNonNull(histogram, "histogram");

        if (this.schema != histogram.schema) {
            throw new IllegalArgumentException("schema not equals. this=" + this + ", histogram=" + histogram);

        }
        this.fastCount += histogram.getFastCount();
        this.fastErrorCount += histogram.getFastErrorCount();
        this.normalCount += histogram.getNormalCount();
        this.normalErrorCount += histogram.getNormalErrorCount();
        this.slowCount += histogram.getSlowCount();
        this.slowErrorCount += histogram.getSlowErrorCount();
        this.verySlowCount += histogram.getVerySlowCount();
        this.verySlowErrorCount += histogram.getVerySlowErrorCount();
        this.sumElapsed += histogram.getSumElapsed();
        updateMaxElapsed(histogram.getMaxElapsed());
        this.pingCount += histogram.getPingCount();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Histogram histogram = (Histogram) o;

        if (schema != histogram.schema) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return schema.hashCode();
    }

    @Override
    public String toString() {
        return "Histogram{" +
                "schema=" + schema +
                ", fastCount=" + fastCount +
                ", normalCount=" + normalCount +
                ", slowCount=" + slowCount +
                ", verySlowCount=" + verySlowCount +
                ", fastErrorCount=" + fastErrorCount +
                ", normalErrorCount=" + normalErrorCount +
                ", slowErrorCount=" + slowErrorCount +
                ", verySlowErrorCount=" + verySlowErrorCount +
                ", sumElapsed=" + sumElapsed +
                ", maxElapsed=" + maxElapsed +
                ", pingCount=" + pingCount +
                '}';
    }
}