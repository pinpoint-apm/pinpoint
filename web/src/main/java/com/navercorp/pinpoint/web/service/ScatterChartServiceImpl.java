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

package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.util.TransactionId;
import com.navercorp.pinpoint.web.dao.ApplicationTraceIndexDao;
import com.navercorp.pinpoint.web.dao.TraceDao;
import com.navercorp.pinpoint.web.filter.Filter;
import com.navercorp.pinpoint.web.scatter.ScatterData;
import com.navercorp.pinpoint.web.vo.Range;
import com.navercorp.pinpoint.web.vo.SelectedScatterArea;
import com.navercorp.pinpoint.web.vo.TransactionMetadataQuery;
import com.navercorp.pinpoint.web.vo.scatter.Dot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author netspider
 * @author emeroad
 */
@Service
public class ScatterChartServiceImpl implements ScatterChartService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ApplicationTraceIndexDao applicationTraceIndexDao;

    @Autowired
    @Qualifier("hbaseTraceDaoFactory")
    private TraceDao traceDao;

    @Override
    public List<Dot> selectScatterData(String applicationName, SelectedScatterArea area, TransactionId offsetTransactionId, int offsetTransactionElapsed, int limit) {
        if (applicationName == null) {
            throw new NullPointerException("applicationName must not be null");
        }
        if (area == null) {
            throw new NullPointerException("area must not be null");
        }
        return applicationTraceIndexDao.scanTraceScatter(applicationName, area, offsetTransactionId, offsetTransactionElapsed, limit);
    }

    @Override
    public List<Dot> selectScatterData(List<TransactionId> transactionIdList, String applicationName, Filter filter) {
        if (transactionIdList == null) {
            throw new NullPointerException("transactionIdList must not be null");
        }
        if (applicationName == null) {
            throw new NullPointerException("applicationName must not be null");
        }
        if (filter == null) {
            throw new NullPointerException("filter must not be null");
        }

        final List<List<SpanBo>> traceList = traceDao.selectAllSpans(transactionIdList);

        final List<Dot> result = new ArrayList<>();

        for (List<SpanBo> trace : traceList) {
            if (!filter.include(trace)) {
                continue;
            }

            for (SpanBo span : trace) {
                if (applicationName.equals(span.getApplicationId())) {
                    final TransactionId transactionId = span.getTransactionId();
                    final Dot dot = new Dot(transactionId, span.getCollectorAcceptTime(), span.getElapsed(), span.getErrCode(), span.getAgentId());
                    result.add(dot);
                }
            }
        }

        return result;
    }

    /**
     * Queries for details on dots selected from the scatter chart.
     */
    @Override
    public List<SpanBo> selectTransactionMetadata(final TransactionMetadataQuery query) {
        if (query == null) {
            throw new NullPointerException("query must not be null");
        }
        final List<TransactionId> transactionIdList = query.getTransactionIdList();
        final List<List<SpanBo>> selectedSpans = traceDao.selectSpans(transactionIdList);


        final List<SpanBo> result = new ArrayList<>(query.size());
        int index = 0;
        for (List<SpanBo> spans : selectedSpans) {
            if (spans.isEmpty()) {
                // span data does not exist in storage - skip
            } else if (spans.size() == 1) {
                // case with a single unique span data
                result.add(spans.get(0));
            } else {
                // for recursive calls, we need to identify which of the spans was selected.
                // pick only the spans with the same transactionId, collectorAcceptor, and responseTime
                for (SpanBo span : spans) {

                    // should find the filtering condition with the correct index
                    final TransactionMetadataQuery.QueryCondition filterQueryCondition = query.getQueryConditionByIndex(index);

                    final TransactionId transactionId = span.getTransactionId();
                    final TransactionMetadataQuery.QueryCondition queryConditionKey = new TransactionMetadataQuery.QueryCondition(transactionId, span.getCollectorAcceptTime(), span.getElapsed());
                    if (queryConditionKey.equals(filterQueryCondition)) {
                        result.add(span);
                    }
                }
            }
            index++;
        }

        return result;
    }

    @Override
    public ScatterData selectScatterData(String applicationName, Range range, int xGroupUnit, int yGroupUnit, int limit, boolean backwardDirection) {
        if (applicationName == null) {
            throw new NullPointerException("applicationName must not be null");
        }
        if (range == null) {
            throw new NullPointerException("range must not be null");
        }
        return applicationTraceIndexDao.scanTraceScatterData(applicationName, range, xGroupUnit, yGroupUnit, limit, backwardDirection);
    }

    @Override
    public ScatterData selectScatterData(List<TransactionId> transactionIdList, String applicationName, Range range, int xGroupUnit, int yGroupUnit, Filter filter) {
        if (transactionIdList == null) {
            throw new NullPointerException("transactionIdList must not be null");
        }
        if (applicationName == null) {
            throw new NullPointerException("applicationName must not be null");
        }
        if (filter == null) {
            throw new NullPointerException("filter must not be null");
        }

        final List<List<SpanBo>> traceList = traceDao.selectAllSpans(transactionIdList);

        ScatterData scatterData = new ScatterData(range.getFrom(), range.getTo(), xGroupUnit, yGroupUnit);
        for (List<SpanBo> trace : traceList) {
            if (!filter.include(trace)) {
                continue;
            }

            for (SpanBo span : trace) {
                if (applicationName.equals(span.getApplicationId())) {
                    final TransactionId transactionId = span.getTransactionId();
                    final Dot dot = new Dot(transactionId, span.getCollectorAcceptTime(), span.getElapsed(), span.getErrCode(), span.getAgentId());
                    scatterData.addDot(dot);
                }
            }
        }

        return scatterData;
    }

}
