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

import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.hbase.HBaseTables;
import com.navercorp.pinpoint.common.hbase.HbaseOperations2;
import com.navercorp.pinpoint.common.hbase.RowMapper;
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
import java.util.Arrays;
import java.util.Collection;
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
    @Qualifier("transactionIdRowKeyEncoderV1")
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
        return template2.get(HBaseTables.TRACES, transactionIdRowKey, HBaseTables.TRACES_CF_SPAN, spanMapper);
    }

    public List<SpanBo> selectSpanAndAnnotation(TransactionId transactionId) {
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

    public List<List<SpanBo>> selectSpans(List<TransactionId> transactionIdList, int hBaseGetLimitSize) {
        if (transactionIdList == null) {
            throw new NullPointerException("transactionIdList must not be null");
        }

        List<List<TransactionId>> splitTransactionIdList = splitTransactionIdList(transactionIdList, hBaseGetLimitSize);

        List<byte[]> hBaseFamilyList = new ArrayList<>(1);
        hBaseFamilyList.add(HBaseTables.TRACES_CF_SPAN);

        return getSpans(splitTransactionIdList, hBaseFamilyList);
    }

    @Override
    public List<List<SpanBo>> selectAllSpans(Collection<TransactionId> transactionIdList) {
        return selectAllSpans(transactionIdList, selectAllSpansLimit);
    }

    public List<List<SpanBo>> selectAllSpans(Collection<TransactionId> transactionIdList, int hBaseGetLimitSize) {
        if (transactionIdList == null) {
            throw new NullPointerException("transactionIdList must not be null");
        }

        List<List<TransactionId>> splitTransactionIdList = splitTransactionIdList(collectionToList(transactionIdList), hBaseGetLimitSize);

        List<byte[]> hBaseFamilyList = new ArrayList<>(2);
        hBaseFamilyList.add(HBaseTables.TRACES_CF_SPAN);
        hBaseFamilyList.add(HBaseTables.TRACES_CF_TERMINALSPAN);

        return getSpans(splitTransactionIdList, hBaseFamilyList);
    }

    private List<TransactionId> collectionToList(Collection<TransactionId> transactionIdList) {
        TransactionId[] transactionIds = new TransactionId[transactionIdList.size()];
        transactionIdList.toArray(transactionIds);

        return Arrays.asList(transactionIds);
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

    private List<List<SpanBo>> getSpans(List<List<TransactionId>> splitTransactionIdList, List<byte[]> hBaseFamiliyList) {
        if (splitTransactionIdList == null || splitTransactionIdList.isEmpty()) {
            return Collections.emptyList();
        }

        List<List<SpanBo>> spanBoList = new ArrayList<>();
        for (List<TransactionId> transactionIdList : splitTransactionIdList) {
            spanBoList.addAll(getSpans0(transactionIdList, hBaseFamiliyList));
        }
        return spanBoList;
    }

    private List<List<SpanBo>> getSpans0(List<TransactionId> transactionIdList, List<byte[]> hBaseFamiliyList) {
        if (transactionIdList == null || transactionIdList.isEmpty()) {
            return Collections.emptyList();
        }

        if (hBaseFamiliyList == null) {
            throw new NullPointerException("hBaseFamiliyList may not be null.");
        }

        final List<Get> getList = new ArrayList<>(transactionIdList.size());
        for (TransactionId transactionId : transactionIdList) {
            byte[] transactionIdRowKey = rowKeyDecoder.encodeRowKey(transactionId);
            final Get get = new Get(transactionIdRowKey);
            for (byte[] hbaseFamily : hBaseFamiliyList) {
                get.addFamily(hbaseFamily);
            }
            getList.add(get);
        }
        return template2.get(HBaseTables.TRACES, getList, spanMapper);
    }

    @Override
    public List<SpanBo> selectSpans(TransactionId transactionId) {
        if (transactionId == null) {
            throw new NullPointerException("transactionId must not be null");
        }
        byte[] transactionIdRowKey = rowKeyDecoder.encodeRowKey(transactionId);
        Get get = new Get(transactionIdRowKey);
        get.addFamily(HBaseTables.TRACES_CF_SPAN);
        get.addFamily(HBaseTables.TRACES_CF_TERMINALSPAN);
        return template2.get(HBaseTables.TRACES, get, spanMapper);
    }

}
