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

import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.KeyValueTestUtil;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;


public class CellUtilsTest {

    @Test
    public void rowToString() {
        String value = "abc";
        byte[] bytes = Bytes.toBytes(value);

        Cell cell = new KeyValue(bytes, Bytes.toBytes("cf"),
                Bytes.toBytes("qf"), 1L, KeyValue.Type.Put, Bytes.toBytes(1));

        Result result = Result.create(List.of(cell));

        Assertions.assertEquals(value, CellUtils.rowToString(result));
    }

    @Test
    public void qualifierToShort() {
        short value = 5;
        byte[] bytes = Bytes.toBytes(value);

        Cell cell = cellByQualifier(bytes, Bytes.toBytes(1));

        Assertions.assertEquals(value, CellUtils.qualifierToShort(cell));
    }

    @Test
    public void qualifierToInt() {
        int value = 5;
        byte[] bytes = Bytes.toBytes(value);

        Cell cell = cellByQualifier(bytes, Bytes.toBytes(1));

        Assertions.assertEquals(value, CellUtils.qualifierToInt(cell));
    }

    @Test
    public void qualifierToString() {
        String value = "abc";
        byte[] bytes = Bytes.toBytes(value);

        Cell cell = cellByQualifier(bytes, Bytes.toBytes(1));

        Assertions.assertEquals(value, CellUtils.qualifierToString(cell));
    }

    @Test
    public void valueToShort() {
        short value = 5;
        byte[] bytes = Bytes.toBytes(value);

        Cell cell = cellByValue(bytes);

        Assertions.assertEquals(value, CellUtils.valueToShort(cell));
    }

    @Test
    public void valueToInt() {
        int value = Integer.MAX_VALUE;
        byte[] bytes = Bytes.toBytes(value);

        Cell cell = cellByValue(bytes);

        Assertions.assertEquals(value, CellUtils.valueToInt(cell));
    }

    @Test
    public void valueToLong() {
        long value = Long.MAX_VALUE;
        byte[] bytes = Bytes.toBytes(value);

        Cell cell = cellByValue(bytes);

        Assertions.assertEquals(value, CellUtils.valueToLong(cell));
    }

    private Cell cellByValue(byte[] value) {
        return new KeyValue(Bytes.toBytes("row"), Bytes.toBytes("cf"),
                Bytes.toBytes("qf"), 1L, KeyValue.Type.Put, value);
    }

    private Cell cellByQualifier(byte[] qf, byte[] value) {
        return new KeyValue(Bytes.toBytes("row"), Bytes.toBytes("cf"),
                qf, 1L, KeyValue.Type.Put, value);
    }

    @Test
    public void valueToString() {
        String value = "abc";
        Cell cell = KeyValueTestUtil.create("row", "cf", "cq", 1L, KeyValue.Type.Put, value);

        Assertions.assertEquals(value, CellUtils.valueToString(cell));
    }

    @Test
    public void testCompareRow_whenRowsAreEqual() {
        Cell leftCell = new KeyValue(Bytes.toBytes("row"), 1000L);
        Cell rightCell = new KeyValue(Bytes.toBytes("row"), 1000L);
        int saltKeySize = 0;

        int result = CellUtils.compareRow(leftCell, rightCell, saltKeySize);

        Assertions.assertEquals(0, result, "Rows should be equal.");
    }

    @Test
    public void testCompareRow_whenLeftRowIsLess() {
        Cell leftCell = new KeyValue(Bytes.toBytes("aa"), 1000L);
        Cell rightCell = new KeyValue(Bytes.toBytes("bb"), 2000L);
        int saltKeySize = 0;

        int result = CellUtils.compareRow(leftCell, rightCell, saltKeySize);

        Assertions.assertTrue(result < 0, "Left row should be less than right row.");
    }

    @Test
    public void testCompareRow_whenRightRowIsLess() {
        Cell leftCell = new KeyValue(Bytes.toBytes("bb"), 2000L);
        Cell rightCell = new KeyValue(Bytes.toBytes("aa"), 1000L);
        int saltKeySize = 0;

        int result = CellUtils.compareRow(leftCell, rightCell, saltKeySize);

        Assertions.assertTrue(result > 0, "Right row should be less than left row.");
    }

    @Test
    public void testCompareRow_unsigned() {
        byte[] row1 = new byte[]{(byte) 0x00 }; // 0
        byte[] row2 = new byte[]{(byte) 0xFF }; // -1 (unsigned 255)

        Cell leftCell = new KeyValue(row1, 1000L);
        Cell rightCell = new KeyValue(row2, 2000L);
        int saltKeySize = 0;

        int result = CellUtils.compareRow(leftCell, rightCell, saltKeySize);

        Assertions.assertTrue(result < 0, "Left row should be less than right row.");
    }

    @Test
    public void testCompareRow_saltKey() {
        byte[] leftRow = new byte[]{2, 1};
        Cell leftCell = new KeyValue(leftRow, 2000L);
        byte[] rightRow = new byte[]{1, 2};
        Cell rightCell = new KeyValue(rightRow, 1000L);

        int saltKeySize = 1;

        int result = CellUtils.compareRow(leftCell, rightCell, saltKeySize);

        Assertions.assertTrue(result < 0, "Left row should be less than right row.");
    }
}