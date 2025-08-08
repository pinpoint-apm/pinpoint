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
import com.navercorp.pinpoint.web.applicationmap.appender.histogram.datasource.ResponseHistogramsNodeHistogramDataSource;
import com.navercorp.pinpoint.web.applicationmap.appender.histogram.datasource.WasNodeHistogramDataSource;
import com.navercorp.pinpoint.web.applicationmap.dao.MapResponseDao;
import com.navercorp.pinpoint.web.vo.ResponseHistograms;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class NodeHistogramServiceImpl implements NodeHistogramService {

    private final MapResponseDao mapResponseDao;

    public NodeHistogramServiceImpl(MapResponseDao mapResponseDao) {
        this.mapResponseDao = Objects.requireNonNull(mapResponseDao, "mapResponseDao");
    }

    @Override
    public NodeHistogramFactory getSimpleHistogram() {
        WasNodeHistogramDataSource dataSource = new MapResponseSimplifiedNodeHistogramDataSource(mapResponseDao);
        return new SimplifiedNodeHistogramFactory(dataSource);
    }

    @Override
    public NodeHistogramFactory getApplicationHistogram() {
        WasNodeHistogramDataSource dataSource = new MapApplicationResponseNodeHistogramDataSource(mapResponseDao);
        return new DefaultNodeHistogramFactory(dataSource);
    }

    @Override
    public NodeHistogramFactory getAgentHistogram() {
        WasNodeHistogramDataSource wasNodeHistogramDataSource = new MapResponseNodeHistogramDataSource(mapResponseDao);
        return new DefaultNodeHistogramFactory(wasNodeHistogramDataSource);
    }

    @Override
    public NodeHistogramFactory getAgentHistogram(ResponseHistograms responseHistograms) {
        final WasNodeHistogramDataSource wasNodeHistogramDataSource = new ResponseHistogramsNodeHistogramDataSource(responseHistograms);
        return new DefaultNodeHistogramFactory(wasNodeHistogramDataSource);
    }
}
