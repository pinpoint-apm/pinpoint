package com.profiler.server.dao;

import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

import com.profiler.common.dto.thrift.Span;
import com.profiler.common.hbase.HBaseTables;
import com.profiler.common.util.SpanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.hadoop.hbase.HbaseOperations;
import org.springframework.data.hadoop.hbase.TableCallback;

public class HbaseTraceIndex implements TraceIndex {

	private final Logger logger = Logger.getLogger(this.getClass().getName());

    private String tableName = HBaseTables.TRACE_INDEX;
	private byte[] COLFAM_TRACE = Bytes.toBytes("Trace");
	private byte[] COLNAME_ID = Bytes.toBytes("ID");

    public HbaseTraceIndex() {
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

//    @Autowired
//	private HBaseClient client;
    @Autowired
    private HbaseOperations hbaseTemplate;

	@Override
    public boolean insert(final Span span) {
            // 이거 왜 put은 없지?
        hbaseTemplate.execute(tableName, new TableCallback<Object>() {
            @Override
            public Object doInTable(HTable table) throws Throwable {

                Put put = new Put(SpanUtils.getTraceIndexRowKey(span), span.getTimestamp());
                put.add(COLFAM_TRACE, COLNAME_ID, SpanUtils.getTraceId(span));
                table.put(put);
                return null;
            }
        });
        return true;
	}
}
