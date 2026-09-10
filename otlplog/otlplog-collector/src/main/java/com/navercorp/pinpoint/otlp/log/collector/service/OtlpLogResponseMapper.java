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
import io.opentelemetry.proto.collector.logs.v1.ExportLogsPartialSuccess;
import io.opentelemetry.proto.collector.logs.v1.ExportLogsServiceResponse;

/**
 * Maps an {@link OtlpLogExportResult} to the OTLP response shared by the gRPC and HTTP transports.
 * Only client-visible drops (invalid resource, no trace context, no exception type, mapping error)
 * become {@code rejected_log_records}; records the receiver discards by design (non-exception logs,
 * blacklist, duplicates, unsampled) are a clean success from the exporter's point of view, per the
 * OTLP rule that a server may drop data it does not want without reporting it as rejected.
 */
public final class OtlpLogResponseMapper {

    private OtlpLogResponseMapper() {
    }

    public static ExportLogsServiceResponse toResponse(OtlpLogExportResult result) {
        final OtlpLogRejectedRecords rejected = result.rejected();
        final long clientVisible = rejected.clientVisibleCount();
        if (clientVisible > 0) {
            final ExportLogsPartialSuccess partialSuccess = ExportLogsPartialSuccess.newBuilder()
                    .setRejectedLogRecords(clientVisible)
                    .setErrorMessage(rejected.clientVisibleMessage())
                    .build();
            return ExportLogsServiceResponse.newBuilder().setPartialSuccess(partialSuccess).build();
        }
        return ExportLogsServiceResponse.getDefaultInstance();
    }
}
