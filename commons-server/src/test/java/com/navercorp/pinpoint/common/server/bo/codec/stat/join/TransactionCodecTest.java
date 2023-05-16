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
import com.navercorp.pinpoint.common.server.bo.codec.stat.ApplicationStatCodec;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.AgentStatUtils;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.ApplicationStatDecodingContext;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinTransactionBo;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author minwoo.jung
 */
public class TransactionCodecTest {
    @Test
    public void encodeValuesTest() {
        final String id = "test_app";
        final long currentTime = new Date().getTime();
        ApplicationStatCodec<JoinTransactionBo> transactionCodec = new TransactionCodec(new AgentStatDataPointCodec());
        final Buffer encodedValueBuffer = new AutomaticBuffer();
        final List<JoinTransactionBo> joinTransactionBoList = createJoinTransactionBoList(currentTime);
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
        List<JoinTransactionBo> decodedJoinTransactionBoList = transactionCodec.decodeValues(valueBuffer, decodingContext);
        for (int i = 0; i < decodedJoinTransactionBoList.size(); i++) {
            assertEquals(decodedJoinTransactionBoList.get(i), joinTransactionBoList.get(i));
        }
    }

    private List<JoinTransactionBo> createJoinTransactionBoList(long currentTime) {
        final String id = "test_app";
        return List.of(
                new JoinTransactionBo(id, 5000, 150, 10, "app_1_1", 230, "app_1_2", currentTime),
                new JoinTransactionBo(id, 10000, 110, 22, "app_2_1", 330, "app_2_2", currentTime + 5000),
                new JoinTransactionBo(id, 15000, 120, 24, "app_3_1", 540, "app_3_2", currentTime + 10000),
                new JoinTransactionBo(id, 20000, 130, 25, "app_4_1", 560, "app_4_2", currentTime + 15000),
                new JoinTransactionBo(id, 25000, 140, 12, "app_5_1", 260, "app_5_2", currentTime + 20000)
        );
    }


}