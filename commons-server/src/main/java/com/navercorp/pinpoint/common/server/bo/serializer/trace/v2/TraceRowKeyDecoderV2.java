/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.common.server.bo.serializer.trace.v2;

import com.navercorp.pinpoint.common.hbase.wd.ByteSaltKey;
import com.navercorp.pinpoint.common.server.bo.serializer.RowKeyDecoder;
import com.navercorp.pinpoint.common.server.trace.ServerTraceId;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
@Component
public class TraceRowKeyDecoderV2 implements RowKeyDecoder<ServerTraceId> {

    public static final int AGENT_ID_MAX_LEN = TraceRowKeyEncoderV2.AGENT_ID_MAX_LEN;

    private static final int OPENTELEMETRY_TRACE_ID_LEN = TraceRowKeyEncoderV2.OPENTELEMETRY_TRACE_ID_LEN;

    private final ByteSaltKey saltKey;

    public TraceRowKeyDecoderV2() {
        this(ByteSaltKey.SALT);
    }

    public TraceRowKeyDecoderV2(ByteSaltKey saltKey) {
        this.saltKey = Objects.requireNonNull(saltKey, "saltKey");
    }


    @Override
    public ServerTraceId decodeRowKey(byte[] rowkey) {
        Objects.requireNonNull(rowkey, "rowkey");

        return readTransactionId(rowkey, saltKey.size());
    }

    private ServerTraceId readTransactionId(byte[] rowKey, int offset) {
        return ServerTraceId.decodeTraceRowKey(rowKey, offset);
    }
}
