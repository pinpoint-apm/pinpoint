/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.monitor.metric.rpc;

import com.navercorp.pinpoint.common.trace.ServiceType;

import java.util.Objects;

/**
 * @author emeroad
 */
public class ContextMetric {
    // Response time of WAS
    private final Histogram responseMetric;
    // Response time to unknown user
    private final Histogram userHistogram;

    private final ServiceType contextServiceType;

    // Response time to known peer
    private final AcceptHistogram acceptHistogram = new DefaultAcceptHistogram();

    public ContextMetric(ServiceType contextServiceType) {
        this.contextServiceType = Objects.requireNonNull(contextServiceType, "contextServiceType");

        this.responseMetric = new LongAdderHistogram(contextServiceType);
        this.userHistogram = new LongAdderHistogram(contextServiceType);
    }

    public void addResponseTime(int millis, boolean error) {
        this.responseMetric.addResponseTime(millis, error);
    }

    public void addAcceptHistogram(String parentApplicationName, short serviceType, int millis, boolean error) {
        Objects.requireNonNull(parentApplicationName, "parentApplicationName");

        this.acceptHistogram.addResponseTime(parentApplicationName, serviceType, millis, error);
    }

    public void addUserAcceptHistogram(int millis, boolean error) {
        this.userHistogram.addResponseTime(millis, error);
    }
}
