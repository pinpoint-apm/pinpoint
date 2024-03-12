/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.common.hbase;

import com.navercorp.pinpoint.common.hbase.scan.DefaultScanner;
import com.navercorp.pinpoint.common.hbase.scan.ResultScannerSupplier;
import com.navercorp.pinpoint.common.hbase.scan.Scanner;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.metrics.ScanMetrics;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * @author emeroad
 * @author minwoo.jung
 */
@ExtendWith(MockitoExtension.class)
public class ResultScannerFactoryTest {

    @Mock
    ResultScanner scanner1;
    @Mock
    ResultScanner scanner2;
    @Mock
    ResultScanner scanner3;

    @Test
    public void extractData_close() {
        ResultsExtractor<Integer> action = new ResultsExtractor<>() {
            private int i;
            @Override
            public Integer extractData(ResultScanner results) throws Exception {
                return i++;
            }
        };

        ResultScannerSupplier[] suppliers = {
                new DefaultResultScannerSupplier(scanner1),
                new DefaultResultScannerSupplier(scanner2),
                new DefaultResultScannerSupplier(scanner3)
        };

        Scanner<Integer> scanner = new DefaultScanner<>(suppliers, 1024);
        scanner.extractData(action);

        verify(scanner1).close();
        verify(scanner2).close();
        verify(scanner3).close();
    }

    @Test
    public void extractData_error() throws IOException {
        ResultsExtractor<Integer> action = new ResultsExtractor<>() {
            private int i;
            @Override
            public Integer extractData(ResultScanner results) throws Exception {
                if (i >= 1) {
                    throw new Exception("error:" + i);
                }
                results.next(); // touch

                return i++;
            }
        };
        ResultScannerSupplier[] suppliers = {
                new DefaultResultScannerSupplier(scanner1),
                new DefaultResultScannerSupplier(scanner2),
                new DefaultResultScannerSupplier(scanner3)
        };

        HbaseSystemException error = Assertions.assertThrowsExactly(HbaseSystemException.class, () -> {
            Scanner<Integer> scanner = new DefaultScanner<>(suppliers, 1024);
            scanner.extractData(action);
        });

        verify(scanner1, atLeast(1)).close();
        verify(scanner2, atLeast(1)).close();
        verify(scanner3, atLeast(1)).close();

        verify(scanner1).next();
        verify(scanner2, never()).next();
        verify(scanner3, never()).next();

        Assertions.assertEquals("error:1", error.getCause().getMessage());
    }

    public static class DefaultResultScannerSupplier implements ResultScannerSupplier {
        private final ResultScanner scanner;

        public DefaultResultScannerSupplier(ResultScanner scanner) {
            this.scanner = scanner;
        }

        @Override
        public ResultScanner getScanner() {
            return scanner;
        }

        @Override
        public void close() throws Exception {
            scanner.close();
        }

        @Override
        public ScanMetrics getScanMetrics() {
            return scanner.getScanMetrics();
        }
    };
}
