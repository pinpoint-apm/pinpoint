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

package com.navercorp.pinpoint.otlp.log.collector.mapper;

import com.navercorp.pinpoint.otlp.trace.collector.mapper.IdAndName;
import io.opentelemetry.proto.logs.v1.LogRecord;

/**
 * An exception LogRecord that passed selection, together with the resource-level context it needs
 * for mapping.
 *
 * @param idAndName   the (service, application, agent) the resource resolved to
 * @param sdkLanguage {@code telemetry.sdk.language} of the resource, selects the stacktrace parser; may be null
 * @param record      the LogRecord (valid {@code trace_id}/{@code span_id} guaranteed by the caller)
 * @param unsampled   whether the record's trace flags say the trace was not sampled
 */
public record ExceptionLogCandidate(IdAndName idAndName, String sdkLanguage, LogRecord record, boolean unsampled) {
}
