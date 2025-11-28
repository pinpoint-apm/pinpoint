package com.navercorp.pinpoint.web.util;

import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.OffsetFixedBuffer;
import com.navercorp.pinpoint.common.profiler.util.TransactionIdV1;
import com.navercorp.pinpoint.common.server.bo.serializer.agent.TraceIndexRowUtils;
import com.navercorp.pinpoint.web.vo.scatter.Dot;
import org.apache.hadoop.hbase.Cell;

public class TraceIndexUtils {

    public static Dot createDot(Cell cell) {
        final Buffer valueBuffer = new OffsetFixedBuffer(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength());
        int elapsed = valueBuffer.readInt(); //v2
        int exceptionCode = valueBuffer.readSVInt();
        String agentId = valueBuffer.readPrefixedString();

        long acceptedTime = TraceIndexRowUtils.extractAcceptTime(cell.getRowArray(), cell.getRowOffset());

        return new Dot(TransactionIdV1.EMPTY_ID, acceptedTime, elapsed, exceptionCode, agentId);
    }
}
