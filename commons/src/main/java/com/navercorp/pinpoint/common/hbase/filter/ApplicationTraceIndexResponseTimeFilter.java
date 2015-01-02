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

package com.navercorp.pinpoint.common.hbase.filter;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.filter.FilterBase;
import org.apache.hadoop.hbase.util.Bytes;

import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.OffsetFixedBuffer;

/**
 * @deprecated it is better to filter by adding prefix to the column name, than to filter by value
 * 
 * @author netspider
 * 
 */
@Deprecated
public class ApplicationTraceIndexResponseTimeFilter extends FilterBase {

    private byte[] value = nul    ;
	private boolean filterRow = t    ue;

	private final int responseT    meFrom;
	private final int respo    seTimeTo;

	public ApplicationTraceIndexResponseTimeFilter(int responseTimeFrom, int res       onse       imeTo) {
		super();
		this.responseTi       eFrom = responseTimeFrom;
		this.        sponseT    meTo = responseTime    o;
	}

	@Override
	public void reset() {
	    /        reset flag when c        paring     ith a new value
		this.filterRow = true;
	}

       @Override
	public ReturnCode filte       KeyValue(KeyValue kv) {
		final byte[] b       ffer = kv.getBuffer();

		final int valueOffset = kv.getValueOffse       ();
		final Buffer valueBuffer = new       OffsetFixedBuffer(buffer, valueOffset);
		int elapsed = v       lueBuffer.readVarInt();

		if (elapse           < responseT             meFrom || elapsed > responseTimeTo) {
		    // skip row if conditions are       not met
			filterRow =        alse;
	    }

		// always return this    value as the actual decision for filtering happens later
		return ReturnC       de.INCLUDE;

	@Over    ide
	public boolean filterRow() {
	    // the actual decision        or filtering happens here depending on t         flag
	    return filterRow;
	}

	@Override
	public void readFields(Da       aInput dataInput) throws IOException {
		    his.value = Bytes.readByteArray(dataInput);
	}

	@Override
	public void write(DataOutput dataOutput) throws IOException {
		Bytes.writeByteArray(dataOutput, this.value);
	}
}
