package com.navercorp.pinpoint.collector.dao.hbase;

import com.navercorp.pinpoint.collector.dao.TraceDao;
import com.navercorp.pinpoint.common.hbase.HbaseOperations2;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.common.server.bo.serializer.trace.v2.SpanChunkSerializerV2;
import com.navercorp.pinpoint.common.server.bo.serializer.trace.v2.SpanSerializerV2;
import com.navercorp.pinpoint.common.server.util.SpanUtils;
import com.sematext.hbase.wd.AbstractRowKeyDistributor;
import org.apache.commons.collections.CollectionUtils;
import org.apache.hadoop.hbase.client.Put;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

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
    private SpanSerializerV2 spanSerializer;

    @Autowired
    private SpanChunkSerializerV2 spanChunkSerializer;

    @Autowired
    @Qualifier("traceV2Distributor")
    private AbstractRowKeyDistributor rowKeyDistributor;


    @Override
    public void insert(final SpanBo spanBo) {
        if (spanBo == null) {
            throw new NullPointerException("spanBo must not be null");
        }


        long acceptedTime = spanBo.getCollectorAcceptTime();

        final byte[] rowKey = getDistributeRowKey(SpanUtils.getTransactionId(spanBo));
        final Put put = new Put(rowKey, acceptedTime);

        this.spanSerializer.serialize(spanBo, put, null);


        boolean success = hbaseTemplate.asyncPut(TRACE_V2, put);
        if (!success) {
            hbaseTemplate.put(TRACE_V2, put);
        }

    }



    private byte[] getDistributeRowKey(byte[] transactionId) {
        byte[] distributedKey = rowKeyDistributor.getDistributedKey(transactionId);
        return distributedKey;
    }



    @Override
    public void insertSpanChunk(SpanChunkBo spanChunkBo) {

        final byte[] rowKey = getDistributeRowKey(SpanUtils.getTransactionId(spanChunkBo));

        final long acceptedTime = spanChunkBo.getCollectorAcceptTime();
        final Put put = new Put(rowKey, acceptedTime);

        final List<SpanEventBo> spanEventBoList = spanChunkBo.getSpanEventBoList();
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





}
