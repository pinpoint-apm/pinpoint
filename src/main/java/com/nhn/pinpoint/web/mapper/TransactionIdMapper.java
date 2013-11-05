package com.nhn.pinpoint.web.mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.nhn.pinpoint.common.buffer.Buffer;
import com.nhn.pinpoint.common.buffer.FixedBuffer;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Component;

import com.nhn.pinpoint.web.vo.TransactionId;

/**
 * @author emeroad
 */
@Component
public class TransactionIdMapper implements RowMapper<List<TransactionId>> {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	// @Autowired
	// private AbstractRowKeyDistributor rowKeyDistributor;

	@Override
	public List<TransactionId> mapRow(Result result, int rowNum) throws Exception {
		if (result.isEmpty()) {
			return Collections.emptyList();
		}
		KeyValue[] raw = result.raw();
		List<TransactionId> traceIdList = new ArrayList<TransactionId>(raw.length);
		for (KeyValue kv : raw) {
			byte[] buffer = kv.getBuffer();
			int qualifierOffset = kv.getQualifierOffset();
			// key값만큼 1증가 시킴
			TransactionId traceId = parseVarTransactionId(buffer, qualifierOffset);
			traceIdList.add(traceId);
			
			logger.debug("found traceId {}", traceId);
		}
		return traceIdList;
	}

    public static TransactionId parseVarTransactionId(byte[] bytes, int offset) {
        if (bytes == null) {
            throw new NullPointerException("bytes must not be null");
        }
        final Buffer buffer = new FixedBuffer(bytes, offset);
        String agentId = buffer.readPrefixedString();
        long agentStartTime = buffer.readSVarLong();
        long transactionSequence = buffer.readVarLong();
        return new TransactionId(agentId, agentStartTime, transactionSequence);
    }


}
