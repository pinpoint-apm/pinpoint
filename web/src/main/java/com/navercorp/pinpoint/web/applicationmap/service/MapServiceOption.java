/*
 * Copyright 2023 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.web.applicationmap.service;

import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.SearchOption;

import java.util.List;
import java.util.Objects;

/**
 * @author jaehong.kim
 */
public class MapServiceOption {
    private final List<Application> sourceApplications;
    private final TimeWindow timeWindow;
    private final SearchOption searchOption;

    private final boolean useStatisticsAgentState;

    private MapServiceOption(Builder builder) {
        this.sourceApplications = builder.sourceApplications;
        this.timeWindow = builder.timeWindow;
        this.searchOption = builder.searchOption;
        this.useStatisticsAgentState = builder.useStatisticsAgentState;
    }

    public Application getSourceApplication() {
        return sourceApplications.get(0);
    }

    public List<Application> getSourceApplications() {
        return sourceApplications;
    }

    public int getSourceApplicationCount() {
        return sourceApplications.size();
    }

    public Range getRange() {
        return timeWindow.getWindowRange();
    }

    public TimeWindow getTimeWindow() {
        return timeWindow;
    }

    public SearchOption getSearchOption() {
        return searchOption;
    }

    public boolean isUseStatisticsAgentState() {
        return useStatisticsAgentState;
    }

    @Override
    public String toString() {
        return "MapServiceOption{" +
                "sourceApplications=" + sourceApplications +
                ", timeWindow=" + timeWindow +
                ", searchOption=" + searchOption +
                ", useStatisticsAgentState=" + useStatisticsAgentState +
                '}';
    }

    public static class Builder {
        private final List<Application> sourceApplications;
        private final TimeWindow timeWindow;
        private final SearchOption searchOption;

        // option
        private boolean useStatisticsAgentState;

        public Builder(Application sourceApplication, TimeWindow timeWindow, SearchOption searchOption) {
            this(List.of(Objects.requireNonNull(sourceApplication, "sourceApplication")), timeWindow, searchOption);
        }

        public Builder(List<Application> sourceApplications, TimeWindow timeWindow, SearchOption searchOption) {
            Objects.requireNonNull(sourceApplications, "sourceApplications");
            if (CollectionUtils.isEmpty(sourceApplications)) {
                throw new IllegalArgumentException("sourceApplications must not be empty");
            }
            this.sourceApplications = List.copyOf(sourceApplications);
            this.timeWindow = Objects.requireNonNull(timeWindow, "timeWindow");
            this.searchOption = Objects.requireNonNull(searchOption, "searchOption");
        }


        public Builder setUseStatisticsAgentState(boolean useStatisticsAgentState) {
            this.useStatisticsAgentState = useStatisticsAgentState;
            return this;
        }

        public MapServiceOption build() {
            return new MapServiceOption(this);
        }
    }
}