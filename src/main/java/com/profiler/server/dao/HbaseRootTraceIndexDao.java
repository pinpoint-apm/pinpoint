package com.profiler.server.dao;

import com.profiler.common.dto.thrift.Span;
import com.profiler.common.hbase.HBaseTables;
import com.profiler.common.hbase.HbaseOperations2;
import com.profiler.common.util.SpanUtils;
import org.apache.hadoop.hbase.client.Put;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 */
public class HbaseRootTraceIndexDao implements RootTraceIndexDao {
    // 소스가 HbaeTraceIndexDao 동일함. 중복이므로 향후 합치는 방안강구.
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    private String tableName = HBaseTables.ROOT_TRACE_INDEX;
    private byte[] COLFAM_TRACE = HBaseTables.ROOT_TRACE_INDEX_CF_TRACE;
    private byte[] COLNAME_ID = HBaseTables.ROOT_TRACE_INDEX_CN_ID;

    public HbaseRootTraceIndexDao() {
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @Autowired
    private HbaseOperations2 hbaseTemplate;


    @Override
    public void insert(final Span rootSpan) {
        if (rootSpan.getParentSpanId() != -1) {
            logger.debug("invalid root span{}", rootSpan);
            return;
        }
        Put put = new Put(SpanUtils.getTraceIndexRowKey(rootSpan), rootSpan.getTimestamp());
        put.add(COLFAM_TRACE, COLNAME_ID, SpanUtils.getTraceId(rootSpan));

        hbaseTemplate.put(tableName, put);

    }
}
