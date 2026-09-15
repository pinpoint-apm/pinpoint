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

package com.navercorp.pinpoint.web.vo;

/**
 *
 * @author netspider
 * @author emeroad
 * @param <V>
 * @param limitedTime the oldest point the scan actually reached; the resume cursor
 * @param scanData    rows collected
 * @param truncated   true when the scan gave up before reaching the requested range start
 *                    (e.g. a scan budget ran out), so {@code scanData} is partial even
 *                    though it holds fewer rows than the requested limit
 */
public record LimitedScanResult<V>(long limitedTime, V scanData, boolean truncated) {

    public LimitedScanResult(long limitedTime, V scanData) {
        this(limitedTime, scanData, false);
    }
}
