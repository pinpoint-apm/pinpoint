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

package com.navercorp.pinpoint.common.server.bo.codec.stat.join;

import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.FixedBuffer;
import com.navercorp.pinpoint.common.server.bo.codec.stat.AgentStatDataPointCodec;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.AgentStatUtils;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.ApplicationStatDecodingContext;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinDirectBufferBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinStatBo;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Roy Kim
 */
public class DirectBufferCodecTest {

    @Test
    public void encodeAndDecodeTest(){
        final String id = "test_app";
        final long currentTime = new Date().getTime();
        final AgentStatDataPointCodec agentStatDataPointCodec = new AgentStatDataPointCodec();
        final DirectBufferCodec directBufferCodec = new DirectBufferCodec(agentStatDataPointCodec);
        final Buffer encodedValueBuffer = new AutomaticBuffer();
        final List<JoinStatBo> joinDirectBufferBoList = createJoinDirectBufferBoList(currentTime);
        encodedValueBuffer.putByte(directBufferCodec.getVersion());
        directBufferCodec.encodeValues(encodedValueBuffer, joinDirectBufferBoList);

        final Buffer valueBuffer = new FixedBuffer(encodedValueBuffer.getBuffer());;
        final long baseTimestamp = AgentStatUtils.getBaseTimestamp(currentTime);
        final long timestampDelta = currentTime - baseTimestamp;
        final ApplicationStatDecodingContext decodingContext = new ApplicationStatDecodingContext();
        decodingContext.setApplicationId(id);
        decodingContext.setBaseTimestamp(baseTimestamp);
        decodingContext.setTimestampDelta(timestampDelta);

        assertEquals(valueBuffer.readByte(), directBufferCodec.getVersion());
        List<JoinStatBo> decodedJoinDirectBufferBoList = directBufferCodec.decodeValues(valueBuffer, decodingContext);
        for (int i = 0; i < decodedJoinDirectBufferBoList.size(); i++) {
            assertTrue(decodedJoinDirectBufferBoList.get(i).equals(joinDirectBufferBoList.get(i)));
        }
    }

    private List<JoinStatBo> createJoinDirectBufferBoList(long currentTime) {
        final String id = "test_app";
        final List<JoinStatBo> joinDirectBufferBoList = new ArrayList();
        JoinDirectBufferBo joinDirectBufferBo1 = new JoinDirectBufferBo(id, 80, 1000, "agent1_1", 30, "agent1_2", 80, 1000, "agent1_1", 30, "agent1_2", 80, 1000, "agent1_1", 30, "agent1_2", 80, 1000, "agent1_1", 30, "agent1_2", currentTime);
        JoinDirectBufferBo joinDirectBufferBo2 = new JoinDirectBufferBo(id, 70, 900, "agent2_1", 20, "agent2_2", 70, 900, "agent2_1", 20, "agent2_2", 70, 900, "agent2_1", 20, "agent2_2", 70, 900, "agent2_1", 20, "agent2_2", currentTime + 5000);
        JoinDirectBufferBo joinDirectBufferBo4 = new JoinDirectBufferBo(id, 60, 800, "agent4_1", 15, "agent4_2", 60, 800, "agent4_1", 15, "agent4_2", 60, 800, "agent4_1", 15, "agent4_2", 60, 800, "agent4_1", 15, "agent4_2",  currentTime + 15000);
        JoinDirectBufferBo joinDirectBufferBo3 = new JoinDirectBufferBo(id, 50, 700, "agent3_1", 10, "agent3_2", 50, 700, "agent3_1", 10, "agent3_2", 50, 700, "agent3_1", 10, "agent3_2", 50, 700, "agent3_1", 10, "agent3_2", currentTime + 10000);
        JoinDirectBufferBo joinDirectBufferBo5 = new JoinDirectBufferBo(id, 40, 600, "agent5_1", 5, "agent5_2", 40, 600, "agent5_1", 5, "agent5_2", 40, 600, "agent5_1", 5, "agent5_2", 40, 600, "agent5_1", 5, "agent5_2", currentTime + 20000);
        joinDirectBufferBoList.add(joinDirectBufferBo1);
        joinDirectBufferBoList.add(joinDirectBufferBo2);
        joinDirectBufferBoList.add(joinDirectBufferBo3);
        joinDirectBufferBoList.add(joinDirectBufferBo4);
        joinDirectBufferBoList.add(joinDirectBufferBo5);
        return joinDirectBufferBoList;
    }

}