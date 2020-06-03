package com.navercorp.pinpoint.common.hbase.util;

import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.Assert;
import org.junit.Test;

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

        Assert.assertEquals(CellUtils.rowToString(result), value);
    }

    @Test
    public void qualifierToInt() {
        Cell cell = mock(Cell.class);
        int value = 5;
        byte[] bytes = Bytes.toBytes(value);
        when(cell.getQualifierArray()).thenReturn(bytes);
        when(cell.getQualifierOffset()).thenReturn(0);
        when(cell.getQualifierLength()).thenReturn(bytes.length);

        Assert.assertEquals(CellUtils.qualifierToInt(cell), value);
    }

    @Test
    public void qualifierToString() {
        Cell cell = mock(Cell.class);
        String value = "abc";
        byte[] bytes = Bytes.toBytes(value);
        when(cell.getQualifierArray()).thenReturn(bytes);
        when(cell.getQualifierOffset()).thenReturn(0);
        when(cell.getQualifierLength()).thenReturn(bytes.length);

        Assert.assertEquals(CellUtils.qualifierToString(cell), value);
    }

    @Test
    public void valueToShort() {
        Cell cell = mock(Cell.class);
        short value = 5;
        byte[] bytes = Bytes.toBytes(value);
        when(cell.getValueArray()).thenReturn(bytes);
        when(cell.getValueOffset()).thenReturn(0);
        when(cell.getValueLength()).thenReturn(bytes.length);

        Assert.assertEquals(CellUtils.valueToShort(cell), value);
    }
}