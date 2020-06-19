/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.web.vo;

import com.navercorp.pinpoint.common.util.Assert;

/**
 * @author Taejin Koo
 */
public final class SpanHint {

    private final long collectorAcceptorTime;
    private final int responseTime;
    private final String applicationName;

    private final boolean isSet;

    public SpanHint() {
        this.collectorAcceptorTime = -1;
        this.responseTime = -1;
        this.applicationName = null;

        this.isSet = false;
    }

    public SpanHint(long collectorAcceptorTime, int responseTime, String applicationName) {
        Assert.isTrue(collectorAcceptorTime > 0, "collectorAcceptorTime must be 'collectorAcceptorTime > 0'");
        this.collectorAcceptorTime = collectorAcceptorTime;
        Assert.isTrue(responseTime >= 0, "responseTime must be 'responseTime >= 0'");
        this.responseTime = responseTime;
        this.applicationName = applicationName;

        this.isSet = true;
    }

    public long getCollectorAcceptorTime() {
        return collectorAcceptorTime;
    }

    public int getResponseTime() {
        return responseTime;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public boolean isSet() {
        return isSet;
    }

}
