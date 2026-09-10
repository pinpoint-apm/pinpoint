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

package com.navercorp.pinpoint.otlp.log.collector.service;

import com.navercorp.pinpoint.otlp.log.collector.OtlpLogRejectedRecords;

/**
 * Transport-agnostic outcome of an OTLP logs export. Unlike the trace path there is no retryable
 * server-error class: the only store is the exceptiontrace Kafka producer, whose failures are
 * counted ({@code collector.otlplog.store.error}) rather than turned into a whole-batch retry, so a
 * response is always a success or an OTLP partial success.
 *
 * @param stored   records handed to storage
 * @param rejected records not stored, by reason (client-visible ones go into the partial success)
 */
public record OtlpLogExportResult(int stored, OtlpLogRejectedRecords rejected) {
}
