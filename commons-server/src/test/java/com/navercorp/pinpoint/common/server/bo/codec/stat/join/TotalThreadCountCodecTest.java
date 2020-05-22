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
import com.navercorp.pinpoint.common.server.bo.serializer.stat.AgentStatUtils;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.ApplicationStatDecodingContext;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinTotalThreadCountBo;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TotalThreadCountCodecTest {

    @Test
    public void encodeAndDecodeTest(){
        final String id = "test_app";
        final long currentTime = new Date().getTime();
        final AgentStatDataPointCodec agentStatDataPointCodec = new AgentStatDataPointCodec();
        final TotalThreadCountCodec totalThreadCountCodec = new TotalThreadCountCodec(agentStatDataPointCodec);
        final Buffer encodedValueBuffer = new AutomaticBuffer();
        final List<JoinStatBo> joinTotalThreadCountBoList = createJoinTotalThreadCountBoList(currentTime);
        encodedValueBuffer.putByte(totalThreadCountCodec.getVersion());
        totalThreadCountCodec.encodeValues(encodedValueBuffer, joinTotalThreadCountBoList);

        final Buffer valueBuffer = new FixedBuffer(encodedValueBuffer.getBuffer());;
        final long baseTimestamp = AgentStatUtils.getBaseTimestamp(currentTime);
        final long timestampDelta = currentTime - baseTimestamp;
        final ApplicationStatDecodingContext decodingContext = new ApplicationStatDecodingContext();
        decodingContext.setApplicationId(id);
        decodingContext.setBaseTimestamp(baseTimestamp);
        decodingContext.setTimestampDelta(timestampDelta);

        assertEquals(valueBuffer.readByte(), totalThreadCountCodec.getVersion());
        List<JoinStatBo> decodedJoinTotalThreadCountBoList = totalThreadCountCodec.decodeValues(valueBuffer, decodingContext);
        for (int i = 0; i < decodedJoinTotalThreadCountBoList.size(); i++) {
            assertTrue(decodedJoinTotalThreadCountBoList.get(i).equals(joinTotalThreadCountBoList.get(i)));
        }
    }

    private List<JoinStatBo> createJoinTotalThreadCountBoList(long currentTime) {
        final String id = "test_app";
        final List<JoinStatBo> joinTotalThreadCountBoList = new ArrayList();
        JoinTotalThreadCountBo joinTotalThreadCountBo1 = new JoinTotalThreadCountBo(id, currentTime, 80, 1000, "agent1_1", 30, "agent1_2");
        JoinTotalThreadCountBo joinTotalThreadCountBo2 = new JoinTotalThreadCountBo(id, currentTime+5000, 70, 900, "agent2_1", 20, "agent2_2");
        JoinTotalThreadCountBo joinTotalThreadCountBo3 = new JoinTotalThreadCountBo(id, currentTime+15000, 60, 800, "agent4_1", 15, "agent4_2");
        JoinTotalThreadCountBo joinTotalThreadCountBo4 = new JoinTotalThreadCountBo(id, currentTime+10000, 50, 700, "agent3_1", 10, "agent3_2");
        JoinTotalThreadCountBo joinTotalThreadCountBo5 = new JoinTotalThreadCountBo(id, currentTime+20000, 40, 600, "agent5_1", 5, "agent5_2");
        joinTotalThreadCountBoList.add(joinTotalThreadCountBo1);
        joinTotalThreadCountBoList.add(joinTotalThreadCountBo2);
        joinTotalThreadCountBoList.add(joinTotalThreadCountBo3);
        joinTotalThreadCountBoList.add(joinTotalThreadCountBo4);
        joinTotalThreadCountBoList.add(joinTotalThreadCountBo5);
        return joinTotalThreadCountBoList;
    }}
