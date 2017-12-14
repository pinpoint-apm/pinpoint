package com.navercorp.pinpoint.web.util;

import org.apache.hadoop.hbase.Cell;

/**
 * @author Woonduk Kang(emeroad)
 */
public interface CellTracker {

    void trace(Cell cell);

    void log();
}
