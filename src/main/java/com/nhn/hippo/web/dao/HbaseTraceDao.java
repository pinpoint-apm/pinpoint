package com.nhn.hippo.web.dao;

import com.nhn.hippo.web.vo.TraceId;
import com.profiler.common.bo.SpanBo;
import com.profiler.common.dto.thrift.Span;
import com.profiler.common.hbase.HBaseTables;
import com.profiler.common.hbase.HbaseOperations2;
import com.profiler.common.util.BytesUtils;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 *
 */
@Repository
public class HbaseTraceDao implements TraceDao {

    private final byte[] COLFAM_SPAN = HBaseTables.TRACES_CF_SPAN;

    private final byte[] COLFAM_ANNOTATION = HBaseTables.TRACES_CF_ANNOTATION;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private HbaseOperations2 template2;

    @Autowired
    @Qualifier("spanMapper")
    private RowMapper<List<SpanBo>> spanMapper;

    @Autowired
    @Qualifier("spanAnnotationMapper")
    private RowMapper<List<SpanBo>> spanAnnotationMapper;

    @Override
    public List<SpanBo> selectSpan(UUID traceId) {
        byte[] uuidBytes = BytesUtils.longLongToBytes(traceId.getMostSignificantBits(), traceId.getLeastSignificantBits());
        return template2.get(HBaseTables.TRACES, uuidBytes, COLFAM_SPAN, spanMapper);
    }

    public List<SpanBo> selectSpanAndAnnotation(UUID traceId) {
        byte[] uuidBytes = BytesUtils.longLongToBytes(traceId.getMostSignificantBits(), traceId.getLeastSignificantBits());
        Get get = new Get(uuidBytes);
        get.addFamily(COLFAM_SPAN);
        get.addFamily(COLFAM_ANNOTATION);
        return template2.get(HBaseTables.TRACES, get, spanAnnotationMapper);
    }

    @Override
    public List<SpanBo> selectSpan(long traceIdMost, long traceIdLeast) {
        byte[] uuidBytes = BytesUtils.longLongToBytes(traceIdMost, traceIdLeast);
        return template2.get(HBaseTables.TRACES, uuidBytes, COLFAM_SPAN, spanMapper);
    }

    @Override
    public List<List<SpanBo>> selectSpans(List<UUID> traceIds) {
        List<Get> gets = new ArrayList<Get>(traceIds.size());
        for (UUID traceId : traceIds) {
            byte[] uuidBytes = BytesUtils.longLongToBytes(traceId.getMostSignificantBits(), traceId.getLeastSignificantBits());
            Get get = new Get(uuidBytes);
            get.addFamily(COLFAM_SPAN);
            gets.add(get);
        }
        return template2.get(HBaseTables.TRACES, gets, spanMapper);
    }

    @Override
    public List<List<SpanBo>> selectSpans(Set<TraceId> traceIds) {
        List<Get> gets = new ArrayList<Get>(traceIds.size());
        for (TraceId traceId : traceIds) {
            Get get = new Get(traceId.getBytes());
            get.addFamily(COLFAM_SPAN);
            gets.add(get);
        }
        return template2.get(HBaseTables.TRACES, gets, spanMapper);
    }

    public List<List<SpanBo>> selectSpansAndAnnotation(Set<TraceId> traceIds) {
        List<Get> gets = new ArrayList<Get>(traceIds.size());
        for (TraceId traceId : traceIds) {
            Get get = new Get(traceId.getBytes());
            get.addFamily(COLFAM_SPAN);
            get.addFamily(COLFAM_ANNOTATION);
            gets.add(get);
        }
        return template2.get(HBaseTables.TRACES, gets, spanAnnotationMapper);
    }
}
