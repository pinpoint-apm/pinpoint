package com.nhn.pinpoint.profiler.monitor.metric;

import com.nhn.pinpoint.common.HistogramSchema;
import com.nhn.pinpoint.common.HistogramSlot;
import com.nhn.pinpoint.common.SlotType;
import com.nhn.pinpoint.profiler.util.jdk.LongAdder;

/**
 * @author emeroad
 */
public class Histogram {

    private final LongAdder fastCounter = new LongAdder();
    private final LongAdder normalCounter = new LongAdder();
    private final LongAdder slowCounter = new LongAdder();
    private final LongAdder verySlowCounter = new LongAdder();

    private final LongAdder errorCounter = new LongAdder();

    private final HistogramSchema schema;

    public Histogram(HistogramSchema schema) {
        if (schema == null) {
            throw new NullPointerException("schema must not be null");
        }
        this.schema = schema;
    }


    public void addResponseTime(int millis) {
        final HistogramSlot histogramSlot = schema.findHistogramSlot(millis);
        final SlotType slotType = histogramSlot.getSlotType();
        switch (slotType) {
            case FAST:
                fastCounter.increment();
            case NORMAL:
                normalCounter.increment();
            case SLOW:
                slowCounter.increment();
            case VERY_SLOW:
                verySlowCounter.increment();
            case ERROR:
                errorCounter.increment();
            default:
                throw new IllegalArgumentException("slot Type notFound:" + slotType);
        }
    }


    public HistogramSnapshot createSnapshot() {
        long fast = fastCounter.sum();
        long normal = normalCounter.sum();
        long slow = slowCounter.sum();
        long verySlow = verySlowCounter.sum();
        long error = errorCounter.sum();

        return new HistogramSnapshot(fast, normal, slow, verySlow, error);
    }

}
