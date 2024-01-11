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

import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.SearchOption;

import java.util.Objects;

/**
 * @author jaehong.kim
 */
public class MapServiceOption {
    private final Application sourceApplication;
    private final Range range;
    private final SearchOption searchOption;

    private final boolean simpleResponseHistogram;
    private final boolean useStatisticsAgentState;

    private MapServiceOption(Builder builder) {
        this.sourceApplication = builder.sourceApplication;
        this.range = builder.range;
        this.searchOption = builder.searchOption;
        this.simpleResponseHistogram = builder.simpleResponseHistogram;
        this.useStatisticsAgentState = builder.useStatisticsAgentState;
    }

    public Application getSourceApplication() {
        return sourceApplication;
    }

    public Range getRange() {
        return range;
    }

    public SearchOption getSearchOption() {
        return searchOption;
    }

    public boolean isSimpleResponseHistogram() {
        return simpleResponseHistogram;
    }

    public boolean isUseStatisticsAgentState() {
        return useStatisticsAgentState;
    }

    @Override
    public String toString() {
        return "MapServiceOption{" +
                "sourceApplication=" + sourceApplication +
                ", range=" + range +
                ", searchOption=" + searchOption +
                ", simpleResponseHistogram=" + simpleResponseHistogram +
                ", useStatisticsAgentState=" + useStatisticsAgentState +
                '}';
    }

    public static class Builder {
        private final Application sourceApplication;
        private final Range range;
        private final SearchOption searchOption;

        private boolean simpleResponseHistogram;
        // option
        private boolean useStatisticsAgentState;

        public Builder(Application sourceApplication, Range range, SearchOption searchOption) {
            this.sourceApplication = Objects.requireNonNull(sourceApplication, "sourceApplication");
            this.range = Objects.requireNonNull(range,"range");
            this.searchOption = Objects.requireNonNull(searchOption, "searchOption");
        }

        public Builder setSimpleResponseHistogram(boolean simpleResponseHistogram) {
            this.simpleResponseHistogram = simpleResponseHistogram;
            return this;
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