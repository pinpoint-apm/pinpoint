package com.nhn.pinpoint.common.hbase.filter;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.filter.FilterBase;
import org.apache.hadoop.hbase.util.Bytes;

import com.nhn.pinpoint.common.buffer.Buffer;
import com.nhn.pinpoint.common.buffer.OffsetFixedBuffer;

/**
 * value filter보다 column name에 prefix 붙여 filter하는것이 나을 듯하여 일단 deprecated 처리 함.
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
		// 새로운 값을 비교할 때마다 플래그 재설정.
		this.filterRow = true;
	}

	@Override
	public ReturnCode filterKeyValue(KeyValue kv) {
		final byte[] buffer = kv.getBuffer();

		final int valueOffset = kv.getValueOffset();
		final Buffer valueBuffer = new OffsetFixedBuffer(buffer, valueOffset);
		int elapsed = valueBuffer.readVarInt();

		if (elapsed < responseTimeFrom || elapsed > responseTimeTo) {
			// 조건에 맞지 않으면 row를 통과
			filterRow = false;
		}

		// 실제 결정은 나중에 하기 때문에 항상 이 값을 반환
		return ReturnCode.INCLUDE;
	}

	@Override
	public boolean filterRow() {
		// 실제 결정은 플래그 상태에 따라 이곳에서 이루어진다.
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
