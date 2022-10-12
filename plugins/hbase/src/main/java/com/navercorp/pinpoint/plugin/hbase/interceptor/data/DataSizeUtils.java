package com.navercorp.pinpoint.plugin.hbase.interceptor.data;

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
        return mutation.getRow().length + sumOfFamilyCellMap(mutation.getFamilyCellMap());
    }

    public static int sizeOfResult(Result result) {
        return result.getRow().length + sumOfResultFamilyMap(result.getNoVersionMap());
    }

    public static int sumOfFamilyCellMap(NavigableMap<byte[], List<Cell>> map) {
        if (map == null) {
            return 0;
        }
        int sizeInByte = 0;
        for (Map.Entry<byte[], List<Cell>> e : map.entrySet()) {
            sizeInByte += e.getKey().length;
            for (Cell cell : e.getValue()) {
                sizeInByte += lengthOfCell(cell);
            }
        }
        return sizeInByte;
    }

    public static int sumOfResultFamilyMap(NavigableMap<byte[], NavigableMap<byte[], byte[]>> map) {
        int sizeInByte = 0;
        for (Map.Entry<byte[], NavigableMap<byte[], byte[]>> familyToRest : map.entrySet()) {
            sizeInByte += familyToRest.getKey().length;
            for (Map.Entry<byte[], byte[]> qualifierToValue : familyToRest.getValue().entrySet()) {
                sizeInByte += qualifierToValue.getKey().length;
                sizeInByte += qualifierToValue.getValue().length;
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
