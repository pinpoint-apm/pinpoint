package com.navercorp.pinpoint.common.hbase.wd;

import org.apache.hadoop.hbase.client.ClientUtil;

import java.util.Arrays;
import java.util.Objects;

public record ScanKey(byte[] startRow, byte[] stopRow) {
    public ScanKey {
        Objects.requireNonNull(startRow, "startRow");
        Objects.requireNonNull(stopRow, "stopRow");
    }

    public boolean includeStopRow() {
        return ClientUtil.areScanStartRowAndStopRowEqual(this.startRow, this.stopRow);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        ScanKey scanKey = (ScanKey) o;
        return Arrays.equals(stopRow, scanKey.stopRow) && Arrays.equals(startRow, scanKey.startRow);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(startRow);
        result = 31 * result + Arrays.hashCode(stopRow);
        return result;
    }
}
