/*
 * Copyright 2017 NAVER Corp.
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
import com.navercorp.pinpoint.common.server.bo.serializer.stat.AgentStatUtils;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.ApplicationStatDecodingContext;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinMemoryBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinStatBo;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author minwoo.jung
 */
public class MemoryCodecTest {

    @Test
    public void encodeAndDecodeTest() throws Exception {
        final String id = "test_app";
        final long currentTime = new Date().getTime();
        final AgentStatDataPointCodec agentStatDataPointCodec = new AgentStatDataPointCodec();
        final MemoryCodec memoryCodec = new MemoryCodec(agentStatDataPointCodec);
        final Buffer encodedValueBuffer = new AutomaticBuffer();
        final List<JoinStatBo> joinMemoryBoList = createJoinMemoryBoList(currentTime);
        encodedValueBuffer.putByte(memoryCodec.getVersion());
        memoryCodec.encodeValues(encodedValueBuffer, joinMemoryBoList);

        final Buffer valueBuffer = new FixedBuffer(encodedValueBuffer.getBuffer());;
        final long baseTimestamp = AgentStatUtils.getBaseTimestamp(currentTime);
        final long timestampDelta = currentTime - baseTimestamp;
        final ApplicationStatDecodingContext decodingContext = new ApplicationStatDecodingContext();
        decodingContext.setApplicationId(id);
        decodingContext.setBaseTimestamp(baseTimestamp);
        decodingContext.setTimestampDelta(timestampDelta);

        assertEquals(valueBuffer.readByte(), memoryCodec.getVersion());
        List<JoinStatBo> decodedJoinMemoryBoList = memoryCodec.decodeValues(valueBuffer, decodingContext);
        for (int i = 0; i < decodedJoinMemoryBoList.size(); i++) {
            assertTrue(decodedJoinMemoryBoList.get(i).equals(joinMemoryBoList.get(i)));
        }
    }

    private List<JoinStatBo> createJoinMemoryBoList(long currentTime) {
        final String id = "test_app";
        List<JoinStatBo> joinMemoryBoList = new ArrayList<JoinStatBo>();
        JoinMemoryBo joinMemoryBo1 = new JoinMemoryBo(id, currentTime, 3000, 2000, 5000, "app_1_1", "app_1_2", 500, 50, 600, "app_1_3", "app_1_4");
        JoinMemoryBo joinMemoryBo2 = new JoinMemoryBo(id, currentTime + 5000, 4000, 1000, 7000, "app_2_1", "app_2_2", 400, 150, 600, "app_2_3", "app_2_4");
        JoinMemoryBo joinMemoryBo3 = new JoinMemoryBo(id, currentTime + 10000, 5000, 3000, 8000, "app_3_1", "app_3_2", 200, 100, 200, "app_3_3", "app_3_4");
        JoinMemoryBo joinMemoryBo4 = new JoinMemoryBo(id, currentTime + 15000, 1000, 100, 3000, "app_4_1", "app_4_2", 100, 900, 1000, "app_4_3", "app_4_4");
        JoinMemoryBo joinMemoryBo5 = new JoinMemoryBo(id, currentTime + 20000, 2000, 1000, 6000, "app_5_1", "app_5_2", 300, 100, 2900, "app_5_3", "app_5_4");
        joinMemoryBoList.add(joinMemoryBo1);
        joinMemoryBoList.add(joinMemoryBo3);
        joinMemoryBoList.add(joinMemoryBo2);
        joinMemoryBoList.add(joinMemoryBo5);
        joinMemoryBoList.add(joinMemoryBo4);

        return joinMemoryBoList;
    }

}