/*
 * Copyright 2024 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.metric.common.util;

import com.navercorp.pinpoint.common.server.util.timewindow.TimeWindow;

import java.util.ArrayList;
import java.util.List;

/**
 * @author minwoo-jung
 */
public class TimeUtils {

    public static List<Long> createTimeStampList(TimeWindow timeWindow) {
        List<Long> timestampList = new ArrayList<>(timeWindow.getWindowRangeCount());

        for (Long timestamp : timeWindow) {
            timestampList.add(timestamp);
        }

        return timestampList;
    }

}
