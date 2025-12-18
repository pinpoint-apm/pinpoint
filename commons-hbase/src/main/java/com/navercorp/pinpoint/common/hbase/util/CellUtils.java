/*
 * Copyright 2025 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.common.hbase.util;

import com.navercorp.pinpoint.common.util.ArrayUtils;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;

import java.util.Arrays;
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

    public static short qualifierToShort(Cell cell) {
        Objects.requireNonNull(cell, "cell");
        return Bytes.toShort(cell.getQualifierArray(), cell.getQualifierOffset(), cell.getQualifierLength());
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

    public static int valueToInt(Cell cell) {
        Objects.requireNonNull(cell, "cell");
        return Bytes.toInt(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength());
    }

    public static long valueToLong(Cell cell) {
        Objects.requireNonNull(cell, "cell");
        return Bytes.toLong(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength());
    }

    public static String valueToString(Cell cell) {
        Objects.requireNonNull(cell, "cell");
        return Bytes.toString(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength());
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

    public static int compareFirstRow(Result left, Result right, int saltKeySize) {
        if (isEmpty(left)) {
            throw new IllegalArgumentException("left");
        }
        if (isEmpty(right)) {
            throw new IllegalArgumentException("right");
        }
        final Cell leftCell = left.rawCells()[0];
        final Cell rightCell = right.rawCells()[0];

        return compareRow(leftCell, rightCell, saltKeySize);
    }

    public static boolean isEmpty(Result result) {
        return result == null || result.isEmpty();
    }

    public static int compareRow(Cell leftCell, Cell rightCell, int saltKeySize) {
        if (leftCell == null) {
            throw new NullPointerException("leftCell");
        }
        if (rightCell == null) {
            throw new NullPointerException("rightCell");
        }
        final int leftOffset = leftCell.getRowOffset();
        final int rightOffset = rightCell.getRowOffset();
        return Arrays.compare(leftCell.getRowArray(), leftOffset + saltKeySize, leftOffset + leftCell.getRowLength(),
                rightCell.getRowArray(), rightOffset + saltKeySize, rightOffset + rightCell.getRowLength());
    }
}
