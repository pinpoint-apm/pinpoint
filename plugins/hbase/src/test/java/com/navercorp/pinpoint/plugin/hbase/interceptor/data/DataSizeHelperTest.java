package com.navercorp.pinpoint.plugin.hbase.interceptor.data;

import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Append;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.RowMutations;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class DataSizeHelperTest {

    private static final byte[] rowKey = "row".getBytes();
    private static final byte[] columnFamily = "cf".getBytes();
    private static final byte[] qualifier = "qualifier".getBytes();
    private static final byte[] value = "value".getBytes();

    private static final int ROWKEY_SIZE = rowKey.length;
    private static final int COLUMNFAMILY_SIZE = columnFamily.length;
    private static final int QUALIFIER_SIZE = qualifier.length;
    private static final int VALUE_SIZE = value.length;

    private static final int KEYVALUE_SIZE = ROWKEY_SIZE + COLUMNFAMILY_SIZE + QUALIFIER_SIZE + VALUE_SIZE;

    private static final Result result = Result.create(new Cell[]{new KeyValue(rowKey, columnFamily, qualifier, value)});

    @Test
    public void testGetDataSize() {
        Object[] args = new Object[]{};

        Result result = Result.create(new Cell[]{new KeyValue(rowKey, columnFamily, qualifier, value)});
        int actualSize = DataSizeHelper.getDataSizeFromResult(result, DataOperationType.RESULT);

        int expectedSize = KEYVALUE_SIZE;

        assertEquals(expectedSize, actualSize);
    }

    @Test
    public void testGetListDataSize() {
        Object[] args = new Object[]{};
        Object getAllResult = new Result[]{result, result};

        int expectedSize = (KEYVALUE_SIZE) * 2;
        int actualSize = DataSizeHelper.getDataSizeFromResult(getAllResult, DataOperationType.RESULT_LIST);

        assertEquals(expectedSize, actualSize);
    }

    @Test
    public void testPutDataSize() {
        Put put = new Put(rowKey);
        put.addColumn(columnFamily, qualifier, value);
        Object[] args = new Object[]{put};

        int expectedSize = KEYVALUE_SIZE;
        int actualSize = DataSizeHelper.getDataSizeFromArgument(args, DataOperationType.MUTATION);

        assertEquals(expectedSize, actualSize);
    }

    @Test
    public void testPutMultipleDataSize() {
        Put put = new Put(rowKey);
        put.addColumn(columnFamily, qualifier, value);
        put.addColumn(columnFamily, qualifier, value);
        Object[] args = new Object[]{put};

        int expectedSize = ROWKEY_SIZE + COLUMNFAMILY_SIZE + 2 * (QUALIFIER_SIZE + VALUE_SIZE);
        int actualSize = DataSizeHelper.getDataSizeFromArgument(args, DataOperationType.MUTATION);

        assertEquals(expectedSize, actualSize);
    }

    @Test
    public void testPutListDataSize() {
        Put put = new Put(rowKey);
        put.addColumn(columnFamily, qualifier, value);
        Object[] args = new Object[]{Arrays.asList(put, put)};

        int expectedSize = (KEYVALUE_SIZE) * 2;
        int actualSize = DataSizeHelper.getDataSizeFromArgument(args, DataOperationType.MUTATION_LIST);

        assertEquals(expectedSize, actualSize);
    }

    @Test
    public void testAppendDataSize() {
        Append append = new Append(rowKey);
        append.add(columnFamily, qualifier, value);
        append.add(columnFamily, qualifier, value);
        Object[] args = new Object[]{append};

        int expectedSize = ROWKEY_SIZE + COLUMNFAMILY_SIZE + 2 * (QUALIFIER_SIZE + VALUE_SIZE);
        int actualSize = DataSizeHelper.getDataSizeFromArgument(args, DataOperationType.MUTATION);

        assertEquals(expectedSize, actualSize);
    }

    @Test
    public void testDeleteDataSize() {
        Object[] args = new Object[]{new Delete(rowKey)};
        int actualSize = DataSizeHelper.getDataSizeFromArgument(args, DataOperationType.MUTATION);
        assertEquals(ROWKEY_SIZE, actualSize);
    }


    @Test
    public void testDeleteListDataSize() {
        List<Delete> deletes = new ArrayList<>();
        deletes.add(new Delete(rowKey));
        deletes.add(new Delete(rowKey));
        Object[] args = new Object[]{deletes};
        int actualSize = DataSizeHelper.getDataSizeFromArgument(args, DataOperationType.MUTATION_LIST);
        assertEquals(ROWKEY_SIZE * 2, actualSize);
    }

    @Test
    public void testMutationDataSize() throws IOException {
        RowMutations rowMutations = new RowMutations(rowKey);
        rowMutations.add(new Delete(rowKey));

        Put put = new Put(rowKey);
        put.addColumn(columnFamily, qualifier, value);
        rowMutations.add(put);

        Object[] args = new Object[]{rowMutations};

        int expectedSize = ROWKEY_SIZE + (ROWKEY_SIZE) + (KEYVALUE_SIZE);
        int actualSize = DataSizeHelper.getDataSizeFromArgument(args, DataOperationType.ROW_MUTATION);

        assertEquals(expectedSize, actualSize);
    }

}