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

package com.navercorp.pinpoint.common.hbase.config;

import com.navercorp.pinpoint.common.util.CpuUtils;

public class ParallelScan {

    public static final int DEFAULT_MAX_CONCURRENT_ASYNC_SCANNER = 256;

    private int maxThreads = CpuUtils.workerCount() * 4;
    private int maxThreadsPerParallelScan = CpuUtils.workerCount();
    private int maxConcurrentAsyncScanner = DEFAULT_MAX_CONCURRENT_ASYNC_SCANNER;


    public ParallelScan() {
    }

    public int getMaxThreads() {
        return maxThreads;
    }

    public void setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
    }

    public int getMaxThreadsPerParallelScan() {
        return maxThreadsPerParallelScan;
    }

    public void setMaxThreadsPerParallelScan(int maxThreadsPerParallelScan) {
        this.maxThreadsPerParallelScan = maxThreadsPerParallelScan;
    }


    public int getMaxConcurrentAsyncScanner() {
        return this.maxConcurrentAsyncScanner;
    }

    public void setMaxConcurrentAsyncScanner(int maxConcurrentAsyncScanner) {
        this.maxConcurrentAsyncScanner = maxConcurrentAsyncScanner;
    }

    @Override
    public String toString() {
        return "ParallelScan{" +
                "maxThreads=" + maxThreads +
                ", maxThreadsPerParallelScan=" + maxThreadsPerParallelScan +
                ", maxConcurrentAsyncScanner=" + maxConcurrentAsyncScanner +
                '}';
    }
}
