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

package com.navercorp.pinpoint.otlp.trace.collector;

import com.navercorp.pinpoint.exceptiontrace.collector.ExceptionTraceCollectorConfig;
import com.navercorp.pinpoint.uristat.collector.UriStatCollectorConfig;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

class OtlpTraceCollectorModuleTest {

    /**
     * The OTLPTRACE app is its own Spring context, so every storage module its export path writes
     * to must be imported here explicitly — the BASIC app gets them from PinpointCollectorStarter.
     * Dropping one of these does not fail the build or the boot (the services are injected as
     * Optional / conditional beans); it silently stops storing that data.
     */
    @Test
    void module_importsTheStorageConfigsTheExportPathWritesTo() {
        Import imports = OtlpTraceCollectorModule.class.getAnnotation(Import.class);

        assertThat(imports).isNotNull();
        assertThat(imports.value())
                .contains(UriStatCollectorConfig.class, ExceptionTraceCollectorConfig.class);
    }
}
