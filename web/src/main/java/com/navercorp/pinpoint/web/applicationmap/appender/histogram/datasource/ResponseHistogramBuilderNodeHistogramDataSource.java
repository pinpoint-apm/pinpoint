/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.web.applicationmap.appender.histogram.datasource;

import com.navercorp.pinpoint.web.applicationmap.histogram.NodeHistogram;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.Range;
import com.navercorp.pinpoint.web.vo.ResponseHistogramBuilder;
import com.navercorp.pinpoint.web.vo.ResponseTime;

import java.util.List;

/**
 * @author HyunGil Jeong
 */
public class ResponseHistogramBuilderNodeHistogramDataSource implements WasNodeHistogramDataSource {

    private final ResponseHistogramBuilder responseHistogramBuilder;

    public ResponseHistogramBuilderNodeHistogramDataSource(ResponseHistogramBuilder responseHistogramBuilder) {
        if (responseHistogramBuilder == null) {
            throw new NullPointerException("responseHistogramBuilder must not be null");
        }
        this.responseHistogramBuilder = responseHistogramBuilder;
    }

    @Override
    public NodeHistogram createNodeHistogram(Application application, Range range) {
        List<ResponseTime> responseTimes = responseHistogramBuilder.getResponseTimeList(application);
        final NodeHistogram nodeHistogram = new NodeHistogram(application, range, responseTimes);
        return nodeHistogram;
    }
}
