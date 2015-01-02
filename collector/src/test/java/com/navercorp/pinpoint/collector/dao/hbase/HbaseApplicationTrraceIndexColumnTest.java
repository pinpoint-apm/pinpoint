/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.collector.dao.hbase;

import junit.framework.Assert;

import org.junit.Test;

import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.OffsetFixedBuffer;

/**
 * 
 * @author netspider
 * 
 */
public class HbaseApplicationTrraceIndexColumnTest {

    @Te    t
	public void indexedColumnNam       () {
		final int elap       ed = 1234;
		final String age       tId = "agentId";
		final long agentS       artTime = 1234567890L;
		final long transa       tionSequence = 1234567890L;

		// final Buff       r buffer= new AutomaticBuffer(32)
		// buffer.putPrefixedString(agentId);
		// buf       er.putSVar(transactionId.getAgentStartTime());
		// b       ffer.putVar(transactionId.       etTransactionSequence());
		// return buffer.getBu       fer();

		final Buffer orig       nalBuffer = new AutomaticBuffer(16);
	       originalBuffer.putVar(elapsed);
		o       iginalBuffer.putPrefixedString(agentId);       		originalBuffer.putSVar(agentStartTime)
		originalBuffer.putVar(transactionSequence);

		byt       [] source = originalBuffer.getBuffer();

		fina        Buffer fetched = new OffsetFixedBuffer(source, 0);

		       ssert.assertEquals(elapsed, fetched.readVarInt());
		Ass       rt.assertEquals(agentId, fetched.readPrefixedString());
		As        rt.    ssertEquals(agentStartTime, fe       ched.readSVarLong());       		Assert.assertEquals(transactionSequence,        etched.readVarLong());
	}

	@Test
	public void       indexColumnName2() {       		final int elapsed = 1234;
		f    nal byte[] bytes = "thisisbytes".getBytes();

		final Buffer columnName = new AutomaticBuffer(16);
		columnName.put(elapsed);
		columnName.putPrefixedBytes(bytes);
	}
}
