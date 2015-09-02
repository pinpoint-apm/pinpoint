/*
 *
 *  * Copyright 2014 NAVER Corp.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 *
 */

package com.navercorp.pinpoint.web.vo;

import com.navercorp.pinpoint.thrift.dto.command.TCmdActiveThreadCountRes;
import com.navercorp.pinpoint.thrift.dto.command.TRouteResult;

/**
 * @Author Taejin Koo
 */
public class AgentActiveThreadStatus {

    private final String hostname;
    private final TRouteResult routeResult;
    private final TCmdActiveThreadCountRes activeThreadCount;

    public AgentActiveThreadStatus(String hostname, TRouteResult routeResult, TCmdActiveThreadCountRes activeThreadCount) {
        this.hostname = hostname;
        this.routeResult = routeResult;
        this.activeThreadCount = activeThreadCount;
    }

    public String getHostname() {
        return hostname;
    }

    public TRouteResult getRouteResult() {
        return routeResult;
    }

    public TRouteResult getRouteResult(TRouteResult defaultValue) {
        if (routeResult == null) {
            return defaultValue;
        }
        return routeResult;
    }

    public TCmdActiveThreadCountRes getActiveThreadCount() {
        return activeThreadCount;
    }

    public TCmdActiveThreadCountRes getActiveThreadStatus(TCmdActiveThreadCountRes defaultValue) {
        if (activeThreadCount == null) {
            return defaultValue;
        }
        return activeThreadCount;
    }

}
