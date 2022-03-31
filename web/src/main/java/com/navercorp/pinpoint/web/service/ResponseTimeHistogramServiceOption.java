/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.common.server.util.time.Range;

import java.util.List;
import java.util.Objects;

/**
 * @author jaehong.kim
 */
public class ResponseTimeHistogramServiceOption {
    private Application application;
    private Range range;
    private List<Application> fromApplications;
    private List<Application> toApplications;
    private boolean useStatisticsAgentState;

    private ResponseTimeHistogramServiceOption(Builder builder) {
        this.application = builder.application;
        this.range = builder.range;
        this.fromApplications = builder.fromApplications;
        this.toApplications = builder.toApplications;
        this.useStatisticsAgentState = builder.useStatisticsAgentState;
    }

    public Application getApplication() {
        return application;
    }

    public Range getRange() {
        return range;
    }

    public List<Application> getFromApplications() {
        return fromApplications;
    }

    public List<Application> getToApplications() {
        return toApplications;
    }

    public boolean isUseStatisticsAgentState() {
        return useStatisticsAgentState;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ResponseTimeHistogramServiceOption{");
        sb.append("application=").append(application);
        sb.append(", range=").append(range);
        sb.append(", fromApplications=").append(fromApplications);
        sb.append(", toApplications=").append(toApplications);
        sb.append(", useStatisticsAgentState=").append(useStatisticsAgentState);
        sb.append('}');
        return sb.toString();
    }

    public static class Builder {
        private Application application;
        private Range range;
        private List<Application> fromApplications;
        private List<Application> toApplications;

        private boolean useStatisticsAgentState;

        public Builder(Application application, Range range, List<Application> fromApplications, List<Application> toApplications) {
            this.application = Objects.requireNonNull(application, "application");
            this.range = Objects.requireNonNull(range, "range");
            this.fromApplications = Objects.requireNonNull(fromApplications, "fromApplications");
            this.toApplications = Objects.requireNonNull(toApplications, "toApplications");
        }

        public Builder setUseStatisticsAgentState(boolean useStatisticsAgentState) {
            this.useStatisticsAgentState = useStatisticsAgentState;
            return this;
        }

        public ResponseTimeHistogramServiceOption build() {
            return new ResponseTimeHistogramServiceOption(this);
        }
    }
}
