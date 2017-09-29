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

package com.navercorp.pinpoint.common.util;

/**
 * @author emeroad
 */
public final class CpuUtils {

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int WORKER_COUNT = CPU_COUNT * 2;

    private CpuUtils() {
    }

    public static int cpuCount() {
        return CPU_COUNT;
    }

    public static int workerCount() {
        return WORKER_COUNT;
    }
}
