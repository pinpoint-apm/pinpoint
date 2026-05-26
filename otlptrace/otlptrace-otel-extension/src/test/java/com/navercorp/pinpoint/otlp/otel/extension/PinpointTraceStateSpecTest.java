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

package com.navercorp.pinpoint.otlp.otel.extension;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PinpointTraceStateSpecTest {

    // Cross-checked against the collector-side OtlpTraceConstants. If either side
    // changes these strings without updating the other, the corresponding test fails.
    // Collector counterpart: OtlpTraceConstants.TRACESTATE_KEY_PINPOINT et al.
    @Test
    void wireFormatConstants_areStable() {
        assertThat(PinpointTraceStateSpec.KEY).isEqualTo("pp");
        assertThat(PinpointTraceStateSpec.SUBKEY_SVC).isEqualTo("svc");
        assertThat(PinpointTraceStateSpec.SUBKEY_APP).isEqualTo("app");
        assertThat(PinpointTraceStateSpec.SUBKEY_TYPE).isEqualTo("type");
    }

    @Test
    void buildValue_allThree() {
        assertThat(PinpointTraceStateSpec.buildValue("my-svc", "my-app", 1010))
                .isEqualTo("svc:my-svc;app:my-app;type:1010");
    }

    @Test
    void buildValue_svcAndApp_noType() {
        assertThat(PinpointTraceStateSpec.buildValue("my-svc", "my-app", null))
                .isEqualTo("svc:my-svc;app:my-app");
    }

    @Test
    void buildValue_appOnly() {
        assertThat(PinpointTraceStateSpec.buildValue(null, "my-app", null))
                .isEqualTo("app:my-app");
    }

    @Test
    void buildValue_svcOnly() {
        assertThat(PinpointTraceStateSpec.buildValue("my-svc", null, null))
                .isEqualTo("svc:my-svc");
    }

    @Test
    void buildValue_appAndType_noSvc() {
        assertThat(PinpointTraceStateSpec.buildValue(null, "my-app", 1010))
                .isEqualTo("app:my-app;type:1010");
    }

    @Test
    void buildValue_neitherSvcNorApp_returnsNull() {
        // Without svc or app the collector ignores the entry — there's no point emitting.
        assertThat(PinpointTraceStateSpec.buildValue(null, null, 1010)).isNull();
        assertThat(PinpointTraceStateSpec.buildValue("", "", 1010)).isNull();
    }

    @Test
    void buildValue_emptyStringsTreatedAsNull() {
        assertThat(PinpointTraceStateSpec.buildValue("", "my-app", null))
                .isEqualTo("app:my-app");
        assertThat(PinpointTraceStateSpec.buildValue("my-svc", "", null))
                .isEqualTo("svc:my-svc");
    }
}
