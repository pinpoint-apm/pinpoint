package com.navercorp.pinpoint.plugin.hbase.interceptor.data;

import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.*;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class DataSizeHelperTest {

    private static final byte[] rowKey = "row".getBytes();
    private static final byte[] familyBytes = "cf".getBytes();
    private static final byte[] qualifier = "qualifier".getBytes();
    private static final byte[] valBytes = "value".getBytes();

    private static final Result result = Result.create(new Cell[]{new KeyValue(rowKey, familyBytes, qualifier, valBytes)});

    @Test
    public void testGetDataSize() {
        Object[] args = new Object[]{};
        int expectedSize = DataSizeUtils.sumOfCells(result.rawCells());
        int actualSize = DataSizeHelper.getDataSizeFrom("get", args, result);
        assertEquals(expectedSize, actualSize);
    }

    @Test
    public void testGetListDataSize() {
        Object[] args = new Object[]{};
        Object getAllResult = new Result[]{result, result};
        assertEquals(10, DataSizeHelper.getDataSizeFrom("get", args, getAllResult));
    }

    @Test
    public void testPutDataSize() {
        Put put = new Put(rowKey);
        put.addColumn(familyBytes, qualifier, valBytes);
        Object[] args = new Object[]{put};
        assertEquals(5, DataSizeHelper.getDataSizeFrom("put", args, result));
    }


    @Test
    public void testPutListDataSize() {
        Put put = new Put(rowKey);
        put.addColumn(familyBytes, qualifier, valBytes);
        Object[] args = new Object[]{Arrays.asList(put, put)};
        DataSizeProvider putListSizeProvider = new PutListSizeProvider();
        assertEquals(10, DataSizeHelper.getDataSizeFrom("put", args, result));
    }

    @Test
    public void testAppendDataSize() {
        Append append = new Append(rowKey);
        append.add(familyBytes, qualifier, valBytes);
        append.add(familyBytes, qualifier, valBytes);
        Object[] args = new Object[]{append};
        assertEquals(10, DataSizeHelper.getDataSizeFrom("append", args, result));
    }

    @Test
    public void testDeleteDataSize() {
        Object[] args = new Object[]{new Delete(rowKey)};
        assertEquals(0, DataSizeHelper.getDataSizeFrom("delete", args, result));
    }


    @Test
    public void testDeleteListDataSize() {
        Object[] args = new Object[]{new Delete(rowKey), new Delete(rowKey)};
        assertEquals(0, DataSizeHelper.getDataSizeFrom("delete", args, result));
    }

    @Test
    public void testMutationDataSize() throws IOException {
        Put put = new Put(rowKey);
        Object[] args = new Object[]{};
        RowMutations rowMutations = new RowMutations(rowKey);
        rowMutations.add(put);
        rowMutations.add(new Delete(rowKey));
        assertEquals(0, DataSizeHelper.getDataSizeFrom("mutateRow", args, result));
    }

}