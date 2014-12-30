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

	private byte[] value = null;
	private boolean filterRow = true;

	private final int responseTimeFrom;
	private final int responseTimeTo;

	public ApplicationTraceIndexResponseTimeFilter(int responseTimeFrom, int responseTimeTo) {
		super();
		this.responseTimeFrom = responseTimeFrom;
		this.responseTimeTo = responseTimeTo;
	}

	@Override
	public void reset() {
	    // reset flag when comparing with a new value
		this.filterRow = true;
	}

	@Override
	public ReturnCode filterKeyValue(KeyValue kv) {
		final byte[] buffer = kv.getBuffer();

		final int valueOffset = kv.getValueOffset();
		final Buffer valueBuffer = new OffsetFixedBuffer(buffer, valueOffset);
		int elapsed = valueBuffer.readVarInt();

		if (elapsed < responseTimeFrom || elapsed > responseTimeTo) {
		    // skip row if conditions are not met
			filterRow = false;
		}

		// always return this value as the actual decision for filtering happens later
		return ReturnCode.INCLUDE;
	}

	@Override
	public boolean filterRow() {
	    // the actual decision for filtering happens here depending on the flag
		return filterRow;
	}

	@Override
	public void readFields(DataInput dataInput) throws IOException {
		this.value = Bytes.readByteArray(dataInput);
	}

	@Override
	public void write(DataOutput dataOutput) throws IOException {
		Bytes.writeByteArray(dataOutput, this.value);
	}
}
