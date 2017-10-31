/*
 * Copyright 2016 Naver Corp.
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

package com.navercorp.pinpoint.web.vo.stat.chart;

import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.chart.Chart;
import com.navercorp.pinpoint.web.vo.chart.Point;

import java.util.Map;

/**
 * @author HyunGil Jeong
 * @author minwoo.jung
 */
public interface StatChartGroup {

    interface ChartType {

        String[] getSchema();
    }

    interface AgentChartType extends ChartType {

        String[] DEFAULT_SCHEMA = {"min", "max", "avg", "sum"};

        @Override
        default String[] getSchema() {
            return DEFAULT_SCHEMA;
        }
    }

    interface ApplicationChartType extends ChartType {

        String[] DEFAULT_SCHEMA = {"min", "minAgentId", "max", "maxAgentId", "avg"};

        @Override
        default String[] getSchema() {
            return DEFAULT_SCHEMA;
        }
    }

    TimeWindow getTimeWindow();

    Map<ChartType, Chart<? extends Point>> getCharts();
}
