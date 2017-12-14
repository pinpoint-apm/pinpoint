package com.navercorp.pinpoint.web.dao.hbase;

import com.google.common.annotations.Beta;
import com.google.common.collect.Lists;
import com.navercorp.pinpoint.common.hbase.HBaseTables;
import com.navercorp.pinpoint.common.hbase.HbaseOperations2;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.serializer.RowKeyEncoder;
import com.navercorp.pinpoint.common.server.bo.serializer.trace.v2.SpanEncoder;
import com.navercorp.pinpoint.common.util.TransactionId;
import com.navercorp.pinpoint.web.dao.TraceDao;
import com.navercorp.pinpoint.web.mapper.CellTraceMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.filter.BinaryPrefixComparator;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.QualifierFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
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

    private final Filter spanFilter = createSpanQualifierFilter();


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


    @Override
    public List<List<SpanBo>> selectSpans(List<TransactionId> transactionIdList) {
        return selectSpans(transactionIdList, selectSpansLimit);
    }

    List<List<SpanBo>> selectSpans(List<TransactionId> transactionIdList, int eachPartitionSize) {
        if (CollectionUtils.isEmpty(transactionIdList)) {
            return Collections.emptyList();
        }

        List<List<TransactionId>> splitTransactionIdList = partition(transactionIdList, eachPartitionSize);

        return partitionSelect(splitTransactionIdList, HBaseTables.TRACE_V2_CF_SPAN, spanFilter);
    }

    @Override
    public List<List<SpanBo>> selectAllSpans(List<TransactionId> transactionIdList) {
        return selectAllSpans(transactionIdList, selectAllSpansLimit);
    }

    List<List<SpanBo>> selectAllSpans(List<TransactionId> transactionIdList, int eachPartitionSize) {
        if (CollectionUtils.isEmpty(transactionIdList)) {
            return Collections.emptyList();
        }

        List<List<TransactionId>> partitionTransactionIdList = partition(transactionIdList, eachPartitionSize);

        return partitionSelect(partitionTransactionIdList, HBaseTables.TRACE_V2_CF_SPAN, null);
    }


    private List<List<TransactionId>> partition(List<TransactionId> transactionIdList, int maxTransactionIdListSize) {
        return Lists.partition(transactionIdList, maxTransactionIdListSize);
    }

    private List<List<SpanBo>> partitionSelect(List<List<TransactionId>> partitionTransactionIdList, byte[] columnFamily, Filter filter) {
        if (CollectionUtils.isEmpty(partitionTransactionIdList)) {
            return Collections.emptyList();
        }
        if (columnFamily == null) {
            throw new NullPointerException("columnFamily must not be null.");
        }

        List<List<SpanBo>> spanBoList = new ArrayList<>();
        for (List<TransactionId> transactionIdList : partitionTransactionIdList) {
            List<List<SpanBo>> partitionSpanList = select0(transactionIdList, columnFamily, filter);
            spanBoList.addAll(partitionSpanList);
        }
        return spanBoList;
    }

    private List<List<SpanBo>> select0(List<TransactionId> transactionIdList, byte[] columnFamily, Filter filter) {
        if (CollectionUtils.isEmpty(transactionIdList)) {
            return Collections.emptyList();
        }

        final List<Get> multiGet = new ArrayList<>(transactionIdList.size());
        for (TransactionId transactionId : transactionIdList) {
            final Get get = createGet(transactionId, columnFamily, filter);
            multiGet.add(get);
        }
        return template2.get(HBaseTables.TRACE_V2, multiGet, spanMapperV2);
    }

    private Get createGet(TransactionId transactionId, byte[] columnFamily, Filter filter) {

        byte[] transactionIdRowKey = rowKeyEncoder.encodeRowKey(transactionId);
        final Get get = new Get(transactionIdRowKey);

        get.addFamily(columnFamily);
        if (filter != null) {
            get.setFilter(filter);
        }
        return get;
    }


    public QualifierFilter createSpanQualifierFilter() {
        byte indexPrefix = SpanEncoder.TYPE_SPAN;
        BinaryPrefixComparator prefixComparator = new BinaryPrefixComparator(new byte[] {indexPrefix});
        QualifierFilter qualifierPrefixFilter = new QualifierFilter(CompareFilter.CompareOp.EQUAL, prefixComparator);
        return qualifierPrefixFilter;
    }


}
