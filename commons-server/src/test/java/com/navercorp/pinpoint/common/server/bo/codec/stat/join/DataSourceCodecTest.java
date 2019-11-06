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
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinDataSourceBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinDataSourceListBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinStatBo;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author minwoo.jung
 */
public class DataSourceCodecTest {

    @Test
    public void encodeAndDecodeTest() {
        final String id = "test_app";
        final long currentTime = new Date().getTime();
        final AgentStatDataPointCodec agentStatDataPointCodec = new AgentStatDataPointCodec();
        final DataSourceCodec dataSourceCodec = new DataSourceCodec(agentStatDataPointCodec);
        final List<JoinStatBo> joinDataSourceListBoList = createJoinDataSourceListBoList(currentTime);
        final Buffer encodedValueBuffer = new AutomaticBuffer();
        encodedValueBuffer.putByte(dataSourceCodec.getVersion());
        dataSourceCodec.encodeValues(encodedValueBuffer, joinDataSourceListBoList);

        final Buffer valueBuffer = new FixedBuffer(encodedValueBuffer.getBuffer());;
        final long baseTimestamp = AgentStatUtils.getBaseTimestamp(currentTime);
        final long timestampDelta = currentTime - baseTimestamp;
        final ApplicationStatDecodingContext decodingContext = new ApplicationStatDecodingContext();
        decodingContext.setApplicationId(id);
        decodingContext.setBaseTimestamp(baseTimestamp);
        decodingContext.setTimestampDelta(timestampDelta);

        assertEquals(valueBuffer.readByte(), dataSourceCodec.getVersion());
        List<JoinStatBo> decodedJoinDataSourceListBoList = dataSourceCodec.decodeValues(valueBuffer, decodingContext);
        for (int i = 0 ; i < decodedJoinDataSourceListBoList.size(); ++i) {
            assertTrue(decodedJoinDataSourceListBoList.get(i).equals(joinDataSourceListBoList.get(i)));
        }
    }

    private List<JoinStatBo> createJoinDataSourceListBoList(long currentTime) {
        final String id = "test_app";
        List<JoinStatBo> joinDataSourceListBoList = new ArrayList<JoinStatBo>();

        JoinDataSourceListBo joinDataSourceListBo1 = new JoinDataSourceListBo(id, createJoinDataSourceBoList(10), currentTime);
        JoinDataSourceListBo joinDataSourceListBo2 = new JoinDataSourceListBo(id, createJoinDataSourceBoList(20), currentTime + 5000);
        JoinDataSourceListBo joinDataSourceListBo3 = new JoinDataSourceListBo(id, createJoinDataSourceBoList(30), currentTime + 10000);
        JoinDataSourceListBo joinDataSourceListBo4 = new JoinDataSourceListBo(id, createJoinDataSourceBoList(40), currentTime + 15000);
        JoinDataSourceListBo joinDataSourceListBo5 = new JoinDataSourceListBo(id, createJoinDataSourceBoList(50), currentTime + 20000);

        joinDataSourceListBoList.add(joinDataSourceListBo1);
        joinDataSourceListBoList.add(joinDataSourceListBo2);
        joinDataSourceListBoList.add(joinDataSourceListBo3);
        joinDataSourceListBoList.add(joinDataSourceListBo4);
        joinDataSourceListBoList.add(joinDataSourceListBo5);

        return joinDataSourceListBoList;
    }

    private List<JoinDataSourceBo> createJoinDataSourceBoList(int plus) {
        List<JoinDataSourceBo> joinDataSourceBoList = new ArrayList<JoinDataSourceBo>();

        JoinDataSourceBo joinDataSourceBo1 = new JoinDataSourceBo((short)1000, "jdbc:mysql", 30 + plus, 25 + plus, "agent_id_1", 60 + plus, "agent_id_6");
        JoinDataSourceBo joinDataSourceBo2 = new JoinDataSourceBo((short)2000, "jdbc:mssql", 20 + plus, 5 + plus, "agent_id_2", 30 + plus, "agent_id_7");
        JoinDataSourceBo joinDataSourceBo3 = new JoinDataSourceBo((short)3000, "jdbc:postgre", 10 + plus, 25 + plus, "agent_id_3", 50 + plus, "agent_id_8");
        JoinDataSourceBo joinDataSourceBo4 = new JoinDataSourceBo((short)4000, "jdbc:oracle", 40 + plus, 5 + plus, "agent_id_4", 70 + plus, "agent_id_9");
        JoinDataSourceBo joinDataSourceBo5 = new JoinDataSourceBo((short)5000, "jdbc:cubrid", 50 + plus, 25 + plus, "agent_id_5", 80 + plus, "agent_id_10");

        joinDataSourceBoList.add(joinDataSourceBo1);
        joinDataSourceBoList.add(joinDataSourceBo2);
        joinDataSourceBoList.add(joinDataSourceBo3);
        joinDataSourceBoList.add(joinDataSourceBo4);
        joinDataSourceBoList.add(joinDataSourceBo5);

        return joinDataSourceBoList;
    }

}