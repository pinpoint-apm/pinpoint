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

package com.navercorp.pinpoint.web.applicationmap.appender.histogram;

import com.navercorp.pinpoint.web.applicationmap.histogram.NodeHistogram;
import com.navercorp.pinpoint.web.dao.MapResponseDao;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.Range;
import com.navercorp.pinpoint.web.vo.ResponseHistogramBuilder;
import com.navercorp.pinpoint.web.vo.ResponseTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author HyunGil Jeong
 */
@Component
public class NodeHistogramAppenderFactory {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String mode;

    @Autowired
    public NodeHistogramAppenderFactory(@Value("#{pinpointWebProps['web.servermap.appender.mode'] ?: 'serial'}") String mode) {
        this.mode = mode;
    }

    public NodeHistogramAppender createAppender(MapResponseDao mapResponseDao) {
        NodeHistogramDataSource nodeHistogramDataSource = new NodeHistogramDataSource() {
            @Override
            public NodeHistogram createNodeHistogram(Application application, Range range) {
                List<ResponseTime> responseTimes = mapResponseDao.selectResponseTime(application, range);
                final NodeHistogram nodeHistogram = new NodeHistogram(application, range, responseTimes);
                return nodeHistogram;
            }
        };
        return from(nodeHistogramDataSource);
    }

    public NodeHistogramAppender createAppender(ResponseHistogramBuilder responseHistogramBuilder) {
        NodeHistogramDataSource nodeHistogramDataSource = new NodeHistogramDataSource() {
            @Override
            public NodeHistogram createNodeHistogram(Application application, Range range) {
                List<ResponseTime> responseTimes = responseHistogramBuilder.getResponseTimeList(application);
                final NodeHistogram nodeHistogram = new NodeHistogram(application, range, responseTimes);
                return nodeHistogram;
            }
        };
        return from(nodeHistogramDataSource);
    }

    public NodeHistogramAppender createEmptyAppender() {
        NodeHistogramDataSource nodeHistogramDataSource = new NodeHistogramDataSource() {
            @Override
            public NodeHistogram createNodeHistogram(Application application, Range range) {
                final NodeHistogram nodeHistogram = new NodeHistogram(application, range);
                return nodeHistogram;
            }
        };
        return from(nodeHistogramDataSource);
    }

    private NodeHistogramAppender from(NodeHistogramDataSource nodeHistogramDataSource) {
        logger.debug("NodeHistogramAppender mode : {}", mode);
        return new SerialNodeHistogramAppender(nodeHistogramDataSource);
    }
}
