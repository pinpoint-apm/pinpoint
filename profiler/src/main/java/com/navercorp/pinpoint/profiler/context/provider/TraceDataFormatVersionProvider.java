/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.provider;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.TransportModule;
import com.navercorp.pinpoint.profiler.context.TraceDataFormatVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Woonduk Kang(emeroad)
 */
public class TraceDataFormatVersionProvider implements Provider<TraceDataFormatVersion> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final TraceDataFormatVersion version;

    @Inject
    public TraceDataFormatVersionProvider(ProfilerConfig profilerConfig) {
        this.version = getVersion(profilerConfig);
        logger.info("TraceDataFormatVersion:{}", this.version);
    }

    private TraceDataFormatVersion getVersion(ProfilerConfig profilerConfig) {
        final TransportModule transportModule = profilerConfig.getTransportModule();
        logger.info("TransportModule:{}", transportModule);
        if (TransportModule.THRIFT == transportModule) {
            final String version = profilerConfig.readString(TraceDataFormatVersion.THRIFT_TRACE_VERSION_KEY, "v1");
            if ("v1".equalsIgnoreCase(version)) {
                return TraceDataFormatVersion.V1;
            }
            throw new UnsupportedOperationException("unknown " + TraceDataFormatVersion.THRIFT_TRACE_VERSION_KEY + ":" + version);
        }


        if (TransportModule.GRPC == transportModule) {
            final String version = profilerConfig.readString(TraceDataFormatVersion.GRPC_TRACE_VERSION_KEY, "v2");
            if ("v2".equalsIgnoreCase(version)) {
                return TraceDataFormatVersion.V2;
            }
            throw new UnsupportedOperationException("unknown " + TraceDataFormatVersion.GRPC_TRACE_VERSION_KEY + ":" + version);
        }

        throw new UnsupportedOperationException("unknown transportModule:" + transportModule);
    }

    @Override
    public TraceDataFormatVersion get() {
        return version;
    }
}
