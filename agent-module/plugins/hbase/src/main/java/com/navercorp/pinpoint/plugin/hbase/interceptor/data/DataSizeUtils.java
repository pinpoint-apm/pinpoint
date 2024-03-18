package com.navercorp.pinpoint.plugin.hbase.interceptor.data;

import com.navercorp.pinpoint.common.util.ArrayUtils;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.client.Mutation;
import org.apache.hadoop.hbase.client.Result;

import java.util.List;
import java.util.Map;
import java.util.NavigableMap;

public class DataSizeUtils {


    //    row 1
    //    repeated (
    //            family : 1
    //            repeated (
    //            QualifierValue : n
    //       )
    //    )


    private DataSizeUtils() {
    }

    public static int sizeOfMutation(Mutation mutation) {
        return ArrayUtils.getLength(mutation.getRow()) + sumOfFamilyCellMap(mutation.getFamilyCellMap());
    }

    public static int sizeOfResult(Result result) {
        return ArrayUtils.getLength(result.getRow()) + sumOfResultFamilyMap(result.getNoVersionMap());
    }

    public static int sumOfFamilyCellMap(NavigableMap<byte[], List<Cell>> map) {
        if (map == null) {
            return 0;
        }
        int sizeInByte = 0;
        for (Map.Entry<byte[], List<Cell>> e : map.entrySet()) {
            sizeInByte += ArrayUtils.getLength(e.getKey());
            for (Cell cell : e.getValue()) {
                sizeInByte += lengthOfCell(cell);
            }
        }
        return sizeInByte;
    }

    public static int sumOfResultFamilyMap(NavigableMap<byte[], NavigableMap<byte[], byte[]>> map) {
        int sizeInByte = 0;
        for (Map.Entry<byte[], NavigableMap<byte[], byte[]>> familyToRest : map.entrySet()) {
            sizeInByte += ArrayUtils.getLength(familyToRest.getKey());
            for (Map.Entry<byte[], byte[]> qualifierToValue : familyToRest.getValue().entrySet()) {
                sizeInByte += ArrayUtils.getLength(qualifierToValue.getKey());
                sizeInByte += ArrayUtils.getLength(qualifierToValue.getValue());
            }
        }
        return sizeInByte;
    }

    private static int lengthOfCell(Cell cell) {
        int sizeInByte = 0;
        sizeInByte += cell.getQualifierLength();
        sizeInByte += cell.getValueLength();
        return sizeInByte;
    }
}
