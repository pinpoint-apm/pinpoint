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

import com.navercorp.pinpoint.web.applicationmap.appender.histogram.DefaultNodeHistogramFactory;
import com.navercorp.pinpoint.web.applicationmap.appender.histogram.NodeHistogramFactory;
import com.navercorp.pinpoint.web.applicationmap.appender.histogram.SimplifiedNodeHistogramFactory;
import com.navercorp.pinpoint.web.applicationmap.appender.histogram.datasource.MapApplicationResponseNodeHistogramDataSource;
import com.navercorp.pinpoint.web.applicationmap.appender.histogram.datasource.MapResponseNodeHistogramDataSource;
import com.navercorp.pinpoint.web.applicationmap.appender.histogram.datasource.MapResponseSimplifiedNodeHistogramDataSource;
import com.navercorp.pinpoint.web.applicationmap.appender.histogram.datasource.WasNodeHistogramDataSource;
import com.navercorp.pinpoint.web.applicationmap.dao.MapResponseDao;

public class NodeHistogramServiceImpl implements NodeHistogramService {

    private final WasNodeHistogramDataSource simpleHistogram;

    private final WasNodeHistogramDataSource applicationHistogram;
    private final WasNodeHistogramDataSource agentHistogram;

    public NodeHistogramServiceImpl(MapResponseDao mapResponseDao) {
        this.simpleHistogram = new MapResponseSimplifiedNodeHistogramDataSource(mapResponseDao);
        this.applicationHistogram = new MapApplicationResponseNodeHistogramDataSource(mapResponseDao);
        this.agentHistogram = new MapResponseNodeHistogramDataSource(mapResponseDao);
    }

    @Override
    public NodeHistogramFactory getSimpleHistogram() {
        return new SimplifiedNodeHistogramFactory(simpleHistogram);
    }

    @Override
    public NodeHistogramFactory getApplicationHistogram() {
        return new DefaultNodeHistogramFactory(applicationHistogram);
    }

    @Override
    public NodeHistogramFactory getAgentHistogram() {
        return new DefaultNodeHistogramFactory(agentHistogram);
    }

}
