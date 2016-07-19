package com.navercorp.pinpoint.web.util;

import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;

/**
 * @author Woonduk Kang(emeroad)
 */
public interface CellTracker {

    void trace(Cell cell);

    void log();
}
