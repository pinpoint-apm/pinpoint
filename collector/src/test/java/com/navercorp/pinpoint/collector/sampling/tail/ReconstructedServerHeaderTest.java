/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.collector.sampling.tail;

import com.navercorp.pinpoint.common.server.io.ServerHeader;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class ReconstructedServerHeaderTest {

    @Test
    void exposesStoredFields() {
        ServerHeader header = new ReconstructedServerHeader("agent-1", "agent-name", "app-1", 1000L);
        assertThat(header.getAgentId()).isEqualTo("agent-1");
        assertThat(header.getAgentName()).isEqualTo("agent-name");
        assertThat(header.getApplicationName()).isEqualTo("app-1");
        assertThat(header.getAgentStartTime()).isEqualTo(1000L);
        assertThat(header.getServiceUid().get()).isEqualTo(ServiceUid.DEFAULT);
        assertThat(header.isGrpcBuiltInRetry()).isFalse();
    }
}
