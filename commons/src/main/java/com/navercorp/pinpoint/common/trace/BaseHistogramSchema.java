package com.navercorp.pinpoint.common.trace;

import java.util.Objects;

import static com.navercorp.pinpoint.common.trace.HistogramSlotGroup.entry;

/**
 * @author jaehong.kim
 */
public class BaseHistogramSchema implements HistogramSchema {

    private static final short VERY_SLOW_SLOT_TIME = 0;
    // All negative numbers are included in error count
    private static final short ERROR_SLOT_TIME = -1;
    // Do not use negative numbers.
    private static final short PING_SLOT_TIME = Short.MAX_VALUE - 1;
    private static final short STAT_SLOT_TIME_TOTAL = Short.MAX_VALUE - 2;
    private static final short STAT_SLOT_TIME_MAX = Short.MAX_VALUE - 3;


    public static final HistogramSchema FAST_SCHEMA = new BaseHistogramSchema(Schema.FAST,
            new HistogramSlotGroup(
                    entry(100, "100ms", SlotType.FAST),
                    entry(300, "300ms", SlotType.NORMAL),
                    entry(500, "500ms", SlotType.SLOW),
                    entry(VERY_SLOW_SLOT_TIME, "Slow", SlotType.VERY_SLOW)),
            new HistogramSlotGroup(
                    entry(-100, "100ms", SlotType.FAST_ERROR),
                    entry(-300, "300ms", SlotType.NORMAL_ERROR),
                    entry(-500, "500ms", SlotType.SLOW_ERROR),
                    entry(-999, "Slow", SlotType.VERY_SLOW_ERROR))
            );

    public static final HistogramSchema NORMAL_SCHEMA = new BaseHistogramSchema(Schema.NORMAL,
            new HistogramSlotGroup(
                    entry(1000, "1s", SlotType.FAST),
                    entry(3000, "3s", SlotType.NORMAL),
                    entry(5000, "5s", SlotType.SLOW),
                    entry(VERY_SLOW_SLOT_TIME, "Slow", SlotType.VERY_SLOW)),
            new HistogramSlotGroup(
                    entry(-1000, "1s", SlotType.FAST_ERROR),
                    entry(-3000, "3s", SlotType.NORMAL_ERROR),
                    entry(-5000, "5s", SlotType.SLOW_ERROR),
                    entry(-9999, "Slow", SlotType.VERY_SLOW_ERROR))
            );

    private static final String PING_SLOT_NAME = "Ping";

    private final Schema schema;

    public final HistogramSlotGroup success;
    public final HistogramSlotGroup failed;

    @Deprecated
    private final HistogramSlot errorSlot;

    private final HistogramSlot sumStatSlot;
    private final HistogramSlot maxStatSlot;
    private final HistogramSlot pingSlot;

    private BaseHistogramSchema(Schema schema,
                                HistogramSlotGroup success,
                                HistogramSlotGroup failed) {
        this.schema = Objects.requireNonNull(schema, "schema");

        this.success = Objects.requireNonNull(success, "success");
        this.failed = Objects.requireNonNull(failed, "failed");

        this.errorSlot = new HistogramSlot(ERROR_SLOT_TIME, SlotType.ERROR, "Error");

        this.sumStatSlot = new HistogramSlot(STAT_SLOT_TIME_TOTAL, SlotType.SUM_STAT, "SumTime");
        this.maxStatSlot = new HistogramSlot(STAT_SLOT_TIME_MAX, SlotType.MAX_STAT, "Max");

        this.pingSlot = new HistogramSlot(PING_SLOT_TIME, SlotType.PING, PING_SLOT_NAME);
    }

    public Schema getSchema() {
        return schema;
    }

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