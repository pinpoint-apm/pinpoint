package com.navercorp.pinpoint.plugin.hbase.interceptor.data;

import org.apache.hadoop.hbase.Cell;

import java.util.List;
import java.util.Map;
import java.util.NavigableMap;

public class DataSizeUtils {

    private DataSizeUtils() {
    }

    public static int sumOfFamilyCellMap(NavigableMap<byte[], List<Cell>> map) {
        if (map == null) {
            return 0;
        }
        int sizeInByte = 0;
        for (Map.Entry<byte[], List<Cell>> e : map.entrySet()) {
            for (Cell cell : e.getValue()) {
                sizeInByte += cell.getValueLength();
            }
        }
        return sizeInByte;
    }

    public static int sumOfCells(Cell[] cells) {
        if (cells == null) {
            return 0;
        }
        int sizeInByte = 0;
        for (Cell cell : cells) {
            sizeInByte += cell.getValueLength();
        }
        return sizeInByte;
    }

}
