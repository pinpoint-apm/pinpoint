package com.navercorp.pinpoint.web.dao.hbase;

import com.google.common.annotations.Beta;
import com.google.common.collect.Lists;
import com.navercorp.pinpoint.common.hbase.HBaseTables;
import com.navercorp.pinpoint.common.hbase.HbaseOperations2;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.serializer.RowKeyEncoder;
import com.navercorp.pinpoint.common.util.TransactionId;
import com.navercorp.pinpoint.web.dao.TraceDao;
import com.navercorp.pinpoint.web.mapper.CellTraceMapper;
import org.apache.hadoop.hbase.client.Get;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
@Beta
@Repository
public class HbaseTraceDaoV2 implements TraceDao {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private HbaseOperations2 template2;

    @Autowired
    @Qualifier("traceRowKeyEncoderV2")
    private RowKeyEncoder<TransactionId> rowKeyEncoder;


    private RowMapper<List<SpanBo>> spanMapperV2;


    @Value("#{pinpointWebProps['web.hbase.selectSpans.limit'] ?: 500}")
    private int selectSpansLimit;

    @Value("#{pinpointWebProps['web.hbase.selectAllSpans.limit'] ?: 500}")
    private int selectAllSpansLimit;

    @Autowired
    @Qualifier("spanMapperV2")
    public void setSpanMapperV2(RowMapper<List<SpanBo>> spanMapperV2) {
        final Logger logger = LoggerFactory.getLogger(spanMapperV2.getClass());
        if (logger.isDebugEnabled()) {
            spanMapperV2 = CellTraceMapper.wrap(spanMapperV2);
        }
        this.spanMapperV2 = spanMapperV2;
    }

    @Override
    public List<SpanBo> selectSpan(TransactionId transactionId) {
        if (transactionId == null) {
            throw new NullPointerException("transactionId must not be null");
        }

        byte[] transactionIdRowKey = rowKeyEncoder.encodeRowKey(transactionId);
        return template2.get(HBaseTables.TRACE_V2, transactionIdRowKey, HBaseTables.TRACE_V2_CF_SPAN, spanMapperV2);
    }


    @Deprecated
    @Override
    public List<SpanBo> selectSpanAndAnnotation(TransactionId transactionId) {
        if (transactionId == null) {
            throw new NullPointerException("transactionId must not be null");
        }

        return selectSpan(transactionId);
    }


    @Override
    public List<List<SpanBo>> selectSpans(List<TransactionId> transactionIdList) {
        return selectSpans(transactionIdList, selectSpansLimit);
    }

    public List<List<SpanBo>> selectSpans(List<TransactionId> transactionIdList, int hBaseGetLimitSize) {
        if (transactionIdList == null) {
            throw new NullPointerException("transactionIdList must not be null");
        }

        List<List<TransactionId>> splitTransactionIdList = splitTransactionIdList(transactionIdList, hBaseGetLimitSize);

        return getSpans(splitTransactionIdList, HBaseTables.TRACE_V2_CF_SPAN);
    }

    @Override
    public List<List<SpanBo>> selectAllSpans(Collection<TransactionId> transactionIdList) {
        return selectAllSpans(transactionIdList, selectAllSpansLimit);
    }

    public List<List<SpanBo>> selectAllSpans(Collection<TransactionId> transactionIdList, int hBaseGetLimitSize) {
        if (transactionIdList == null) {
            throw new NullPointerException("transactionIdList must not be null");
        }

        List<List<TransactionId>> splitTransactionIdList = splitTransactionIdList(Lists.newArrayList(transactionIdList), hBaseGetLimitSize);


        return getSpans(splitTransactionIdList, HBaseTables.TRACE_V2_CF_SPAN);
    }


    private List<List<TransactionId>> splitTransactionIdList(List<TransactionId> transactionIdList, int maxTransactionIdListSize) {
        if (transactionIdList == null || transactionIdList.isEmpty()) {
            return Collections.emptyList();
        }

        List<List<TransactionId>> splitTransactionIdList = new ArrayList<>();

        int index = 0;
        int endIndex = transactionIdList.size();
        while (index < endIndex) {
            int subListEndIndex = Math.min(index + maxTransactionIdListSize, endIndex);
            splitTransactionIdList.add(transactionIdList.subList(index, subListEndIndex));
            index = subListEndIndex;
        }

        return splitTransactionIdList;
    }

    private List<List<SpanBo>> getSpans(List<List<TransactionId>> splitTransactionIdList, byte[] columnFamily) {
        if (splitTransactionIdList == null || splitTransactionIdList.isEmpty()) {
            return Collections.emptyList();
        }

        List<List<SpanBo>> spanBoList = new ArrayList<>();
        for (List<TransactionId> transactionIdList : splitTransactionIdList) {
            spanBoList.addAll(getSpans0(transactionIdList, columnFamily));
        }
        return spanBoList;
    }

    private List<List<SpanBo>> getSpans0(List<TransactionId> transactionIdList, byte[] columnFamily) {
        if (transactionIdList == null || transactionIdList.isEmpty()) {
            return Collections.emptyList();
        }

        if (columnFamily == null) {
            throw new NullPointerException("columnFamily may not be null.");
        }

        final List<Get> getList = new ArrayList<>(transactionIdList.size());
        for (TransactionId transactionId : transactionIdList) {
            byte[] transactionIdRowKey = rowKeyEncoder.encodeRowKey(transactionId);
            final Get get = new Get(transactionIdRowKey);
            get.addFamily(columnFamily);

            getList.add(get);
        }
        return template2.get(HBaseTables.TRACE_V2, getList, spanMapperV2);
    }

    @Override
    public List<SpanBo> selectSpans(TransactionId transactionId) {
        return selectSpan(transactionId);
    }
}
