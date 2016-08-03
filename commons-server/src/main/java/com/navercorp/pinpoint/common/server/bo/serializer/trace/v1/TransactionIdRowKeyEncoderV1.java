package com.navercorp.pinpoint.common.server.bo.serializer.trace.v1;

import com.navercorp.pinpoint.common.PinpointConstants;
import com.navercorp.pinpoint.common.server.bo.BasicSpan;
import com.navercorp.pinpoint.common.server.bo.serializer.RowKeyEncoder;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.common.util.TransactionId;
import com.sematext.hbase.wd.AbstractRowKeyDistributor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @author Woonduk Kang(emeroad)
 */
@Component
public class TransactionIdRowKeyEncoderV1 implements RowKeyEncoder<TransactionId> {

    public static final int AGENT_NAME_MAX_LEN = TraceRowKeyEncoderV1.AGENT_NAME_MAX_LEN;
    public static final int DISTRIBUTE_HASH_SIZE = TraceRowKeyEncoderV1.DISTRIBUTE_HASH_SIZE;

    private final AbstractRowKeyDistributor rowKeyDistributor;

    @Autowired
    public TransactionIdRowKeyEncoderV1(@Qualifier("traceDistributor") AbstractRowKeyDistributor rowKeyDistributor) {
        if (rowKeyDistributor == null) {
            throw new NullPointerException("rowKeyDistributor must not be null");
        }

        this.rowKeyDistributor = rowKeyDistributor;
    }

    public byte[] encodeRowKey(TransactionId transactionId) {
        if (transactionId == null) {
            throw new NullPointerException("transactionId must not be null");
        }

        byte[] rowKey = BytesUtils.stringLongLongToBytes(transactionId.getAgentId(), AGENT_NAME_MAX_LEN, transactionId.getAgentStartTime(), transactionId.getTransactionSequence());
        return wrapDistributedRowKey(rowKey);
    }

    private byte[] wrapDistributedRowKey(byte[] rowKey) {
        return rowKeyDistributor.getDistributedKey(rowKey);
    }

}
