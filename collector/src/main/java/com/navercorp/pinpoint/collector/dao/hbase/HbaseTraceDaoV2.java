package com.navercorp.pinpoint.collector.dao.hbase;

import com.navercorp.pinpoint.collector.dao.TraceDao;
import com.navercorp.pinpoint.collector.dao.hbase.filter.SpanEventFilter;
import com.navercorp.pinpoint.common.hbase.HbaseOperations2;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.common.server.bo.serializer.trace.v2.SpanChunkSerializerV2;
import com.navercorp.pinpoint.common.server.bo.serializer.trace.v2.SpanSerializerV2;
import com.navercorp.pinpoint.common.server.util.AcceptedTimeService;
import com.navercorp.pinpoint.common.util.SpanUtils;
import com.navercorp.pinpoint.common.util.TransactionId;
import com.navercorp.pinpoint.common.util.TransactionIdUtils;
import com.navercorp.pinpoint.thrift.dto.TSpan;
import com.navercorp.pinpoint.thrift.dto.TSpanChunk;
import com.navercorp.pinpoint.thrift.dto.TSpanEvent;
import com.sematext.hbase.wd.AbstractRowKeyDistributor;
import org.apache.commons.collections.CollectionUtils;
import org.apache.hadoop.hbase.client.Put;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.navercorp.pinpoint.common.hbase.HBaseTables.TRACE_V2;

/**
 * @author Woonduk Kang(emeroad)
 */
@Repository
public class HbaseTraceDaoV2 implements TraceDao {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private HbaseOperations2 hbaseTemplate;

    @Autowired
    private AcceptedTimeService acceptedTimeService;

    @Autowired
    private SpanEventFilter spanEventFilter;

    @Autowired
    private SpanSerializerV2 spanSerializer;

    @Autowired
    private SpanChunkSerializerV2 spanChunkSerializer;

    @Autowired
    @Qualifier("traceV2Distributor")
    private AbstractRowKeyDistributor rowKeyDistributor;


    @Override
    public void insert(final TSpan span) {
        if (span == null) {
            throw new NullPointerException("span must not be null");
        }

        final SpanBo spanBo = new SpanBo(span);
        List<SpanEventBo> spanEventBoList = buildSpanEventList(span);
        spanBo.addSpanEventBoList(spanEventBoList);

        long acceptedTime = acceptedTimeService.getAcceptedTime();
        spanBo.setCollectorAcceptTime(acceptedTime);

        final byte[] rowKey = getDistributeRowKey(SpanUtils.getTransactionId(span));
        final Put put = new Put(rowKey, acceptedTime);

        this.spanSerializer.serialize(spanBo, put, null);


        boolean success = hbaseTemplate.asyncPut(TRACE_V2, put);
        if (!success) {
            hbaseTemplate.put(TRACE_V2, put);
        }

    }

    private List<SpanEventBo> buildSpanEventList(TSpan span) {
        final List<TSpanEvent> spanEventList = span.getSpanEventList();
        if (CollectionUtils.isEmpty(spanEventList)) {
            return Collections.emptyList();
        }

        List<SpanEventBo> spanEventBoList = new ArrayList<>(spanEventList.size());
        for (TSpanEvent spanEvent : spanEventList) {
            final SpanEventBo spanEventBo = new SpanEventBo(span, spanEvent);
            if (!spanEventFilter.filter(spanEventBo)) {
                continue;
            }
            spanEventBoList.add(spanEventBo);
        }


        return spanEventBoList;
    }

    private List<SpanEventBo> buildSpanEventBoList(TSpanChunk tSpanChunk) {
        List<TSpanEvent> spanEventList = tSpanChunk.getSpanEventList();
        if (CollectionUtils.isEmpty(spanEventList)) {
            return new ArrayList<>();
        }
        List<SpanEventBo> spanEventBoList = new ArrayList<>(spanEventList.size());
        for (TSpanEvent tSpanEvent : spanEventList) {
            SpanEventBo spanEventBo = new SpanEventBo(tSpanChunk, tSpanEvent);
            if (!spanEventFilter.filter(spanEventBo)) {
                continue;
            }
            spanEventBoList.add(spanEventBo);
        }

        return spanEventBoList;
    }

    private byte[] getDistributeRowKey(byte[] transactionId) {
        byte[] distributedKey = rowKeyDistributor.getDistributedKey(transactionId);
        return distributedKey;
    }



    @Override
    public void insertSpanChunk(TSpanChunk spanChunk) {
        SpanChunkBo spanChunkBo = buildSpanChunkBo(spanChunk);

        final byte[] rowKey = getDistributeRowKey(SpanUtils.getTransactionId(spanChunk));
        final long acceptedTime = acceptedTimeService.getAcceptedTime();
        final Put put = new Put(rowKey, acceptedTime);

        final List<TSpanEvent> spanEventBoList = spanChunk.getSpanEventList();
        if (CollectionUtils.isEmpty(spanEventBoList)) {
            return;
        }

        this.spanChunkSerializer.serialize(spanChunkBo, put, null);

        if (!put.isEmpty()) {
            boolean success = hbaseTemplate.asyncPut(TRACE_V2, put);
            if (!success) {
                hbaseTemplate.put(TRACE_V2, put);
            }
        }
    }

    public SpanChunkBo buildSpanChunkBo(TSpanChunk tSpanChunk) {
        SpanChunkBo spanChunkBo = new SpanChunkBo();
        spanChunkBo.setAgentId(tSpanChunk.getAgentId());
        spanChunkBo.setApplicationId(tSpanChunk.getApplicationName());
        spanChunkBo.setAgentStartTime(tSpanChunk.getAgentStartTime());

        final TransactionId transactionId = TransactionIdUtils.parseTransactionId(tSpanChunk.getTransactionId());
        final String traceAgentId = transactionId.getAgentId();
        if (traceAgentId == null) {
            spanChunkBo.setTraceAgentId(spanChunkBo.getAgentId());
        } else {
            spanChunkBo.setTraceAgentId(traceAgentId);
        }
        spanChunkBo.setTraceAgentStartTime(transactionId.getAgentStartTime());
        spanChunkBo.setTraceTransactionSequence(transactionId.getTransactionSequence());

        spanChunkBo.setSpanId(tSpanChunk.getSpanId());

        List<SpanEventBo> spanEventBoList = buildSpanEventBoList(tSpanChunk);
        spanChunkBo.addSpanEventBoList(spanEventBoList);
        return spanChunkBo;
    }




}
