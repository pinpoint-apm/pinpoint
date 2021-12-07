package com.navercorp.pinpoint.web.util;

import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DefaultCellTracker implements CellTracker {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final String message;

    private int serializedSize = 0;
    private int serializedKeySize = 0;
    private int cellCount = 0;

    public DefaultCellTracker(String message) {
        this.message = message;
    }

    @Override
    public void trace(Cell cell) {
        this.serializedSize += CellUtil.estimatedSerializedSizeOf(cell);
        this.serializedKeySize += CellUtil.estimatedSerializedSizeOfKey(cell);
        this.cellCount++;
    }

    @Override
    public void log() {
        final int nonKey = serializedSize - serializedKeySize;
        logger.debug("{} cellCount:{} serializedSize:{} key:{} nonKey:{}", message, cellCount, serializedSize, serializedKeySize, nonKey);
    }

}
