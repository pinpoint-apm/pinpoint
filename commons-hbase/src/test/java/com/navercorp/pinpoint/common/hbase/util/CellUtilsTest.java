package com.navercorp.pinpoint.common.hbase.util;

import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CellUtilsTest {

    @Test
    public void rowToString() {
        Cell cell = mock(Cell.class);
        String value = "abc";
        byte[] bytes = Bytes.toBytes(value);
        when(cell.getRowArray()).thenReturn(bytes);
        when(cell.getRowOffset()).thenReturn(0);
        when(cell.getRowLength()).thenReturn((short) bytes.length);
        Result result = mock(Result.class);
        Cell[] cells = new Cell[]{cell};
        when(result.rawCells()).thenReturn(cells);

        Assertions.assertEquals(value, CellUtils.rowToString(result));
    }

    @Test
    public void qualifierToInt() {
        Cell cell = mock(Cell.class);
        int value = 5;
        byte[] bytes = Bytes.toBytes(value);
        when(cell.getQualifierArray()).thenReturn(bytes);
        when(cell.getQualifierOffset()).thenReturn(0);
        when(cell.getQualifierLength()).thenReturn(bytes.length);

        Assertions.assertEquals(value, CellUtils.qualifierToInt(cell));
    }

    @Test
    public void qualifierToString() {
        Cell cell = mock(Cell.class);
        String value = "abc";
        byte[] bytes = Bytes.toBytes(value);
        when(cell.getQualifierArray()).thenReturn(bytes);
        when(cell.getQualifierOffset()).thenReturn(0);
        when(cell.getQualifierLength()).thenReturn(bytes.length);

        Assertions.assertEquals(value, CellUtils.qualifierToString(cell));
    }

    @Test
    public void valueToShort() {
        Cell cell = mock(Cell.class);
        short value = 5;
        byte[] bytes = Bytes.toBytes(value);
        when(cell.getValueArray()).thenReturn(bytes);
        when(cell.getValueOffset()).thenReturn(0);
        when(cell.getValueLength()).thenReturn(bytes.length);

        Assertions.assertEquals(value, CellUtils.valueToShort(cell));
    }

    @Test
    public void valueToInt() {
        Cell cell = mock(Cell.class);
        int value = Integer.MAX_VALUE;
        byte[] bytes = Bytes.toBytes(value);
        when(cell.getValueArray()).thenReturn(bytes);
        when(cell.getValueOffset()).thenReturn(0);
        when(cell.getValueLength()).thenReturn(bytes.length);

        Assertions.assertEquals(value, CellUtils.valueToInt(cell));
    }

    @Test
    public void valueToLong() {
        Cell cell = mock(Cell.class);
        long value = Long.MAX_VALUE;
        byte[] bytes = Bytes.toBytes(value);
        when(cell.getValueArray()).thenReturn(bytes);
        when(cell.getValueOffset()).thenReturn(0);
        when(cell.getValueLength()).thenReturn(bytes.length);

        Assertions.assertEquals(value, CellUtils.valueToLong(cell));
    }

    @Test
    public void valueToString() {
        Cell cell = mock(Cell.class);
        String value = "abc";
        byte[] bytes = Bytes.toBytes(value);
        when(cell.getValueArray()).thenReturn(bytes);
        when(cell.getValueOffset()).thenReturn(0);
        when(cell.getValueLength()).thenReturn(bytes.length);

        Assertions.assertEquals(value, CellUtils.valueToString(cell));
    }
}