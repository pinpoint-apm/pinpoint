package com.navercorp.pinpoint.common.server.util;

import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.FixedBuffer;
import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.common.profiler.util.TransactionIdUtils;
import com.navercorp.pinpoint.common.util.IdValidateUtils;
import com.navercorp.pinpoint.common.util.StringUtils;

import java.util.Arrays;
import java.util.Objects;

public class TransactionIdParser {

    public static TransactionId parse(final byte[] transactionId, String defaultAgentId) {
        Objects.requireNonNull(transactionId, "transactionId");

        final Buffer buffer = new FixedBuffer(transactionId);
        final byte version = buffer.readByte();
        if (version != TransactionIdUtils.VERSION) {
            throw new IllegalArgumentException("invalid Version");
        }

        String agentId = buffer.readPrefixedString();
        agentId = StringUtils.defaultString(agentId, defaultAgentId);
        if (!IdValidateUtils.validateId(agentId)) {
            throw new IllegalArgumentException("invalid transactionId:" + Arrays.toString(transactionId));
        }

        final long agentStartTime = buffer.readVLong();
        final long transactionSequence = buffer.readVLong();

        return TransactionId.of(agentId, agentStartTime,transactionSequence);
    }

}
