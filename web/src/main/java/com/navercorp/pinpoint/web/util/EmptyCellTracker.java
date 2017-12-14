package com.navercorp.pinpoint.web.util;

import org.apache.hadoop.hbase.Cell;

/**
 * @author Woonduk Kang(emeroad)
 */
public final class EmptyCellTracker implements CellTracker {

    public static final EmptyCellTracker EMPTY_CELL_TRACER = new EmptyCellTracker();

    private EmptyCellTracker() {
    }

    @Override
    public void trace(Cell cell) {
        // skip
    }

    @Override
    public void log() {
        // skip
    }
}
