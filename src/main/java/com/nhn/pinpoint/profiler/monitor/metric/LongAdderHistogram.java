package com.nhn.pinpoint.profiler.monitor.metric;

import com.nhn.pinpoint.common.HistogramSchema;
import com.nhn.pinpoint.common.HistogramSlot;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.SlotType;
import com.nhn.pinpoint.profiler.util.jdk.LongAdder;

/**
 * @author emeroad
 */
public class LongAdderHistogram implements Histogram {
    // fastCounter만 LongAdder를 사용하고 나머지는 AtomicLong을 사용하는 방법도 있음.
    private final LongAdder fastCounter = new LongAdder();
    private final LongAdder normalCounter = new LongAdder();
    private final LongAdder slowCounter = new LongAdder();
    private final LongAdder verySlowCounter = new LongAdder();

    private final LongAdder errorCounter = new LongAdder();

    private final ServiceType serviceType;
    private final HistogramSchema schema;

    public LongAdderHistogram(ServiceType serviceType) {
        if (serviceType == null) {
            throw new NullPointerException("serviceType must not be null");
        }
        this.serviceType = serviceType;
        this.schema = serviceType.getHistogramSchema();
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public void addResponseTime(int millis) {
        final HistogramSlot histogramSlot = schema.findHistogramSlot(millis);
        final SlotType slotType = histogramSlot.getSlotType();
        switch (slotType) {
            case FAST:
                fastCounter.increment();
                return;
            case NORMAL:
                normalCounter.increment();
                return;
            case SLOW:
                slowCounter.increment();
                return;
            case VERY_SLOW:
                verySlowCounter.increment();
                return;
            case ERROR:
                errorCounter.increment();
                return;
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

        return new HistogramSnapshot(this.serviceType, fast, normal, slow, verySlow, error);
    }

    @Override
    public String toString() {
        return "LongAdderHistogram{" +
                "fastCounter=" + fastCounter +
                ", normalCounter=" + normalCounter +
                ", slowCounter=" + slowCounter +
                ", verySlowCounter=" + verySlowCounter +
                ", errorCounter=" + errorCounter +
                ", serviceType=" + serviceType +
                '}';
    }
}
