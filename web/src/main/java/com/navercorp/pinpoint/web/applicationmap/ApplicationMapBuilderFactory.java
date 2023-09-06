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

package com.navercorp.pinpoint.web.applicationmap;

import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.web.applicationmap.appender.histogram.NodeHistogramAppenderFactory;
import com.navercorp.pinpoint.web.applicationmap.appender.server.ServerInfoAppenderFactory;


/**
 * @author HyunGil Jeong
 */
public class ApplicationMapBuilderFactory {

    private final NodeHistogramAppenderFactory nodeHistogramAppenderFactory;

    private final ServerInfoAppenderFactory serverInfoAppenderFactory;

    public ApplicationMapBuilderFactory(
            NodeHistogramAppenderFactory nodeHistogramAppenderFactory,
            ServerInfoAppenderFactory serverInfoAppenderFactory) {
        this.nodeHistogramAppenderFactory = nodeHistogramAppenderFactory;
        this.serverInfoAppenderFactory = serverInfoAppenderFactory;
    }

    public ApplicationMapBuilder createApplicationMapBuilder(Range range) {
        return new ApplicationMapBuilder(range, nodeHistogramAppenderFactory, serverInfoAppenderFactory);
    }
}
