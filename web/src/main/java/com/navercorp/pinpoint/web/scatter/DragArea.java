package com.navercorp.pinpoint.web.scatter;


import com.navercorp.pinpoint.common.server.util.pair.LongPair;

public class DragArea {

    private final long xHigh;
    private final long xLow;
    private final long yHigh;
    private final long yLow;

    private DragArea(long xHigh, long xLow, long yHigh, long yLow) {
        this.xHigh = xHigh;
        this.xLow = xLow;
        this.yHigh = yHigh;
        this.yLow = yLow;
    }

    public static DragArea normalize(long x1, long x2, long y1, long y2) {
        final LongPair xPair = normalize(x1, x2);
        final LongPair yPair = normalize(y1, y2);
        return new DragArea(xPair.getFirst(), xPair.getSecond(), yPair.getFirst(), yPair.getSecond());
    }

    public long getXHigh() {
        return xHigh;
    }

    public long getXLow() {
        return xLow;
    }

    public long getYHigh() {
        return yHigh;
    }

    public long getYLow() {
        return yLow;
    }

    private static LongPair normalize(long x1, long x2) {
        if (x1 > x2) {
            return new LongPair(x1, x2);
        }
        return new LongPair(x2, x1);
    }

    @Override
    public String toString() {
        return "DragArea{" +
                "xHigh=" + xHigh +
                ", xLow=" + xLow +
                ", yHigh=" + yHigh +
                ", yLow=" + yLow +
                '}';
    }
}
