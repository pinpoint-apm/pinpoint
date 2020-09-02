/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.trace;

import com.navercorp.pinpoint.common.trace.TraceMetadataProvider;
import com.navercorp.pinpoint.loader.plugins.PinpointPluginLoader;
import com.navercorp.pinpoint.loader.plugins.trace.TraceMetadataProviderLoader;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * @author HyunGil Jeong
 */
public class TraceMetadataVerificationTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final PinpointPluginLoader<TraceMetadataProvider> traceMetadataProviderLoader = new TraceMetadataProviderLoader();

    @Rule
    public ErrorCollector collector = new ErrorCollector();

    @Test
    public void checkTraceMetadata() throws IOException {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();

        List<TraceMetadataProvider> traceMetadataProviders = traceMetadataProviderLoader.load(classLoader);
        TraceMetadataVerifier verifier = new TraceMetadataVerifier();
        for (TraceMetadataProvider traceMetadataProvider : traceMetadataProviders) {
            traceMetadataProvider.setup(verifier.getTraceMetadataSetupContext());
        }
        verifier.verifyServiceTypes(collector);
        verifier.verifyAnnotationKeys(collector);
    }
}
