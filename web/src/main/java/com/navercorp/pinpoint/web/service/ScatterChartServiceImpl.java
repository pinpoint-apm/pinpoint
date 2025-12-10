/*
 * Copyright 2025 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.web.scatter.dao.ApplicationTraceIndexDao;
import com.navercorp.pinpoint.web.scatter.dao.TraceIndexDao;
import com.navercorp.pinpoint.web.scatter.ScatterData;
import com.navercorp.pinpoint.web.scatter.ScatterDataBuilder;
import com.navercorp.pinpoint.web.trace.dao.TraceDao;
import com.navercorp.pinpoint.web.trace.service.SpanService;
import com.navercorp.pinpoint.web.util.ListListUtils;
import com.navercorp.pinpoint.web.vo.GetTraceInfo;
import com.navercorp.pinpoint.web.vo.LimitedScanResult;
import com.navercorp.pinpoint.web.scatter.vo.Dot;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author netspider
 * @author emeroad
 */
@Service
public class ScatterChartServiceImpl implements ScatterChartService {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ApplicationTraceIndexDao applicationTraceIndexDao;
    private final TraceIndexDao traceIndexDao;

    private final TraceDao traceDao;

    private final SpanService spanService;

    public ScatterChartServiceImpl(ApplicationTraceIndexDao applicationTraceIndexDao,
                                   TraceIndexDao traceIndexDao,
                                   TraceDao traceDao,
                                   SpanService spanService) {
        this.applicationTraceIndexDao = Objects.requireNonNull(applicationTraceIndexDao, "applicationTraceIndexDao");
        this.traceIndexDao = Objects.requireNonNull(traceIndexDao, "applicationTraceIndexV2Dao");
        this.traceDao = Objects.requireNonNull(traceDao, "traceDao");
        this.spanService = Objects.requireNonNull(spanService, "spanService");
    }

    /**
     * Queries for details on dots selected from the scatter chart.
     */
    @Override
    public List<SpanBo> selectTransactionMetadata(final List<GetTraceInfo> getTraceInfoList) {
        Objects.requireNonNull(getTraceInfoList, "getTraceInfoList");

        final List<List<SpanBo>> selectedSpans = traceDao.selectSpans(getTraceInfoList);
        populateAgentNameListOfList(selectedSpans);

        return ListListUtils.toList(selectedSpans, getTraceInfoList.size());
    }

    @Override
    public List<SpanBo> selectTransactionMetadata(TransactionId transactionId) {
        final List<SpanBo> selectedSpans = traceDao.selectSpan(transactionId);
        populateAgentName(selectedSpans);
        return selectedSpans;
    }

    private void populateAgentNameListOfList(Collection<List<SpanBo>> listOfList) {
        if (CollectionUtils.isEmpty(listOfList)) {
            return;
        }
        final List<SpanBo> list = listOfList.stream().flatMap(List::stream).collect(Collectors.toList());
        populateAgentName(list);
    }

    private void populateAgentName(List<SpanBo> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        spanService.populateAgentName(list);
    }

    @Override
    public ScatterData selectScatterData(String applicationName, Range range, int xGroupUnit, int yGroupUnit, int limit, boolean backwardDirection) {
        Objects.requireNonNull(applicationName, "applicationName");
        Objects.requireNonNull(range, "range");
        LimitedScanResult<List<Dot>> scanResult = applicationTraceIndexDao.scanTraceScatterData(applicationName, range, limit, backwardDirection);

        ScatterDataBuilder builder = new ScatterDataBuilder(range.getFrom(), range.getTo(), xGroupUnit, yGroupUnit);
        builder.addDot(scanResult.scanData());
        return builder.build();
    }

    @Override
    public ScatterData selectScatterDataV2(int serviceUid, String applicationName, int serviceTypeCode, Range range, int xGroupUnit, int yGroupUnit, int limit, boolean backwardDirection) {
        Objects.requireNonNull(applicationName, "applicationName");
        Objects.requireNonNull(range, "range");
        LimitedScanResult<List<Dot>> scanResult = traceIndexDao.scanTraceScatterData(serviceUid, applicationName, serviceTypeCode, range, limit, backwardDirection);

        ScatterDataBuilder builder = new ScatterDataBuilder(range.getFrom(), range.getTo(), xGroupUnit, yGroupUnit);
        builder.addDot(scanResult.scanData());
        return builder.build();
    }

}
