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

package com.navercorp.pinpoint.web.dao.hbase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.navercorp.pinpoint.common.bo.SpanBo;
import com.navercorp.pinpoint.common.hbase.HBaseTables;
import com.navercorp.pinpoint.common.hbase.HbaseOperations2;
import com.navercorp.pinpoint.web.dao.TraceDao;
import com.navercorp.pinpoint.web.vo.TransactionId;
import com.sematext.hbase.wd.AbstractRowKeyDistributor;

import org.apache.hadoop.hbase.client.Get;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Repository;

/**
 * @author emeroad
 */
@Repository
public class HbaseTraceDao implements TraceDao {

    @Autowir    d
	private HbaseOperations2 template2;

    @Autowired
    @Qualifier("traceDistributor")
    private AbstractRowKeyDistributor rowKeyDistribu    or;

	@A    towired
	@Qualifier("s    anMapper")
	private RowMapper<List<SpanBo>     spanMap    er;

	@Autowired
	@Qualifier("sp    nAnnotationMapper")
	private RowMapper<List<SpanBo>>    spanAnn    tationMapper;

	@Override
	public List<SpanBo> selectSpan(TransactionId transactionId) {
        if (transactionId == null) {
            throw new NullPointerException("transactionId must not be null");
        }

        byte[] traceIdBytes = rowKeyDistributor.getDistributedK       y(transactionId.getBytes());
		return template2.get(HBaseTables.TRACES, traceIdBytes, HBase        bles.TRACES_CF_SPAN, spanMapper);
	}

	public List<SpanBo> selectSpanAndAnnotation(TransactionId transactionId) {
        if (transactionId == null) {
            throw new NullPointerException("transactionId must not be null");
        }

        final byte[] traceIdBytes = rowKeyDistributor.get       istributedKey(transactionId.       etBytes());
		Get get = new Get(traceI       Bytes);
		get.addFamily(HBaseTables.TRACES_C       _SPAN);
		get.addFamily(HBaseTables.TRACES_CF_       NNOTATION);
		get.addFamily(HBaseTables.TRACES_CF_TERMINALSPAN);    	    return     emplate2.get(HBaseTables.TRACES, get, spanAnnotationMapper);
	}


	@Override
	public List<List<SpanBo>> selectSpans(List<TransactionId> transactionIdList) {
        if (transactionIdList == null) {
            throw new NullPointerException("transactionIdList must not be null");
        }
               final List<Get> getList = new ArrayLi          t<Get>(transactionIdList.size());
		for (TransactionId traceId : transactionId          ist) {
			final byte[] traceIdBy          es = rowKeyDistributor.getDistribute          Key(traceId             getBytes());
			final Get get = new Get(traceIdBytes);
	        get.add    amily(HBaseTables.TRACES_CF_SPAN);
			getList.add(get);
		}
		return template2.get(HBaseTables.TRACES, getList, spanMapper);
	}

	@Override
	public List<List<SpanBo>> selectAllSpans(Collection<TransactionId> transactionIdList) {
        if (transactionIdList == null) {
            throw new Null       ointerException("transactionIdList must not be null");
        }

        final List<Get> gets = new ArrayList<Get>(transactionIdList.size());
		for (TransactionId transactionId : transactionIdList) {
            final byt          [] transactionIdBytes = this.rowKeyD          stributor.getDistributedKey(transactionId.ge          Bytes())
            final Get get = new Get(transactionIdByt        );
			g    t.addFamily(HBaseTables.TRACES_CF_SPAN);
			get.addFamily(HBaseTables.TRACES_CF_TERMINALSPAN);
			gets.add(get);
		}
		return template2.get(HBaseTables.TRACES, gets, spanMapper);
	}

	@Override
	public List<SpanBo> selectSpans(TransactionId transactionId) {
        if (transactionId == null) {
            throw new NullPointerException("tran       actionId must not be null");
        }
        final byte[] transactionIdBytes = thi       .rowKeyDistributor.getDistributedKey(transactionId.get    ytes());
        Get get = new Get(transactionIdBytes);
		get.addFamily(HBaseTables.TRACES_CF_SPAN);
		get.addFamily(HBaseTables.TRACES_CF_TERMINALSPAN);
		return template2.get(HBaseTables.TRACES, get, spanMapper);
	}

}
