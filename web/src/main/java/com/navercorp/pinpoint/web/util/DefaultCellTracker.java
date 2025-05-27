package com.navercorp.pinpoint.web.util;

import org.apache.hadoop.hbase.Cell;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DefaultCellTracker implements CellTracker {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final String message;

    private int serializedSize = 0;
    private int cellCount = 0;

    public DefaultCellTracker(String message) {
        this.message = message;
    }

    @Override
    public void trace(Cell cell) {
        this.serializedSize += cell.getSerializedSize();
        this.cellCount++;
    }

    @Override
    public void log() {
        logger.debug("{} cellCount:{} serializedSize:{}", message, cellCount, serializedSize);
    }

}
