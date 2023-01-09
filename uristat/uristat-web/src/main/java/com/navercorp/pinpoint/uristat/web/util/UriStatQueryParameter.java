/*
 * Copyright 2022 NAVER Corp.
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

package com.navercorp.pinpoint.uristat.web.util;

import com.navercorp.pinpoint.metric.web.util.QueryParameter;
import com.navercorp.pinpoint.metric.web.util.TimePrecision;

import java.util.concurrent.TimeUnit;

public class UriStatQueryParameter extends QueryParameter {
    private final String tenantId;
    private final String serviceName;
    private final String applicationName;
    private final String agentId;
    private final String uri;

    protected UriStatQueryParameter(Builder builder) {
        super(builder.getRange(), builder.getTimePrecision(), builder.getLimit());
        this.tenantId = builder.tenantId;
        this.serviceName = builder.serviceName;
        this.applicationName = builder.applicationName;
        this.agentId = builder.agentId;
        this.uri = builder.uri;
    }

    public static class Builder extends QueryParameter.Builder {
        private String tenantId;
        private String serviceName;
        private String applicationName;
        private String agentId;
        private String uri;

        public void setTenantId(String tenantId) {
            this.tenantId = tenantId;
        }
        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }

        public void setApplicationName(String applicationName) {
            this.applicationName = applicationName;
        }

        public void setAgentId(String agentId) {
            this.agentId = agentId;
        }

        public void setUri(String uri) { this.uri = uri; }

        @Override
        public UriStatQueryParameter build() {
            if (timePrecision == null) {
                this.timePrecision = TimePrecision.newTimePrecision(TimeUnit.MILLISECONDS, 30000);
            }
            this.limit = estimateLimit();
            return new UriStatQueryParameter(this);
        }
    }
}
