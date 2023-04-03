/*
 * Copyright 2020 Naver Corp.
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

package com.navercorp.pinpoint.common.server.bo.codec.stat.join;

import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.FixedBuffer;
import com.navercorp.pinpoint.common.server.bo.codec.stat.AgentStatDataPointCodec;
import com.navercorp.pinpoint.common.server.bo.codec.stat.ApplicationStatCodec;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.AgentStatUtils;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.ApplicationStatDecodingContext;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinLoadedClassBo;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LoadedClassCountCodecTest {

    @Test
    public void encodeAndDecodeTest() {
        final String id = "test_app";
        final long currentTime = new Date().getTime();
        final AgentStatDataPointCodec agentStatDataPointCodec = new AgentStatDataPointCodec();
        final ApplicationStatCodec<JoinLoadedClassBo> loadedClassCodec = new LoadedClassCodec(agentStatDataPointCodec);
        final Buffer encodedValueBuffer = new AutomaticBuffer();
        final List<JoinLoadedClassBo> joinLoadedClassBoList = createJoinLoadedClassCountBoList(currentTime);
        encodedValueBuffer.putByte(loadedClassCodec.getVersion());
        loadedClassCodec.encodeValues(encodedValueBuffer, joinLoadedClassBoList);

        final Buffer valueBuffer = new FixedBuffer(encodedValueBuffer.getBuffer());
        final long baseTimestamp = AgentStatUtils.getBaseTimestamp(currentTime);
        final long timestampDelta = currentTime - baseTimestamp;
        final ApplicationStatDecodingContext decodingContext = new ApplicationStatDecodingContext();
        decodingContext.setApplicationId(id);
        decodingContext.setBaseTimestamp(baseTimestamp);
        decodingContext.setTimestampDelta(timestampDelta);

        assertEquals(valueBuffer.readByte(), loadedClassCodec.getVersion());
        List<JoinLoadedClassBo> decodedJoinLoadedClassBoList = loadedClassCodec.decodeValues(valueBuffer, decodingContext);
        for (int i = 0; i < decodedJoinLoadedClassBoList.size(); i++) {
            assertEquals(decodedJoinLoadedClassBoList.get(i), joinLoadedClassBoList.get(i));
        }
    }

    private List<JoinLoadedClassBo> createJoinLoadedClassCountBoList(long currentTime) {
        final String id = "test_app";
        return List.of(
                new JoinLoadedClassBo(id, 80, 900, "agent2_1", 20, "agent2_2", 70, 900, "agent2_1", 20, "agent2_2", currentTime),
                new JoinLoadedClassBo(id, 70, 900, "agent2_1", 20, "agent2_2", 70, 900, "agent2_1", 20, "agent2_2", currentTime + 5000),
                new JoinLoadedClassBo(id, 60, 800, "agent4_1", 15, "agent4_2", 60, 800, "agent4_1", 15, "agent4_2", currentTime + 15000),
                new JoinLoadedClassBo(id, 50, 700, "agent3_1", 10, "agent3_2", 50, 700, "agent3_1", 10, "agent3_2", currentTime + 10000),
                new JoinLoadedClassBo(id, 40, 600, "agent5_1", 5, "agent5_2", 40, 600, "agent5_1", 5, "agent5_2", currentTime + 20000)
        );
    }
}
