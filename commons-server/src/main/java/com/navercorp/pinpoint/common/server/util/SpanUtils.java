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

import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.trace.PinpointServerTraceId;
import com.navercorp.pinpoint.common.server.trace.ServerTraceId;

/**
 * @deprecated use {@link TransactionIdParser}
 *
 * @author emeroad
 */
@Deprecated
public final class SpanUtils {
    private SpanUtils() {
    }

    /**
     * deserializer ref : TransactionIdMapper.parseVarTransactionId
     */
    public static byte[] getVarTransactionId(SpanBo span) {
        ServerTraceId serverTraceId = span.getTransactionId();
        if (serverTraceId instanceof PinpointServerTraceId) {
            return TransactionIdParser.getVarTransactionId(serverTraceId, span::getAgentId);
        } else {
            return serverTraceId.getId();
        }
    }

    public static TransactionId parseVarTransactionId(byte[] bytes, int offset, int length) {
        return TransactionIdParser.parseVarTransactionId(bytes, offset, length);
    }

    public static void writeTransactionIdV1(Buffer buffer, TransactionId transactionId) {
        TransactionIdParser.writeTransactionIdV1(buffer, transactionId);
    }

    public static TransactionId readTransactionIdV1(Buffer buffer) {
        return TransactionIdParser.readTransactionIdV1(buffer);
    }
}
