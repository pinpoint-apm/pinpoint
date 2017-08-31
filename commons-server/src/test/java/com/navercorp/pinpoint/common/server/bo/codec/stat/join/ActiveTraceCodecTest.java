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
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinActiveTraceBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinStatBo;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author minwoo.jung
 */
public class ActiveTraceCodecTest {

    @Test
    public void encodeValuesTest() {
        final String id = "test_app";
        final long currentTime = new Date().getTime();
        ActiveTraceCodec activeTraceCodec = new ActiveTraceCodec(new AgentStatDataPointCodec());
        final Buffer encodedValueBuffer = new AutomaticBuffer();
        final List<JoinStatBo> joinActiveTraceBoList = createJoinActiveTRaceBoList(currentTime);
        encodedValueBuffer.putByte(activeTraceCodec.getVersion());
        activeTraceCodec.encodeValues(encodedValueBuffer, joinActiveTraceBoList);

        final Buffer valueBuffer = new FixedBuffer(encodedValueBuffer.getBuffer());
        final long baseTimestamp = AgentStatUtils.getBaseTimestamp(currentTime);
        final long timestampDelta = currentTime - baseTimestamp;
        final ApplicationStatDecodingContext decodingContext = new ApplicationStatDecodingContext();
        decodingContext.setApplicationId(id);
        decodingContext.setBaseTimestamp(baseTimestamp);
        decodingContext.setTimestampDelta(timestampDelta);

        assertEquals(valueBuffer.readByte(), activeTraceCodec.getVersion());
        List<JoinStatBo> decodedJoinActiveTraceBoList = activeTraceCodec.decodeValues(valueBuffer, decodingContext);
        for (int i = 0; i < decodedJoinActiveTraceBoList.size(); i++) {
            assertTrue(decodedJoinActiveTraceBoList.get(i).equals(joinActiveTraceBoList.get(i)));
        }
    }

    private List<JoinStatBo> createJoinActiveTRaceBoList(long currentTime) {
        final String id = "test_app";
        List<JoinStatBo> joinActiveTraceBoList = new ArrayList<JoinStatBo>();
        JoinActiveTraceBo joinActiveTraceBo1 = new JoinActiveTraceBo(id, 1, (short)2, 31, 11, "app_1_1", 41, "app_1_2", currentTime);
        JoinActiveTraceBo joinActiveTraceBo2 = new JoinActiveTraceBo(id, 1, (short)2, 32, 12, "app_2_1", 42, "app_2_2", currentTime + 5000);
        JoinActiveTraceBo joinActiveTraceBo3 = new JoinActiveTraceBo(id, 1, (short)2, 33, 13, "app_3_1", 43, "app_3_2", currentTime + 10000);
        JoinActiveTraceBo joinActiveTraceBo4 = new JoinActiveTraceBo(id, 1, (short)2, 34, 14, "app_4_1", 44, "app_4_2", currentTime + 15000);
        JoinActiveTraceBo joinActiveTraceBo5 = new JoinActiveTraceBo(id, 1, (short)2, 35, 15, "app_5_1", 45, "app_5_2", currentTime + 20000);
        joinActiveTraceBoList.add(joinActiveTraceBo1);
        joinActiveTraceBoList.add(joinActiveTraceBo2);
        joinActiveTraceBoList.add(joinActiveTraceBo3);
        joinActiveTraceBoList.add(joinActiveTraceBo4);
        joinActiveTraceBoList.add(joinActiveTraceBo5);

        return joinActiveTraceBoList;
    }


}