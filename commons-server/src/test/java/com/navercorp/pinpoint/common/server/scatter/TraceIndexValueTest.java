/*
 * Copyright 2026 NAVER Corp.
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

package com.navercorp.pinpoint.common.server.scatter;

import com.navercorp.pinpoint.common.server.trace.OtelServerTraceId;
import com.navercorp.pinpoint.common.server.trace.PinpointServerTraceId;
import com.navercorp.pinpoint.common.server.trace.ServerTraceId;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class TraceIndexValueTest {

    @Test
    public void IndexEncodeDecodeTest() {
        String agentId = "agentId";
        int elapsed = 1234;
        int errorCode = 0;

        byte[] encoded = TraceIndexValue.Index.encode(agentId, elapsed, errorCode);
        TraceIndexValue.Index index = TraceIndexValue.Index.decode(encoded, 0, encoded.length);

        Assertions.assertThat(index.agentId()).isEqualTo(agentId);
        Assertions.assertThat(index.elapsed()).isEqualTo(elapsed);
        Assertions.assertThat(index.errorCode()).isEqualTo(errorCode);
    }

    @Test
    public void MetaEncodeDecodeTest1() {
        ServerTraceId serverTraceId = new PinpointServerTraceId("rootAgentId", 123456789L, 1);
        long startTime = 123456789L;
        String remoteAddr = "1.2.3.4";
        String endpoint = "host:8080";
        String agentName = "agentName";

        byte[] encoded = TraceIndexValue.Meta.encode(serverTraceId, startTime, remoteAddr, endpoint, agentName);
        TraceIndexValue.Meta meta = TraceIndexValue.Meta.decode(encoded, 0, encoded.length);

        Assertions.assertThat(meta.serverTraceId()).isEqualTo(serverTraceId);
        Assertions.assertThat(meta.startTime()).isEqualTo(startTime);
        Assertions.assertThat(meta.remoteAddr()).isEqualTo(remoteAddr);
        Assertions.assertThat(meta.endpoint()).isEqualTo(endpoint);
        Assertions.assertThat(meta.agentName()).isEqualTo(agentName);
    }

    @Test
    public void MetaEncodeDecodeTest2() {
        byte[] byte16 = new byte[16];
        Arrays.fill(byte16, (byte) 2);
        ServerTraceId serverTraceId = new OtelServerTraceId(byte16);
        long startTime = 123456789L;
        String remoteAddr = "1.2.3.4";
        String endpoint = "host:8080";
        String agentName = "agentName";

        byte[] encoded = TraceIndexValue.Meta.encode(serverTraceId, startTime, remoteAddr, endpoint, agentName);
        TraceIndexValue.Meta meta = TraceIndexValue.Meta.decode(encoded, 0, encoded.length);

        Assertions.assertThat(meta.serverTraceId()).isEqualTo(serverTraceId);
        Assertions.assertThat(meta.startTime()).isEqualTo(startTime);
        Assertions.assertThat(meta.remoteAddr()).isEqualTo(remoteAddr);
        Assertions.assertThat(meta.endpoint()).isEqualTo(endpoint);
        Assertions.assertThat(meta.agentName()).isEqualTo(agentName);
    }

    @Test
    public void MetaRpcEncodeDecodeTest() {
        String rpc = "/api/test";

        byte[] encoded = TraceIndexValue.MetaRpc.encode(rpc);
        TraceIndexValue.MetaRpc metaRpc = TraceIndexValue.MetaRpc.decode(encoded, 0, encoded.length);

        Assertions.assertThat(metaRpc.rpc()).isEqualTo(rpc);
    }
}
