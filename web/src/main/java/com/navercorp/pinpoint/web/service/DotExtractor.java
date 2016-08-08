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
import com.navercorp.pinpoint.web.scatter.ScatterData;
import com.navercorp.pinpoint.web.vo.*;
import com.navercorp.pinpoint.web.vo.scatter.ApplicationScatterScanResult;
import com.navercorp.pinpoint.web.vo.scatter.Dot;
import com.navercorp.pinpoint.web.vo.scatter.ScatterScanResult;
import com.navercorp.pinpoint.common.util.TransactionId;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author emeroad
 */
public class DotExtractor {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Range range;
    private final ApplicationFactory applicationFactory;

    private final Map<Application, List<Dot>> dotMap = new HashMap<>();


    public DotExtractor(Range range, ApplicationFactory applicationFactory) {
        if (range == null) {
            throw new NullPointerException("range must not be null");
        }
        if (applicationFactory == null) {
            throw new NullPointerException("applicationFactory must not be null");
        }
        this.range = range;
        this.applicationFactory = applicationFactory;
    }

    public void addDot(SpanBo span) {
        if (span == null) {
            throw new NullPointerException("span must not be null");
        }

        Application spanApplication = this.applicationFactory.createApplication(span.getApplicationId(), span.getApplicationServiceType());
        final List<Dot> dotList = getDotList(spanApplication);

        final TransactionId transactionId = span.getTransactionId();
        final Dot dot = new Dot(transactionId, span.getCollectorAcceptTime(), span.getElapsed(), span.getErrCode(), span.getAgentId());
        dotList.add(dot);
        logger.trace("Application:{} Dot:{}", spanApplication, dot);
    }

    private List<Dot> getDotList(Application spanApplication) {
        List<Dot> dotList = this.dotMap.get(spanApplication);
        if (dotList == null) {
            dotList = new ArrayList<>();
            this.dotMap.put(spanApplication, dotList);
        }
        return dotList;
    }

    public List<ApplicationScatterScanResult> getApplicationScatterScanResult() {
        List<ApplicationScatterScanResult> applicationScatterScanResult = new ArrayList<>();
        for (Map.Entry<Application, List<Dot>> entry : this.dotMap.entrySet()) {
            List<Dot> dotList = entry.getValue();
            Application application = entry.getKey();
            ScatterScanResult scatterScanResult = new ScatterScanResult(range.getFrom(), range.getTo(), dotList);
            applicationScatterScanResult.add(new ApplicationScatterScanResult(application, scatterScanResult));
        }
        return applicationScatterScanResult;
    }

    public Map<Application, ScatterData> getApplicationScatterData(long from, long to, int xGroupUnitMillis, int yGroupUnitMillis) {
        Map<Application, ScatterData> applicationScatterDataMap = new HashMap<>();
        for (Map.Entry<Application, List<Dot>> entry : this.dotMap.entrySet()) {
            Application application = entry.getKey();
            List<Dot> dotList = entry.getValue();

            ScatterData scatterData = new ScatterData(from, to, xGroupUnitMillis, yGroupUnitMillis);
            scatterData.addDot(dotList);

            applicationScatterDataMap.put(application, scatterData);
        }
        return applicationScatterDataMap;
    }

}
