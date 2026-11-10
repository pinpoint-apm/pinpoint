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

import com.navercorp.pinpoint.common.server.bo.JvmGcType;

/**
 * @param old true for the collector the agent's {@code jvmGc} chart counts ({@code gcOldCount/Time}):
 *            the old/major/full pauses; false for a collector the chart leaves out (young/minor
 *            pauses, concurrent cycles, cycle counters)
 */
public record GcClassification(JvmGcType gcType, boolean old) {
}
