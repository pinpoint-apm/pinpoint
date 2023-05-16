package com.navercorp.pinpoint.web.authorization.controller;

import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.web.service.appmetric.ApplicationDataSourceService;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.util.TimeWindowSlotCentricSampler;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChart;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ApplicationDataSourceController {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ApplicationDataSourceService applicationDataSourceService;

    public ApplicationDataSourceController(ApplicationDataSourceService applicationDataSourceService) {
        this.applicationDataSourceService = applicationDataSourceService;
    }

    @GetMapping("/getApplicationStat/dataSource/chart")
    public List<StatChart> getAgentStatChart(@RequestParam("applicationId") String applicationId, @RequestParam("from") long from, @RequestParam("to") long to) {
        TimeWindowSlotCentricSampler sampler = new TimeWindowSlotCentricSampler();
        TimeWindow timeWindow = new TimeWindow(Range.between(from, to), sampler);
        try {
            return this.applicationDataSourceService.selectApplicationChart(applicationId, timeWindow);
        } catch (Exception e ) {
            logger.error("error" , e);
            throw e;
        }
    }
}
