/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.web.authorization.controller;

import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.common.server.util.timewindow.TimeWindow;
import com.navercorp.pinpoint.common.server.util.timewindow.TimeWindowSampler;
import com.navercorp.pinpoint.common.server.util.timewindow.TimeWindowSlotCentricSampler;
import com.navercorp.pinpoint.web.heatmap.service.EmptyHeatmapService;
import com.navercorp.pinpoint.web.heatmap.service.HeatmapChartService;
import com.navercorp.pinpoint.web.heatmap.view.HeatMapDataView;
import com.navercorp.pinpoint.web.heatmap.vo.HeatMapData;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * @author minwoo-jung
 */

@RestController
@RequestMapping("/api")
@Validated
public class HeatmapChartController {

    private final int MAX_TIMESLOT_COUNT = 30;
    private final TimeWindowSampler DEFAULT_TIME_WINDOW_SAMPLER = new TimeWindowSlotCentricSampler(10000L, MAX_TIMESLOT_COUNT);
    private final HeatmapChartService heatmapChartService;

    public HeatmapChartController(Optional<HeatmapChartService> heatmapChartService) {
        this.heatmapChartService = heatmapChartService.orElseGet(EmptyHeatmapService::new);
        //TODO : (minwoo) need to set rangeValidator
    }

    @GetMapping(value = "/getHeatmapAppData")
    public HeatMapDataView getHeatmapAppData(@RequestParam("applicationName") @NotBlank String applicationName,
                                  @RequestParam("from") @PositiveOrZero long from,
                                  @RequestParam("to") @PositiveOrZero long to,
                                  @RequestParam("minYAxis") @PositiveOrZero int minYAxis,
                                  @RequestParam("maxYAxis") @Positive int maxYAxis) {
        Range range = Range.between(from, to);
        TimeWindow timeWindow = getTimeWindow(range);
        HeatMapData heatMapData = heatmapChartService.getHeatmapAppData(applicationName, timeWindow, minYAxis, maxYAxis);
        return new HeatMapDataView(heatMapData);
    }

    private TimeWindow getTimeWindow(Range range) {
        return new TimeWindow(range, DEFAULT_TIME_WINDOW_SAMPLER);
    }

}
