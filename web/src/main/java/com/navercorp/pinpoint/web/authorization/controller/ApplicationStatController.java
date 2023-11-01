package com.navercorp.pinpoint.web.authorization.controller;

import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.web.service.appmetric.ApplicationStatChartService;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.util.TimeWindowSlotCentricSampler;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChart;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/getApplicationStat/{chartType}/chart")
@Validated
public class ApplicationStatController {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final Map<String, ApplicationStatChartService> chartServiceMap;

    public ApplicationStatController(List<ApplicationStatChartService> statChartServiceList) {
        this.chartServiceMap = build(statChartServiceList);
    }

    private Map<String, ApplicationStatChartService> build(List<ApplicationStatChartService> statChartServiceList) {
        final ChartTypeMappingBuilder<ApplicationStatChartService> mapping = new ChartTypeMappingBuilder<>();
        final Map<String, ApplicationStatChartService> map = mapping.build(statChartServiceList);

        for (final Map.Entry<String, ApplicationStatChartService> entry : map.entrySet()) {
            logger.info(
                    "ApplicationStatChartService chartType: {} {}",
                    entry.getKey(),
                    entry.getValue().getClass().getSimpleName()
            );
        }
        return map;
    }

    @GetMapping
    public StatChart getAgentStatChart(@RequestParam("applicationId") @NotBlank String applicationId,
                                       @PathVariable("chartType") @NotBlank String chartType,
                                       @RequestParam("from") @PositiveOrZero long from,
                                       @RequestParam("to") @PositiveOrZero long to) {
        final TimeWindowSlotCentricSampler sampler = new TimeWindowSlotCentricSampler();
        final TimeWindow timeWindow = new TimeWindow(Range.between(from, to), sampler);
        try {
            final ApplicationStatChartService<? extends StatChart> service = getService(chartType);
            return service.selectApplicationChart(applicationId, timeWindow);
        } catch (Exception e) {
            logger.error("error", e);
            throw e;
        }
    }

    private ApplicationStatChartService<? extends StatChart> getService(String chartType) {
        final ApplicationStatChartService<? extends StatChart> service = this.chartServiceMap.get(chartType);
        if (service == null) {
            throw new IllegalArgumentException("chartType pathVariable not found chartType:" + chartType);
        }
        return service;
    }

}
