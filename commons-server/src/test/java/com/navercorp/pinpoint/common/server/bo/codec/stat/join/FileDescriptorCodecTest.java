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
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinFileDescriptorBo;
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
public class FileDescriptorCodecTest {

    @Test
    public void encodeAndDecodeTest(){
        final String id = "test_app";
        final long currentTime = new Date().getTime();
        final AgentStatDataPointCodec agentStatDataPointCodec = new AgentStatDataPointCodec();
        final FileDescriptorCodec fileDescriptorCodec = new FileDescriptorCodec(agentStatDataPointCodec);
        final Buffer encodedValueBuffer = new AutomaticBuffer();
        final List<JoinStatBo> joinFileDescriptorBoList = createJoinFileDescriptorBoList(currentTime);
        encodedValueBuffer.putByte(fileDescriptorCodec.getVersion());
        fileDescriptorCodec.encodeValues(encodedValueBuffer, joinFileDescriptorBoList);

        final Buffer valueBuffer = new FixedBuffer(encodedValueBuffer.getBuffer());;
        final long baseTimestamp = AgentStatUtils.getBaseTimestamp(currentTime);
        final long timestampDelta = currentTime - baseTimestamp;
        final ApplicationStatDecodingContext decodingContext = new ApplicationStatDecodingContext();
        decodingContext.setApplicationId(id);
        decodingContext.setBaseTimestamp(baseTimestamp);
        decodingContext.setTimestampDelta(timestampDelta);

        assertEquals(valueBuffer.readByte(), fileDescriptorCodec.getVersion());
        List<JoinStatBo> decodedJoinFileDescriptorBoList = fileDescriptorCodec.decodeValues(valueBuffer, decodingContext);
        for (int i = 0; i < decodedJoinFileDescriptorBoList.size(); i++) {
            assertTrue(decodedJoinFileDescriptorBoList.get(i).equals(joinFileDescriptorBoList.get(i)));
        }
    }

    private List<JoinStatBo> createJoinFileDescriptorBoList(long currentTime) {
        final String id = "test_app";
        final List<JoinStatBo> joinFileDescriptorBoList = new ArrayList();
        JoinFileDescriptorBo joinFileDescriptorBo1 = new JoinFileDescriptorBo(id, 80, 1000, "agent1_1", 30, "agent1_2", currentTime);
        JoinFileDescriptorBo joinFileDescriptorBo2 = new JoinFileDescriptorBo(id, 70, 900, "agent2_1", 20, "agent2_2", currentTime + 5000);
        JoinFileDescriptorBo joinFileDescriptorBo4 = new JoinFileDescriptorBo(id, 60, 800, "agent4_1", 15, "agent4_2",  currentTime + 15000);
        JoinFileDescriptorBo joinFileDescriptorBo3 = new JoinFileDescriptorBo(id, 50, 700, "agent3_1", 10, "agent3_2", currentTime + 10000);
        JoinFileDescriptorBo joinFileDescriptorBo5 = new JoinFileDescriptorBo(id, 40, 600, "agent5_1", 5, "agent5_2", currentTime + 20000);
        joinFileDescriptorBoList.add(joinFileDescriptorBo1);
        joinFileDescriptorBoList.add(joinFileDescriptorBo2);
        joinFileDescriptorBoList.add(joinFileDescriptorBo3);
        joinFileDescriptorBoList.add(joinFileDescriptorBo4);
        joinFileDescriptorBoList.add(joinFileDescriptorBo5);
        return joinFileDescriptorBoList;
    }

}