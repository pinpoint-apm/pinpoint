/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.common.trace;

import com.navercorp.pinpoint.common.util.Assert;

/**
 * @author HyunGil Jeong
 */
public class LoadedTraceMetadataProvider implements TraceMetadataProvider {

    private final TraceMetadataProvider traceMetadataProvider;

    public LoadedTraceMetadataProvider(TraceMetadataProvider traceMetadataProvider) {
        this.traceMetadataProvider = Assert.requireNonNull(traceMetadataProvider, "traceMetadataProvider");
    }


    @Override
    public void setup(TraceMetadataSetupContext context) {
        traceMetadataProvider.setup(context);
    }

    @Override
    public String toString() {
        return traceMetadataProvider.getClass().getName();
    }
}
