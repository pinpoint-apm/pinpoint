package com.navercorp.pinpoint.common.server.bo.stat;

import java.util.Objects;

public abstract class AbstractStatDataPoint implements StatDataPoint {

    protected final DataPoint point;

    public AbstractStatDataPoint(DataPoint point) {
        this.point = Objects.requireNonNull(point, "point");
    }

    @Override
    public DataPoint getDataPoint() {
        return point;
    }

}
