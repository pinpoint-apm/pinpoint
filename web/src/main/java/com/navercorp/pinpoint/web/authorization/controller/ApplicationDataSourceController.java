package com.navercorp.pinpoint.web.authorization.controller;

import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.common.server.util.timewindow.TimeWindowSampler;
import com.navercorp.pinpoint.web.service.appmetric.ApplicationDataSourceService;
import com.navercorp.pinpoint.common.server.util.timewindow.TimeWindow;
import com.navercorp.pinpoint.common.server.util.timewindow.TimeWindowSlotCentricSampler;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChart;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@Validated
public class ApplicationDataSourceController {
    private final Logger logger = LogManager.getLogger(this.getClass());
    private final TimeWindowSampler defaultTimeWindowSampler = new TimeWindowSlotCentricSampler();

    private final ApplicationDataSourceService applicationDataSourceService;

    public ApplicationDataSourceController(ApplicationDataSourceService applicationDataSourceService) {
        this.applicationDataSourceService = applicationDataSourceService;
    }

    @GetMapping("/getApplicationStat/dataSource/chart")
    public List<StatChart> getAgentStatChart(
            @RequestParam("applicationId") @NotBlank String applicationId,
            @RequestParam("from") @PositiveOrZero long from,
            @RequestParam("to") @PositiveOrZero long to
    ) {
        TimeWindow timeWindow = new TimeWindow(Range.between(from, to), defaultTimeWindowSampler);
        try {
            return this.applicationDataSourceService.selectApplicationChart(applicationId, timeWindow);
        } catch (Exception e ) {
            logger.error("error" , e);
            throw e;
        }
    }
}
