/*
 * Copyright 2019 NAVER Corp.
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

import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.web.scatter.ScatterData;
import com.navercorp.pinpoint.web.scatter.ScatterDataBuilder;
import com.navercorp.pinpoint.web.vo.*;
import com.navercorp.pinpoint.web.vo.scatter.ApplicationScatterScanResult;
import com.navercorp.pinpoint.web.vo.scatter.Dot;
import com.navercorp.pinpoint.web.vo.scatter.ScatterScanResult;
import com.navercorp.pinpoint.common.profiler.util.TransactionId;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author emeroad
 * @author HyunGil Jeong
 */
public class DotExtractor {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Map<Application, List<Dot>> dotMap = new HashMap<>(128);


    public DotExtractor() {
    }

    public void addDot(Application application, Dot dot) {
        Objects.requireNonNull(application, "application");
        Objects.requireNonNull(dot, "dot");

        final List<Dot> dotList = getDotList(application);
        dotList.add(dot);
        logger.trace("Application:{} Dot:{}", application, dot);
    }

    public Dot newDot(SpanBo span) {
        Objects.requireNonNull(span, "span");

        final TransactionId transactionId = span.getTransactionId();
        return new Dot(transactionId, span.getCollectorAcceptTime(), span.getElapsed(), span.getErrCode(), span.getAgentId());
    }

    private List<Dot> getDotList(Application spanApplication) {
        List<Dot> dotList = this.dotMap.computeIfAbsent(spanApplication, k -> new ArrayList<>());
        return dotList;
    }

    public List<ApplicationScatterScanResult> getApplicationScatterScanResult(long from, long to) {
        List<ApplicationScatterScanResult> applicationScatterScanResult = new ArrayList<>();
        for (Map.Entry<Application, List<Dot>> entry : this.dotMap.entrySet()) {
            List<Dot> dotList = entry.getValue();
            Application application = entry.getKey();
            ScatterScanResult scatterScanResult = new ScatterScanResult(from, to, dotList);
            applicationScatterScanResult.add(new ApplicationScatterScanResult(application, scatterScanResult));
        }
        return applicationScatterScanResult;
    }

    public Map<Application, ScatterData> getApplicationScatterData(long from, long to, int xGroupUnitMillis, int yGroupUnitMillis) {
        Map<Application, ScatterData> applicationScatterDataMap = new HashMap<>();
        for (Map.Entry<Application, List<Dot>> entry : this.dotMap.entrySet()) {
            Application application = entry.getKey();
            List<Dot> dotList = entry.getValue();

            ScatterDataBuilder scatterData = new ScatterDataBuilder(from, to, xGroupUnitMillis, yGroupUnitMillis);
            scatterData.addDot(dotList);

            applicationScatterDataMap.put(application, scatterData.build());
        }
        return applicationScatterDataMap;
    }

}
