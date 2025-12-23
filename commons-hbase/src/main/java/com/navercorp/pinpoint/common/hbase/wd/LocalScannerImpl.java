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

package com.navercorp.pinpoint.common.hbase.wd;

import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.metrics.ScanMetrics;

import java.io.Closeable;
import java.io.IOException;
import java.util.Objects;

/**
 * A local wrapper around a {@link ResultScanner} that provides single-result buffering
 * and tracks when the underlying scanner has been exhausted.
 * <p>
 * Intended for use by {@code DistributedScanner} to manage per-scanner state, allowing
 * callers to peek at the next {@link Result} and query exhaustion state before
 * consuming it.
 */
public class LocalScannerImpl implements Closeable, LocalScanner {

    private final ResultScanner scanner;

    private boolean exhausted = false;

    private Result localBuffer;

    public LocalScannerImpl(ResultScanner scanner) {
        this.scanner = Objects.requireNonNull(scanner, "scanner");
    }

    @Override
    public Result next() throws IOException {
        if (exhausted) {
            throw new IllegalStateException("Scanner is exhausted, cannot call next() again");
        }
        if (localBuffer != null) {
            return localBuffer;
        }
        Result next = scanner.next();
        if (next == null) {
            exhausted = true;
        }
        localBuffer = next;
        return localBuffer;
    }

    @Override
    public boolean isExhausted() {
        return exhausted;
    }

    @Override
    public void consume() {
        // clear buffer to allow fetching next result
        localBuffer = null;
    }

    @Override
    public void close() throws IOException {
        scanner.close();
    }

    @Override
    public ScanMetrics getScanMetrics() {
        return scanner.getScanMetrics();
    }
}
