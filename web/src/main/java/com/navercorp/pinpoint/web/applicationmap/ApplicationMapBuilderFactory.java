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

import com.navercorp.pinpoint.web.applicationmap.appender.histogram.NodeHistogramAppenderFactory;
import com.navercorp.pinpoint.web.applicationmap.appender.server.ServerInfoAppenderFactory;
import com.navercorp.pinpoint.web.vo.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author HyunGil Jeong
 */
@Component
public class ApplicationMapBuilderFactory {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String mode;

    private final NodeHistogramAppenderFactory nodeHistogramAppenderFactory;

    private final ServerInfoAppenderFactory serverInfoAppenderFactory;

    @Autowired
    public ApplicationMapBuilderFactory(
            @Value("#{pinpointWebProps['web.servermap.builder.mode'] ?: 'v1'}") String mode,
            NodeHistogramAppenderFactory nodeHistogramAppenderFactory,
            ServerInfoAppenderFactory serverInfoAppenderFactory) {
        this.mode = mode;
        this.nodeHistogramAppenderFactory = nodeHistogramAppenderFactory;
        this.serverInfoAppenderFactory = serverInfoAppenderFactory;
    }

    public ApplicationMapBuilder createApplicationMapBuilder(Range range) {
        logger.info("ApplicationMapBuilder mode : {}", mode);
        if (mode.equals("v2")) {
            return new ApplicationMapBuilderV2(range, nodeHistogramAppenderFactory, serverInfoAppenderFactory);
        }
        return new ApplicationMapBuilderV1(range);
    }
}
