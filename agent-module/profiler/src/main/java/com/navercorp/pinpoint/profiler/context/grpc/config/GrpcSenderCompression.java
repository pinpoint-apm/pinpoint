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
package com.navercorp.pinpoint.profiler.context.grpc.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Message compression of the agent's gRPC senders: the span batch sender ({@code SendSpanBatch},
 * {@code profiler.transport.grpc.span.batch-sender.compression}) and the stat stream
 * ({@code SendAgentStat}, {@code profiler.transport.grpc.stat.sender.compression}).
 */
public enum GrpcSenderCompression {
    NONE(null),
    GZIP("gzip");

    private final String grpcName;

    GrpcSenderCompression(String grpcName) {
        this.grpcName = grpcName;
    }

    /**
     * The gRPC compressor name for {@code CallOptions#withCompression}, or {@code null} for no compression.
     */
    public String getGrpcName() {
        return grpcName;
    }

    /**
     * {@link #GZIP}: a configuration file kept from an older agent has no compression key, and must get the
     * same behaviour as a new one. Every collector accepts gzip, and both senders fall back to uncompressed
     * when one does not.
     */
    public static GrpcSenderCompression defaultCompression() {
        return GZIP;
    }

    /**
     * Case-insensitive. A missing or blank value is the default ({@link #defaultCompression()}). An unknown
     * value is also the default, with a warning: compression is an optional optimization, so a typo must not
     * keep the agent (and the application) from starting.
     */
    public static GrpcSenderCompression fromValue(String value) {
        if (value == null) {
            return defaultCompression();
        }
        final String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return defaultCompression();
        }
        for (GrpcSenderCompression compression : values()) {
            if (compression.name().equalsIgnoreCase(trimmed)) {
                return compression;
            }
        }
        final Logger logger = LogManager.getLogger(GrpcSenderCompression.class);
        logger.warn("Unknown gRPC sender compression '{}', using {}. Supported values: none, gzip", value, defaultCompression());
        return defaultCompression();
    }
}
