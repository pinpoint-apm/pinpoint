/*
 * Copyright 2015 NAVER Corp.
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

package com.navercorp.pinpoint.collector.cluster.route.filter;

import com.navercorp.pinpoint.collector.cluster.route.ResponseEvent;
import com.navercorp.pinpoint.collector.service.AgentEventService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author HyunGil Jeong
 */
public class AgentEventHandlingFilter implements RouteFilter<ResponseEvent> {

    @Autowired
    private AgentEventService agentEventService;

    @Override
    public void doEvent(ResponseEvent event) {
        if (event == null) {
            return;
        }
        final long eventTimestamp = System.currentTimeMillis();
        this.agentEventService.handleResponseEvent(event, eventTimestamp);
    }

}
