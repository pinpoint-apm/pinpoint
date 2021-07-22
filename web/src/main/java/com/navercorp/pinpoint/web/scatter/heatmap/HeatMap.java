package com.navercorp.pinpoint.web.scatter.heatmap;

import java.util.List;
import java.util.Objects;

public class HeatMap {
    private final List<Point> data;
    private final long success;
    private final long fail;

    private final long oldestAcceptedTime;
    private final long latestAcceptedTime;

    private final long[] xIndex;
    private final long xTick;

    private final long[] yIndex;
    private final long yTick;

    public HeatMap(List<Point> data, long success, long fail,
                   long oldestAcceptedTime,
                   long latestAcceptedTime,
                   long[] xIndex, long xTick, long[] yIndex, long yTick) {
        this.data = Objects.requireNonNull(data, "data");
        this.success = success;
        this.fail = fail;

        this.oldestAcceptedTime = oldestAcceptedTime;
        this.latestAcceptedTime = latestAcceptedTime;
        this.xIndex = Objects.requireNonNull(xIndex, "xIndex");
        this.xTick = xTick;
        this.yIndex = Objects.requireNonNull(yIndex, "yIndex");
        this.yTick = yTick;
    }

    public List<Point> getData() {
        return data;
    }

    public long getSuccess() {
        return success;
    }

    public long getFail() {
        return fail;
    }

    public long getOldestAcceptedTime() {
        if (oldestAcceptedTime == Long.MAX_VALUE) {
            return -1;
        }
        return oldestAcceptedTime;
    }

    public long getLatestAcceptedTime() {
        if (latestAcceptedTime == Long.MIN_VALUE) {
            return -1;
        }
        return latestAcceptedTime;
    }

    public long[] getXIndex() {
        return xIndex;
    }

    public long[] getYIndex() {
        return yIndex;
    }

    public long getXTick() {
        return xTick;
    }

    public long getYTick() {
        return yTick;
    }

    @Override
    public String toString() {
        return "HeatMap{" +
                "data=" + data.size() +
                ", success=" + success +
                ", fail=" + fail +
                ", oldestAcceptedTime=" + oldestAcceptedTime +
                ", latestAcceptedTime=" + latestAcceptedTime +
                '}';
    }

}
