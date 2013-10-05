package com.nhn.pinpoint.web.mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.springframework.stereotype.Component;

import com.nhn.pinpoint.common.hbase.KeepLastRowMapper;
import com.nhn.pinpoint.common.hbase.KeepLastRowValue;
import com.nhn.pinpoint.web.vo.TransactionId;

/**
 *
 */
@Component
public class TransactionIdMapper implements KeepLastRowMapper<List<TransactionId>> {

	// @Autowired
	// private AbstractRowKeyDistributor rowKeyDistributor;

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
	
	@Override
	public KeepLastRowValue<List<TransactionId>> mapRowAndReturnLastRow(Result result, int rowNum) throws Exception {
		if (result == null) {
			return new KeepLastRowValue<List<TransactionId>>(new ArrayList<TransactionId>(0), null);
		}
		KeyValue[] raw = result.raw();
		List<TransactionId> traceIdList = new ArrayList<TransactionId>(raw.length);
		KeyValue lastRow = null;
		for (KeyValue kv : raw) {
			lastRow = kv;
			byte[] buffer = kv.getBuffer();
			int qualifierOffset = kv.getQualifierOffset();
			// key값만큼 1증가 시킴
			TransactionId traceId = new TransactionId(buffer, qualifierOffset);
			traceIdList.add(traceId);
		}

		return new KeepLastRowValue<List<TransactionId>>(traceIdList, lastRow);
	}
}
