package com.navercorp.pinpoint.common.hbase.util;

import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.OffsetFixedBuffer;
import com.navercorp.pinpoint.common.util.ArrayUtils;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;

import java.util.Objects;
import java.util.UUID;

public final class CellUtils {
    private CellUtils() {
    }

    public static String rowToString(Result result) {
        Objects.requireNonNull(result, "result");
        final Cell[] cells = result.rawCells();
        if (ArrayUtils.isEmpty(cells)) {
            return null;
        }
        final Cell firstCell = cells[0];
        return Bytes.toString(firstCell.getRowArray(), firstCell.getRowOffset(), firstCell.getRowLength());
    }

    public static int qualifierToInt(Cell cell) {
        Objects.requireNonNull(cell, "cell");
        return Bytes.toInt(cell.getQualifierArray(), cell.getQualifierOffset(), cell.getQualifierLength());
    }

    public static long qualifierToLong(Cell cell) {
        Objects.requireNonNull(cell, "cell");
        return Bytes.toLong(cell.getQualifierArray(), cell.getQualifierOffset(), cell.getQualifierLength());
    }

    public static String qualifierToString(Cell cell) {
        Objects.requireNonNull(cell, "cell");
        return Bytes.toString(cell.getQualifierArray(), cell.getQualifierOffset(), cell.getQualifierLength());
    }

    public static short valueToShort(Cell cell) {
        Objects.requireNonNull(cell, "cell");
        return Bytes.toShort(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength());
    }

    public static long valueToLong(Cell cell) {
        Objects.requireNonNull(cell, "cell");
        return Bytes.toLong(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength());
    }

    public static String valueToString(Cell cell) {
        Objects.requireNonNull(cell, "cell");
        return Bytes.toString(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength());
    }

    public static UUID valueToUUID(Cell cell) {
        Objects.requireNonNull(cell, "cell");
        Buffer buffer = new OffsetFixedBuffer(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength());
        return buffer.readUUID();
    }


    public static Cell lastCell(Cell[] rawCells, byte[] columnFamily) {
        Cell last = null;
        for (Cell rawCell : rawCells) {
            if (CellUtil.matchingFamily(rawCell, columnFamily)) {
                last = rawCell;
            }
        }
        return last;
    }
}
