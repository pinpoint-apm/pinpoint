/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.dao.hbase;

import com.google.common.collect.Lists;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.hbase.HBaseTables;
import com.navercorp.pinpoint.common.hbase.HbaseOperations2;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.server.bo.serializer.RowKeyEncoder;
import com.navercorp.pinpoint.common.util.TransactionId;
import com.navercorp.pinpoint.web.dao.TraceDao;
import com.navercorp.pinpoint.web.mapper.CellTraceMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.hadoop.hbase.client.Get;
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
 * @author emeroad
 */
@Repository
public class HbaseTraceDao implements TraceDao {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private HbaseOperations2 template2;


    @Autowired
    @Qualifier("traceRowKeyEncoderV1")
    private RowKeyEncoder<TransactionId> rowKeyDecoder;


    private RowMapper<List<SpanBo>> spanMapper;


    @Value("#{pinpointWebProps['web.hbase.selectSpans.limit'] ?: 500}")
    private int selectSpansLimit;

    @Value("#{pinpointWebProps['web.hbase.selectAllSpans.limit'] ?: 500}")
    private int selectAllSpansLimit;

    @Autowired
    @Qualifier("spanMapper")
    public void setSpanMapper(RowMapper<List<SpanBo>> spanMapper) {
        final Logger logger = LoggerFactory.getLogger(spanMapper.getClass());
        if (logger.isDebugEnabled()) {
            spanMapper = CellTraceMapper.wrap(spanMapper);
        }
        this.spanMapper = spanMapper;
    }

    @Override
    public List<SpanBo> selectSpan(TransactionId transactionId) {
        if (transactionId == null) {
            throw new NullPointerException("transactionId must not be null");
        }
        byte[] transactionIdRowKey = rowKeyDecoder.encodeRowKey(transactionId);

        Get get = new Get(transactionIdRowKey);
        get.addFamily(HBaseTables.TRACES_CF_SPAN);
        get.addFamily(HBaseTables.TRACES_CF_ANNOTATION);
        get.addFamily(HBaseTables.TRACES_CF_TERMINALSPAN);
        return template2.get(HBaseTables.TRACES, get, spanMapper);
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

        List<byte[]> hBaseFamilyList = new ArrayList<>(1);
        hBaseFamilyList.add(HBaseTables.TRACES_CF_SPAN);

        return partitionSelect(splitTransactionIdList, hBaseFamilyList);
    }

    @Override
    public List<List<SpanBo>> selectAllSpans(List<TransactionId> transactionIdList) {
        return selectAllSpans(transactionIdList, selectAllSpansLimit);
    }


    List<List<SpanBo>> selectAllSpans(List<TransactionId> transactionIdList, int eachPartitionSize) {
        if (CollectionUtils.isEmpty(transactionIdList)) {
            return Collections.emptyList();
        }

        List<List<TransactionId>> splitTransactionIdList = partition(transactionIdList, eachPartitionSize);

        List<byte[]> hBaseFamilyList = new ArrayList<>(2);
        hBaseFamilyList.add(HBaseTables.TRACES_CF_SPAN);
        hBaseFamilyList.add(HBaseTables.TRACES_CF_TERMINALSPAN);

        return partitionSelect(splitTransactionIdList, hBaseFamilyList);
    }


    private List<List<TransactionId>> partition(List<TransactionId> transactionIdList, int eachPartitionSize) {
        return Lists.partition(transactionIdList, eachPartitionSize);
    }

    private List<List<SpanBo>> partitionSelect(List<List<TransactionId>> splitTransactionIdList, List<byte[]> hBaseFamilyList) {
        if (CollectionUtils.isEmpty(splitTransactionIdList)) {
            return Collections.emptyList();
        }
        if (hBaseFamilyList == null) {
            throw new NullPointerException("hBaseFamilyList must not be null.");
        }

        List<List<SpanBo>> spanBoList = new ArrayList<>();
        for (List<TransactionId> transactionIdList : splitTransactionIdList) {
            List<List<SpanBo>> partitionSpanList = select0(transactionIdList, hBaseFamilyList);
            spanBoList.addAll(partitionSpanList);
        }
        return spanBoList;
    }

    private List<List<SpanBo>> select0(List<TransactionId> transactionIdList, List<byte[]> hBaseFamilyList) {
        if (CollectionUtils.isEmpty(transactionIdList)) {
            return Collections.emptyList();
        }



        final List<Get> multiGet = new ArrayList<>(transactionIdList.size());
        for (TransactionId transactionId : transactionIdList) {
            final Get get = createGet(transactionId, hBaseFamilyList);
            multiGet.add(get);
        }
        return template2.get(HBaseTables.TRACES, multiGet, spanMapper);
    }

    private Get createGet(TransactionId transactionId, List<byte[]> hBaseFamilyList) {
        byte[] transactionIdRowKey = rowKeyDecoder.encodeRowKey(transactionId);
        final Get get = new Get(transactionIdRowKey);
        addFamily(get, hBaseFamilyList);
        return get;
    }

    private void addFamily(Get get, List<byte[]> hBaseFamilyList) {
        for (byte[] hbaseFamily : hBaseFamilyList) {
            get.addFamily(hbaseFamily);
        }
    }


}
