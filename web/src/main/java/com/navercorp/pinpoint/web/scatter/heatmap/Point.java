package com.navercorp.pinpoint.web.scatter.heatmap;

public class Point {
    private final long x;
    private final long y;
    private final int success;
    private final int fail;

    Point(long x, long y) {
        this(x, y, 1, 0);
    }

    public Point(long x, long y, int success, int fail) {
        this.x = x;
        this.y = y;
        this.success = success;
        this.fail = fail;
    }

    public long getX() {
        return x;
    }
    public long getY() {
        return y;
    }

    public int getSuccess() {
        return success;
    }

    public int getFail() {
        return fail;
    }
}
