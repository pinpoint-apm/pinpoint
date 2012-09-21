package com.profiler.server.dao;

import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;
import org.apache.thrift.TSerializer;

import com.profiler.common.dto.thrift.Span;
import com.profiler.common.hbase.HBaseClient;
import com.profiler.common.hbase.HBaseTables;
import com.profiler.common.util.SpanUtils;
import org.hsqldb.Table;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.hadoop.hbase.HbaseOperations;
import org.springframework.data.hadoop.hbase.TableCallback;

public class Traces {

	private final Logger logger = Logger.getLogger(this.getClass().getName());

	private final byte[] COLFAM_SPAN = Bytes.toBytes("Span");

    @Autowired
	private HBaseClient client;

    @Autowired
    private HbaseOperations hbaseTemplate;

	public boolean insert(final Span span) {
		try {
			final byte[] value = new TSerializer().serialize(span);
             // 이거 왜 put은 없지?
            hbaseTemplate.execute(HBaseTables.TRACES, new TableCallback<Object>() {
                @Override
                public Object doInTable(HTable table) throws Throwable {

                    Put put = new Put(SpanUtils.getTracesRowkey(span), span.getTimestamp());
			        put.add(COLFAM_SPAN, Bytes.toBytes(span.getSpanID()), value);
                    table.put(put);

                    return null;
                }
            });
			return true;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return false;
	}
}
