package com.navercorp.pinpoint.common.hbase.util;

import com.navercorp.pinpoint.common.util.ArrayUtils;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;

import java.util.Objects;

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

    public static String qualifierToString(Cell cell) {
        Objects.requireNonNull(cell, "cell");
        return Bytes.toString(cell.getQualifierArray(), cell.getQualifierOffset(), cell.getQualifierLength());
    }

    public static short valueToShort(Cell cell) {
        Objects.requireNonNull(cell, "cell");
        return Bytes.toShort(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength());
    }
}
