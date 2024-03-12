/*
 * Copyright 2023 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.common.hbase.scan;

import com.navercorp.pinpoint.common.hbase.HbaseSystemException;
import com.sematext.hbase.wd.AbstractRowKeyDistributor;
import org.apache.hadoop.hbase.client.AsyncTable;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;

import java.io.IOException;
import java.util.function.Function;

public final class ScanUtils {

    public static Scan[] splitScans(Scan originalScan, AbstractRowKeyDistributor keyDistributor) throws IOException {
        Scan[] scans = keyDistributor.getDistributedScans(originalScan);
        applyScanOptions(originalScan, scans);
        return scans;
    }

    private static void applyScanOptions(Scan originalScan, Scan[] scans) {
        for (int i = 0; i < scans.length; i++) {
            Scan scan = scans[i];
            scan.setId(originalScan.getId() + "-" + i);
        }
    }


    public static ResultScanner[] newScanners(AsyncTable<?> table, Scan[] scans) {
        return newScanners(table::getScanner, scans);
    }

    public static ResultScanner[] newScanners(Table table, Scan[] scans) {
        return newScanners(getScanner(table), scans);
    }

    private static Function<Scan, ResultScanner> getScanner(Table table) {
        return (scan) -> {
            try {
                return table.getScanner(scan);
            } catch (IOException e) {
                throw new HbaseSystemException(e);
            }
        };
    }

    private static ResultScanner[] newScanners(Function<Scan, ResultScanner> table, Scan[] scans) {
        final int scansLength = scans.length;
        ResultScanner[] scanners = new ResultScanner[scansLength];
        boolean success = false;
        try {
            for (int i = 0; i < scansLength; i++) {
                scanners[i] = table.apply(scans[i]);
            }
            success = true;
        } finally {
            if (!success) {
                closeScanner(scanners);
            }
        }
        return scanners;
    }

    public static void closeScanner(ResultScanner[] scannerList) {
        closeScanner(scannerList, 0);
    }

    public static void closeScanner(ResultScanner[] scannerList, int startOffset) {
        for (int i = startOffset; i < scannerList.length; i++) {
            ResultScanner scanner = scannerList[i];
            if (scanner != null) {
                try {
                    scanner.close();
                } catch (Throwable ignore) {
                    // ignore
                }
            }
        }
    }


}
