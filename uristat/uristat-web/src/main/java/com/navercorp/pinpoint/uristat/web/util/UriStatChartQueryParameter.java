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

import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.metric.web.util.QueryParameter;
import com.navercorp.pinpoint.metric.web.util.TimePrecision;

import java.util.concurrent.TimeUnit;

public class UriStatChartQueryParameter extends QueryParameter {
    private final String tenantId;
    private final String serviceName;
    private final String applicationName;
    private final String agentId;
    private final String uri;

    public String getTenantId() {
        return tenantId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getAgentId() {
        return agentId;
    }

    public String getUri() {
        return uri;
    }

    public boolean isApplicationStat() {
        return StringUtils.isEmpty(agentId);
    }


    protected UriStatChartQueryParameter(Builder builder) {
        super(builder.getRange(), builder.getTimePrecision(), builder.getLimit());
        this.tenantId = builder.tenantId;
        this.serviceName = builder.serviceName;
        this.applicationName = builder.applicationName;
        this.agentId = builder.agentId;
        this.uri = builder.uri;
    }

    public static class Builder extends QueryParameter.Builder<Builder> {
        private String tenantId;
        private String serviceName;
        private String applicationName;
        private String agentId;
        private String uri;

        @Override
        protected Builder self() {
            return this;
        }

        public Builder setTenantId(String tenantId) {
            this.tenantId = tenantId;
            return self();
        }
        public Builder setServiceName(String serviceName) {
            this.serviceName = serviceName;
            return self();
        }

        public Builder setApplicationName(String applicationName) {
            this.applicationName = applicationName;
            return self();
        }

        public Builder setAgentId(String agentId) {
            this.agentId = agentId;
            return self();
        }

        public Builder setUri(String uri) {
            this.uri = uri;
            return self();
        }

        public Builder setLimit(long limit) {
            if (limit > 200) {
                this.limit = 200;
            } else if (limit < 50) {
                this.limit = 50;
            } else {
                this.limit = limit;
            }
            return self();
        }

        @Override
        public UriStatChartQueryParameter build() {
            if (timePrecision == null) {
                this.timePrecision = TimePrecision.newTimePrecision(TimeUnit.MILLISECONDS, 30000);
            }

            if (limit == 0) {
                this.limit = estimateLimit();
            }

            return new UriStatChartQueryParameter(this);
        }
    }
}
