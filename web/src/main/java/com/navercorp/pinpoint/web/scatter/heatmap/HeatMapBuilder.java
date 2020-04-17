package com.navercorp.pinpoint.web.scatter.heatmap;

import com.navercorp.pinpoint.common.server.util.pair.IntegerValuePair;
import com.navercorp.pinpoint.common.server.util.pair.LongPair;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class HeatMapBuilder {
    private static final long modulate = 100;
    private final AxisResolver xAxisResolver;
    private final AxisResolver yAxisResolver;

    private long oldestAcceptedTime = Long.MAX_VALUE;
    private long latestAcceptedTime = Long.MIN_VALUE;

    private Map<LongPair, IntegerValuePair> map = newMap();

    private Map<LongPair, IntegerValuePair> newMap() {
        return new HashMap<>(128);
    }

    public static HeatMapBuilder newBuilder(long startX, long endX, int xSlot, long minY, long maxY, int ySlot) {
        AxisResolver xResolver = new DefaultAxisResolver(xSlot, startX, endX);
        AxisResolver yResolver = new DefaultAxisResolver(ySlot, minY, maxY);
        return new HeatMapBuilder(xResolver, yResolver);
    }

    public HeatMapBuilder(AxisResolver xAxisResolver, AxisResolver yAxisResolver) {
        this.xAxisResolver = Objects.requireNonNull(xAxisResolver, "xAxisResolver");
        this.yAxisResolver = Objects.requireNonNull(yAxisResolver, "yAxisResolver");
    }


    public interface AxisResolver {
        long getIndex(long x);

        long[] getIndex();

        long getTick();
    }

    public static class DefaultAxisResolver implements AxisResolver {
        private final long modulate;
        private final long tick;
        private final long start;
        private final long range;
        private final int slotNumber;

        public DefaultAxisResolver(int slotNumber, long minY, long maxY) {
            this.modulate = 200;
            this.start = minY;
            this.range = maxY - minY;
            this.tick = range / slotNumber;
            this.slotNumber = slotNumber;
        }

        public long getTick() {
            return tick;
        }

        public long getIndex(long x) {
            x = x - start;
            x = Math.min(x, range);
            if (x <= 0) {
                return 0;
            }

            return x / tick;
        }

        @Override
        public long[] getIndex() {
            long[] index = new long[slotNumber];
            for (int i = 0; i < slotNumber; i++) {
                index[i] = (i  * tick) + start;
            }
            return index;
        }
    }

    public void addDataPoint(long x, long y, boolean success) {

        final long xTick = xAxisResolver.getIndex(x);
        final long yTick = yAxisResolver.getIndex(y);

        this.oldestAcceptedTime = Math.min(oldestAcceptedTime, x);
        this.latestAcceptedTime = Math.max(latestAcceptedTime, x);

        final LongPair key = new LongPair(xTick, yTick);
        IntegerValuePair counter = this.map.computeIfAbsent(key, longPair -> new IntegerValuePair(0, 0));
        if (success) {
            counter.addFirst(1);
        } else {
            counter.addSecond(1);
        }
    }


    public HeatMap build() {
        final Map<LongPair, IntegerValuePair> copy = this.map;
        this.map = newMap();

        long success = 0;
        long fail = 0;


        final List<Point> list = new ArrayList<>(copy.size());
        for (Map.Entry<LongPair, IntegerValuePair> entry : copy.entrySet()) {
            LongPair key = entry.getKey();
            IntegerValuePair value = entry.getValue();

            success += value.getFirst();
            fail += value.getSecond();

            final long acceptedTime = key.getFirst();

            Point point = new Point(acceptedTime, key.getSecond(), value.getFirst(), value.getSecond());

            list.add(point);
        }
        list.sort(COMPARATOR);
        long[] xIndex = xAxisResolver.getIndex();
        long xTick = xAxisResolver.getTick();
        long[] yIndex = yAxisResolver.getIndex();
        long yTick = yAxisResolver.getTick();

        return new HeatMap(list, success, fail, oldestAcceptedTime, latestAcceptedTime, xIndex, xTick, yIndex, yTick);
    }

    private static final Comparator<Point> COMPARATOR = new Comparator<Point>() {

        @Override
        public int compare(Point o1, Point o2) {
            final int comp1 = Long.compare(o2.getX(), o1.getX());
            if (comp1 != 0) {
                return comp1;
            }
            return Long.compare(o1.getY(), o2.getY());
        }
    };

}
