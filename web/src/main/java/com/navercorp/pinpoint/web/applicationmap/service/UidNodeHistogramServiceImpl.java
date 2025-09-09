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

package com.navercorp.pinpoint.web.applicationmap.service;

import com.navercorp.pinpoint.uid.service.BaseApplicationUidService;
import com.navercorp.pinpoint.web.applicationmap.appender.histogram.DefaultNodeHistogramFactory;
import com.navercorp.pinpoint.web.applicationmap.appender.histogram.NodeHistogramFactory;
import com.navercorp.pinpoint.web.applicationmap.appender.histogram.datasource.MapUidApplicationResponseDatasource;
import com.navercorp.pinpoint.web.applicationmap.appender.histogram.datasource.WasNodeHistogramDataSource;
import com.navercorp.pinpoint.web.applicationmap.uid.hbase.MapSelfUidDao;

import java.util.Objects;

public class UidNodeHistogramServiceImpl implements NodeHistogramService {

    private final NodeHistogramService nodeHistogramService;
    private final WasNodeHistogramDataSource applicationHistogram;

    public UidNodeHistogramServiceImpl(NodeHistogramService nodeHistogramService,
                                       BaseApplicationUidService baseApplicationUidService,
                                       MapSelfUidDao selfUidDao) {
        this.nodeHistogramService = Objects.requireNonNull(nodeHistogramService, "nodeHistogramService");
        this.applicationHistogram = new MapUidApplicationResponseDatasource(baseApplicationUidService, selfUidDao);
    }

    @Override
    public NodeHistogramFactory getSimpleHistogram() {
        return nodeHistogramService.getSimpleHistogram();
    }

    @Override
    public NodeHistogramFactory getApplicationHistogram() {
        return new DefaultNodeHistogramFactory(applicationHistogram);
    }

    @Override
    public NodeHistogramFactory getAgentHistogram() {
        return nodeHistogramService.getAgentHistogram();
    }

}
