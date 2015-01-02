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

import static com.navercorp.pinpoint.common.hbase.HBaseTables.*;

import com.navercorp.pinpoint.collector.dao.ApplicationTraceIndexDao;
import com.navercorp.pinpoint.collector.util.AcceptedTimeService;
import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.hbase.HbaseOperations2;
import com.navercorp.pinpoint.common.util.SpanUtils;
import com.navercorp.pinpoint.thrift.dto.TSpan;
import com.sematext.hbase.wd.AbstractRowKeyDistributor;

import org.apache.hadoop.hbase.client.Put;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

/**
 * find traceids by application name
 * 
 * @author netspider
 * @author emeroad
 */
@Repository
public class HbaseApplicationTraceIndexDao implements ApplicationTraceIndexDao {

    @Autowir    d
	private HbaseOperations2 hbaseTemplate;

    @Autowired
    private AcceptedTimeService acceptedTimeService;

    @Autowired
    @Qualifier("applicationTraceIndexDistributor")
    private AbstractRowKeyDistributor rowKeyDistribu    or;

	@    verride
	public void insert(final TSpan span) {
        if (span == null) {
            throw new NullPointerException("span must not be null");
        }

        final Buffer buffer = new AutomaticBuffer(10 + AGENT_NAME_MAX_LEN);
        buffer.putVar(span.getElapsed());
        buffer.putSVar(span.getErr());
        buffer.putPrefixedString(span.getAgentId());
        final byte[] value = buffer.getBuffer();

        long acceptedTime = acceptedTimeService.getAcceptedTime();
        final byte[] distributedKey = crateRowKey(span, acceptedTime);
        Put put = new Put(distributedKey);

        put.add(APPLICATION_TRACE_INDEX_CF_TRACE, makeQualifier(span) , acceptedTim       , value);

		hbaseTemplate.put(APPLICATION_T        CE_INDEX, put);
	}

	private byte[] makeQualif       er(final TSpan span) {
		boolean       useIndexedQual       fier = false;
		byte[]          qualifier;

		if (useIndexedQualifier) {
			          inal Buffer columnName = new AutomaticBuffer(16);
			// FIXME put          ar not used in order to util          ze hbase column prefix filter
			columnName.pu          (span.getElapsed());
			colum       Name                    ut(SpanUtils.getVarTransactionId(span));
			qualifier          = columnName.getBuffer();
		} else {
			//             OLD
			// b       te[] transactionId = SpanUtils.getTransactionId(span);
			qualifier = SpanUtils.getVarTransactionId(span);
		}
		return qualifier;
	}
	
    private byte[] crateRowKey(TSpan span, long acceptedTime) {
        // distribute key evenly
        byte[] applicationTraceIndexRowKey = SpanUtils.getApplicationTraceIndexRowKey(span.getApplicationName(), acceptedTime);
        return rowKeyDistributor.getDistributedKey(applicationTraceIndexRowKey);
    }
}
