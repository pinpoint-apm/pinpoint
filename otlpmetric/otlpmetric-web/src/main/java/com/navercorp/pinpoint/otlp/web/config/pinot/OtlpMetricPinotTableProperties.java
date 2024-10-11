/*
 * Copyright 2024 NAVER Corp.
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

package com.navercorp.pinpoint.otlp.web.config.pinot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author minwoo-jung
 */
@Component
public class OtlpMetricPinotTableProperties {

    @Value("${pinot.otlpmetric.topic.double.count}")
    private int doubleTopicCount;
    @Value("${pinot.otlpmetric.topic.double.prefix}")
    private String doubleTopicPrefix;
    @Value("${pinot.otlpmetric.topic.double.padding.length}")
    private int doubleTopicPaddingLength;
    @Value("${pinot.otlpmetric.topic.long.count}")
    private int longTopicCount;
    @Value("${pinot.otlpmetric.topic.long.prefix}")
    private String longTopicPrefix;
    @Value("${pinot.otlpmetric.topic.long.padding.length}")
    private int longTopicPaddingLength;

    public int getDoubleTopicCount() {
        return doubleTopicCount;
    }

    public String getDoubleTopicPrefix() {
        return doubleTopicPrefix;
    }

    public int getDoubleTopicPaddingLength() {
        return doubleTopicPaddingLength;
    }

    public int getLongTopicCount() {
        return longTopicCount;
    }

    public String getLongTopicPrefix() {
        return longTopicPrefix;
    }

    public int getLongTopicPaddingLength() {
        return longTopicPaddingLength;
    }

}
