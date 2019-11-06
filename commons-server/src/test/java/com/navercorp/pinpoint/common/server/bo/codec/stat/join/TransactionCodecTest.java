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
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinTransactionBo;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author minwoo.jung
 */
public class TransactionCodecTest {
    @Test
    public void encodeValuesTest() throws Exception {
        final String id = "test_app";
        final long currentTime = new Date().getTime();
        TransactionCodec transactionCodec = new TransactionCodec(new AgentStatDataPointCodec());
        final Buffer encodedValueBuffer = new AutomaticBuffer();
        final List<JoinStatBo> joinTransactionBoList = createJoinTransactionBoList(currentTime);
        encodedValueBuffer.putByte(transactionCodec.getVersion());
        transactionCodec.encodeValues(encodedValueBuffer, joinTransactionBoList);

        final Buffer valueBuffer = new FixedBuffer(encodedValueBuffer.getBuffer());
        final long baseTimestamp = AgentStatUtils.getBaseTimestamp(currentTime);
        final long timestampDelta = currentTime - baseTimestamp;
        final ApplicationStatDecodingContext decodingContext = new ApplicationStatDecodingContext();
        decodingContext.setApplicationId(id);
        decodingContext.setBaseTimestamp(baseTimestamp);
        decodingContext.setTimestampDelta(timestampDelta);

        assertEquals(valueBuffer.readByte(), transactionCodec.getVersion());
        List<JoinStatBo> decodedJoinTransactionBoList = transactionCodec.decodeValues(valueBuffer, decodingContext);
        for (int i = 0; i < decodedJoinTransactionBoList.size(); i++) {
            assertTrue(decodedJoinTransactionBoList.get(i).equals(joinTransactionBoList.get(i)));
        }
    }

    private List<JoinStatBo> createJoinTransactionBoList(long currentTime) {
        final String id = "test_app";
        List<JoinStatBo> joinTransactionBoList = new ArrayList<JoinStatBo>();
        JoinTransactionBo joinTransactionBo1 = new JoinTransactionBo(id, 5000, 150, 10, "app_1_1", 230, "app_1_2", currentTime);
        JoinTransactionBo joinTransactionBo2 = new JoinTransactionBo(id, 10000, 110, 22, "app_2_1", 330, "app_2_2", currentTime + 5000);
        JoinTransactionBo joinTransactionBo3 = new JoinTransactionBo(id, 15000, 120, 24, "app_3_1", 540, "app_3_2", currentTime + 10000);
        JoinTransactionBo joinTransactionBo4 = new JoinTransactionBo(id, 20000, 130, 25, "app_4_1", 560, "app_4_2", currentTime + 15000);
        JoinTransactionBo joinTransactionBo5 = new JoinTransactionBo(id, 25000, 140, 12, "app_5_1", 260, "app_5_2", currentTime + 20000);
        joinTransactionBoList.add(joinTransactionBo1);
        joinTransactionBoList.add(joinTransactionBo2);
        joinTransactionBoList.add(joinTransactionBo3);
        joinTransactionBoList.add(joinTransactionBo4);
        joinTransactionBoList.add(joinTransactionBo5);

        return joinTransactionBoList;
    }

    @Test
    public void decodeValuesTest() throws Exception {

    }

}