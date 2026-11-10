/*
 * Copyright 2026 NAVER Corp.
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

package com.navercorp.pinpoint.otlp.trace.inspector;

/**
 * What one histogram series contributed during one export interval: the observations, their sum
 * (in the metric's unit) and the maximum, exact when the point carried a usable {@code max}, else
 * the upper bound of the highest bucket that grew.
 */
public record HistogramDelta(long count, double sum, double max) {

    public static final HistogramDelta EMPTY = new HistogramDelta(0, 0, -1);
}
