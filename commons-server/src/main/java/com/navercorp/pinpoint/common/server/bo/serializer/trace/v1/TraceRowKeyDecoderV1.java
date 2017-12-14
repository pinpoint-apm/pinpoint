package com.navercorp.pinpoint.common.server.bo.serializer.trace.v1;

import com.navercorp.pinpoint.common.server.bo.serializer.RowKeyDecoder;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.common.util.TransactionId;
import org.springframework.stereotype.Component;

/**
 * @author Woonduk Kang(emeroad)
 */
@Component
public class TraceRowKeyDecoderV1 implements RowKeyDecoder<TransactionId> {

    public static final int AGENT_NAME_MAX_LEN = TraceRowKeyEncoderV1.AGENT_NAME_MAX_LEN;
    public static final int DISTRIBUTE_HASH_SIZE = TraceRowKeyEncoderV1.DISTRIBUTE_HASH_SIZE;

    private final int distributeHashSize;


    public TraceRowKeyDecoderV1() {
        this(DISTRIBUTE_HASH_SIZE);
    }

    public TraceRowKeyDecoderV1(int distributeHashSize) {
        this.distributeHashSize = distributeHashSize;
    }

    @Override
    public TransactionId decodeRowKey(byte[] rowkey) {
        if (rowkey == null) {
            throw new NullPointerException("rowkey must not be null");
        }

        return readTransactionId(rowkey, distributeHashSize);
    }

    private TransactionId readTransactionId(byte[] rowKey, int offset) {

        String agentId = BytesUtils.toStringAndRightTrim(rowKey, offset, AGENT_NAME_MAX_LEN);
        long agentStartTime = BytesUtils.bytesToLong(rowKey, offset + AGENT_NAME_MAX_LEN);
        long transactionSequence = BytesUtils.bytesToLong(rowKey, offset + BytesUtils.LONG_BYTE_LENGTH + AGENT_NAME_MAX_LEN);

        return new TransactionId(agentId, agentStartTime, transactionSequence);
    }
}
