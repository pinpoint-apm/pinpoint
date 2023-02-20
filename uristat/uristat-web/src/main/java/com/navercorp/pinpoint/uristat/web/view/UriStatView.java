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
package com.navercorp.pinpoint.uristat.web.view;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.navercorp.pinpoint.uristat.web.model.UriStatGroup;
import com.navercorp.pinpoint.metric.web.util.TimeWindow;
import com.navercorp.pinpoint.metric.web.view.TimeSeriesView;
import com.navercorp.pinpoint.metric.web.view.TimeseriesValueGroupView;
import com.navercorp.pinpoint.uristat.web.model.UriStatHistogram;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UriStatView implements TimeSeriesView {

    private final List<Long> timestampList;

    private final List<TimeseriesValueGroupView> uriStats = new ArrayList<>();

    public UriStatView(String uri, TimeWindow timeWindow, List<UriStatHistogram> uriStats) {
        Objects.requireNonNull(timeWindow, "timeWindow");
        Objects.requireNonNull(uriStats, "uriStats");
        this.timestampList = createTimeStampList(timeWindow);
        if (uriStats.isEmpty()) {
            this.uriStats.add(UriStatGroup.EMPTY_URI_STAT_GROUP);
        } else {
            this.uriStats.add(new UriStatGroup(uri, timestampList.size(), timeWindow, uriStats));
        }
    }

    private List<Long> createTimeStampList(TimeWindow timeWindow) {
        List<Long> timestampList = new ArrayList<>((int) timeWindow.getWindowRangeCount());

        for (Long timestamp : timeWindow) {
            timestampList.add(timestamp);
        }

        return timestampList;
    }

    @Override
    public String getTitle() {
        return "uriStat";
    }

    @Override
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getUnit() {
        return null;
    }

    @Override
    public List<Long> getTimestamp() {
        return timestampList;
    }

    @Override
    public List<TimeseriesValueGroupView> getMetricValueGroups() {
        return uriStats;
    }

}
