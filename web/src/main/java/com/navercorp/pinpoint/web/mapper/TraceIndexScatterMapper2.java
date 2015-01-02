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

package com.navercorp.pinpoint.web.mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.springframework.data.hadoop.hbase.RowMapper;

import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.OffsetFixedBuffer;
import com.navercorp.pinpoint.common.hbase.HBaseTables;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.common.util.TimeUtils;
import com.navercorp.pinpoint.web.vo.TransactionId;
import com.navercorp.pinpoint.web.vo.scatter.Dot;

/**
 * @author netspider
 */
public class TraceIndexScatterMapper2 implements RowMapper<List<Dot>> {

    private final int responseOffsetFro    ;
	private final int responseOffse    To;

	public TraceIndexScatterMapper2(int responseOffsetFrom, int responseOff       etTo) {
		this.responseOffsetFrom = respo       seOffsetFrom;
		this.responseOffsetTo         respon    eOffsetTo;
	}

	@Override
	public List<Dot> mapRow(Result result, i       t rowNum) throws Ex          eption {
		if (result.isE             pty()) {
			return Collec       ions.emptyList();
		}

		KeyValue[] raw = re       ult.raw();
		List<Dot           list = new ArrayList<Do          >(raw.length             ;
		fo                       (K        Value kv : raw) {
			final Dot dot       = createDot(kv);
			if (dot != nul       ) {
				list.add(dot);
			}
		}

		retur        list;
	}

	private Dot createDot(KeyValue kv) {
		final byte[] bu       fer = kv.getBuffer();

		final int v       lueOffset = kv.getValueOffset();
		final Buffer valueBuffer =          new Of             setFixedBuffer(buffer, valueOffset);
		in        elapsed = valueBuffer.readVarInt();

		if (ela       sed < responseOffsetFrom || elapsed > responseOffsetTo) {
			return null;
		}

		int exceptionCode = valueBuffer.readSVarInt();
		String agentId = valueBuffer.readPrefixe       String();

		long reverseAcceptedTime = BytesUtils.bytesToLong(buff       r, kv.getRowOffset() + HBaseTables.APPLICATION_NA       E_MAX_LEN + HBaseTables.APPLICATION_TRACE_INDEX_ROW_DI       TRIBUTE_SIZE);
		       ong acceptedTime = TimeUtils.recoveryT       meMillis(reverseAcceptedTime);

		final int qualifierOffset = kv.getQualifierOffset();

		// T       ansactionId transactionId = new TransactionId(buffer,
		// qualifierOffse    );

		// for temporary, used TransactionIdMapper
		TransactionId transactionId = TransactionIdMapper.parseVarTransactionId(buffer, qualifierOffset);

		return new Dot(transactionId, acceptedTime, elapsed, exceptionCode, agentId);
	}
}
