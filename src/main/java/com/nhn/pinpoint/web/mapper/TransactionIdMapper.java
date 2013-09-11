package com.nhn.pinpoint.web.mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.nhn.pinpoint.common.hbase.HBaseTables;
import com.nhn.pinpoint.web.vo.TransactionId;
import com.nhn.pinpoint.common.util.BytesUtils;
import com.sematext.hbase.wd.AbstractRowKeyDistributor;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class TransactionIdMapper implements RowMapper<List<TransactionId>> {
//    @Autowired
//    private AbstractRowKeyDistributor rowKeyDistributor;
	@Override
	public List<TransactionId> mapRow(Result result, int rowNum) throws Exception {
		if (result == null) {
			return Collections.emptyList();
		}

		KeyValue[] raw = result.raw();

		List<TransactionId> traceIdList = new ArrayList<TransactionId>(raw.length);

		for (KeyValue kv : raw) {
            byte[] buffer = kv.getBuffer();
            int qualifierOffset = kv.getQualifierOffset();
            // key값만큼 1증가 시킴
            TransactionId traceId = new TransactionId(buffer, qualifierOffset);

            traceIdList.add(traceId);
		}

		return traceIdList;
	}
}
