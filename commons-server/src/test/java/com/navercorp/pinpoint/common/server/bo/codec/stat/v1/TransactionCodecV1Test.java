/*
 * Copyright 2016 Naver Corp.
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

package com.navercorp.pinpoint.common.server.bo.codec.stat.v1;

import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.FixedBuffer;
import com.navercorp.pinpoint.common.server.bo.codec.stat.TestAgentStatFactory;
import com.navercorp.pinpoint.common.server.bo.stat.TransactionBo;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

/**
 * @author HyunGil Jeong
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext-test.xml")
public class TransactionCodecV1Test {

    private static final int NUM_TEST_RUNS = 20;

    @Autowired
    private TransactionCodecV1 transactionCodec;

    @Test
    public void should_be_encoded_and_decoded_to_same_value() {
        for (int i = 0; i < NUM_TEST_RUNS; ++i) {
            runTest();
        }
    }

    private void runTest() {
        // Given
        final long initialTimestamp = System.currentTimeMillis();
        final List<TransactionBo> expectedTransactionBos = TestAgentStatFactory.createTransactionBos(initialTimestamp);
        // When
        Buffer encodedValueBuffer = new AutomaticBuffer();
        this.transactionCodec.encodeValues(encodedValueBuffer, expectedTransactionBos);
        // Then
        Buffer valueBuffer = new FixedBuffer(encodedValueBuffer.getBuffer());
        List<TransactionBo> actualTransactionBos = this.transactionCodec.decodeValues(valueBuffer, initialTimestamp);
        Assert.assertEquals(expectedTransactionBos, actualTransactionBos);
    }
}
