/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.common.server.util;

import static com.navercorp.pinpoint.common.PinpointConstants.*;

import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.common.util.TimeUtils;
import com.navercorp.pinpoint.common.profiler.util.TransactionId;

import java.util.Objects;

/**
 * @author emeroad
 */
public final class SpanUtils {
    private SpanUtils() {
    }

    public static byte[] getApplicationTraceIndexRowKey(String applicationName, long timestamp) {
        Objects.requireNonNull(applicationName, applicationName);

        final byte[] bApplicationName = BytesUtils.toBytes(applicationName);
        return RowKeyUtils.concatFixedByteAndLong(bApplicationName, APPLICATION_NAME_MAX_LEN, TimeUtils.reverseTimeMillis(timestamp));
    }

    public static byte[] getApplicationTraceIndexRowKey(byte[] applicationName, long timestamp) {
        Objects.requireNonNull(applicationName, "applicationName");

        return RowKeyUtils.concatFixedByteAndLong(applicationName, APPLICATION_NAME_MAX_LEN, TimeUtils.reverseTimeMillis(timestamp));
    }

    public static byte[] getVarTransactionId(SpanBo span) {
        Objects.requireNonNull(span, "span");

        final TransactionId transactionId = span.getTransactionId();
        String agentId = transactionId.getAgentId();
        if (agentId == null) {
            agentId = span.getAgentId();
        }

        final Buffer buffer= new AutomaticBuffer(32);
        buffer.putPrefixedString(agentId);
        buffer.putSVLong(transactionId.getAgentStartTime());
        buffer.putVLong(transactionId.getTransactionSequence());
        return buffer.getBuffer();
    }
}
