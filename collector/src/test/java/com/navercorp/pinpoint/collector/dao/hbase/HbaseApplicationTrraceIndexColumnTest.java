package com.nhn.pinpoint.collector.dao.hbase;

import junit.framework.Assert;

import org.junit.Test;

import com.nhn.pinpoint.common.buffer.AutomaticBuffer;
import com.nhn.pinpoint.common.buffer.Buffer;
import com.nhn.pinpoint.common.buffer.OffsetFixedBuffer;

/**
 * 
 * @author netspider
 * 
 */
public class HbaseApplicationTrraceIndexColumnTest {

	@Test
	public void indexedColumnName() {
		final int elapsed = 1234;
		final String agentId = "agentId";
		final long agentStartTime = 1234567890L;
		final long transactionSequence = 1234567890L;

		// final Buffer buffer= new AutomaticBuffer(32);
		// buffer.putPrefixedString(agentId);
		// buffer.putSVar(transactionId.getAgentStartTime());
		// buffer.putVar(transactionId.getTransactionSequence());
		// return buffer.getBuffer();

		final Buffer originalBuffer = new AutomaticBuffer(16);
		originalBuffer.putVar(elapsed);
		originalBuffer.putPrefixedString(agentId);
		originalBuffer.putSVar(agentStartTime);
		originalBuffer.putVar(transactionSequence);

		byte[] source = originalBuffer.getBuffer();

		final Buffer fetched = new OffsetFixedBuffer(source, 0);

		Assert.assertEquals(elapsed, fetched.readVarInt());
		Assert.assertEquals(agentId, fetched.readPrefixedString());
		Assert.assertEquals(agentStartTime, fetched.readSVarLong());
		Assert.assertEquals(transactionSequence, fetched.readVarLong());
	}

	@Test
	public void indexColumnName2() {
		final int elapsed = 1234;
		final byte[] bytes = "thisisbytes".getBytes();

		final Buffer columnName = new AutomaticBuffer(16);
		columnName.put(elapsed);
		columnName.putPrefixedBytes(bytes);
	}
}
