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
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinResponseTimeBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinStatBo;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author minwoo.jung
 */
public class ResponseTimeCodecTest {

    @Test
    public void encodeAndDecodeTest() {
        final String id = "test_app";
        final long currentTime = new Date().getTime();
        final AgentStatDataPointCodec agentStatDataPointCodec = new AgentStatDataPointCodec();
        final ResponseTimeCodec responseTimeCodec = new ResponseTimeCodec(agentStatDataPointCodec);
        final Buffer encodedValueBuffer = new AutomaticBuffer();
        final List<JoinStatBo> joinResponseTimeBoList = createJoinResponseTimeBoList(currentTime);
        encodedValueBuffer.putByte(responseTimeCodec.getVersion());
        responseTimeCodec.encodeValues(encodedValueBuffer, joinResponseTimeBoList);

        final Buffer valueBuffer = new FixedBuffer(encodedValueBuffer.getBuffer());;
        final long baseTimestamp = AgentStatUtils.getBaseTimestamp(currentTime);
        final long timestampDelta = currentTime - baseTimestamp;
        final ApplicationStatDecodingContext decodingContext = new ApplicationStatDecodingContext();
        decodingContext.setApplicationId(id);
        decodingContext.setBaseTimestamp(baseTimestamp);
        decodingContext.setTimestampDelta(timestampDelta);

        assertEquals(valueBuffer.readByte(), responseTimeCodec.getVersion());
        List<JoinStatBo> decodedJoinResponseTimeBoList = responseTimeCodec.decodeValues(valueBuffer, decodingContext);
        for (int i = 0 ; i < decodedJoinResponseTimeBoList.size(); i++) {
            assertTrue(decodedJoinResponseTimeBoList.get(i).equals(joinResponseTimeBoList.get(i)));
        }
    }

    private List<JoinStatBo> createJoinResponseTimeBoList(long currentTime) {
        final String id = "test_app";
        List<JoinStatBo> joinResponseTimeBoList = new ArrayList<JoinStatBo>();
        JoinResponseTimeBo joinResponseTimeBo1 = new JoinResponseTimeBo(id, currentTime, 3000, 2, "app_1_1", 6000, "app_1_2");
        JoinResponseTimeBo joinResponseTimeBo2 = new JoinResponseTimeBo(id, currentTime + 5000, 4000, 200, "app_2_1", 9000, "app_2_2");
        JoinResponseTimeBo joinResponseTimeBo3 = new JoinResponseTimeBo(id, currentTime + 10000, 2000, 20, "app_3_1", 7000, "app_3_2");
        JoinResponseTimeBo joinResponseTimeBo4 = new JoinResponseTimeBo(id, currentTime + 15000, 5000, 20, "app_4_1", 8000, "app_4_2");
        JoinResponseTimeBo joinResponseTimeBo5 = new JoinResponseTimeBo(id, currentTime + 20000, 1000, 10, "app_5_1", 6600, "app_5_2");
        joinResponseTimeBoList.add(joinResponseTimeBo1);
        joinResponseTimeBoList.add(joinResponseTimeBo2);
        joinResponseTimeBoList.add(joinResponseTimeBo3);
        joinResponseTimeBoList.add(joinResponseTimeBo4);
        joinResponseTimeBoList.add(joinResponseTimeBo5);

        return joinResponseTimeBoList;
    }

}