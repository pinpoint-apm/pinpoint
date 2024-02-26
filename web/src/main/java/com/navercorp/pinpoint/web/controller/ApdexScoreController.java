package com.navercorp.pinpoint.web.controller;

import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.web.applicationmap.histogram.ApdexScore;
import com.navercorp.pinpoint.web.component.ApplicationFactory;
import com.navercorp.pinpoint.web.service.ApdexScoreService;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.util.TimeWindowSampler;
import com.navercorp.pinpoint.web.util.TimeWindowSlotCentricSampler;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChart;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
@Validated
public class ApdexScoreController {
    private static final TimeWindowSampler APDEX_SCORE_TIME_WINDOW_SAMPLER = new TimeWindowSlotCentricSampler(60 * 1000, 200);

    private final ApplicationFactory applicationFactory;
    private final ApdexScoreService apdexScoreService;

    public ApdexScoreController(ApplicationFactory applicationFactory, ApdexScoreService apdexScoreService) {
        this.applicationFactory = Objects.requireNonNull(applicationFactory, "applicationFactory");
        this.apdexScoreService = Objects.requireNonNull(apdexScoreService, "apdexScoreService");
    }

    @GetMapping(value = "/getApdexScore")
    public ApdexScore getApdexScore(
            @RequestParam("applicationName") @NotBlank String applicationName,
            @RequestParam("serviceTypeCode") Short serviceTypeCode,
            @RequestParam("from") @PositiveOrZero long from,
            @RequestParam("to") @PositiveOrZero long to) {
        final Range range = Range.between(from, to);

        Application application = applicationFactory.createApplication(applicationName, serviceTypeCode);

        return apdexScoreService.selectApdexScoreData(application, range);
    }

    @GetMapping(value = "/getApdexScore", params = "serviceTypeName")
    public ApdexScore getApdexScore(
            @RequestParam("applicationName") @NotBlank String applicationName,
            @RequestParam("serviceTypeName") @NotBlank String serviceTypeName,
            @RequestParam("from") @PositiveOrZero long from,
            @RequestParam("to") @PositiveOrZero long to) {
        final Range range = Range.between(from, to);

        Application application = applicationFactory.createApplicationByTypeName(applicationName, serviceTypeName);

        return apdexScoreService.selectApdexScoreData(application, range);
    }

    @GetMapping(value = "/getApdexScore", params = {"agentId"})
    public ApdexScore getApdexScore(
            @RequestParam("applicationName") @NotBlank String applicationName,
            @RequestParam("serviceTypeCode") Short serviceTypeCode,
            @RequestParam("agentId") @NotBlank String agentId,
            @RequestParam("from") @PositiveOrZero long from,
            @RequestParam("to") @PositiveOrZero long to) {
        final Range range = Range.between(from, to);

        Application application = applicationFactory.createApplication(applicationName, serviceTypeCode);

        return apdexScoreService.selectApdexScoreData(application, agentId, range);
    }

    @GetMapping(value = "/getApdexScore", params = {"agentId", "serviceTypeName"})
    public ApdexScore getApdexScore(
            @RequestParam("applicationName") @NotBlank String applicationName,
            @RequestParam("serviceTypeName") @NotBlank String serviceTypeName,
            @RequestParam("agentId") @NotBlank String agentId,
            @RequestParam("from") @PositiveOrZero long from,
            @RequestParam("to") @PositiveOrZero long to) {
        final Range range = Range.between(from, to);

        Application application = applicationFactory.createApplicationByTypeName(applicationName, serviceTypeName);

        return apdexScoreService.selectApdexScoreData(application, agentId, range);
    }

    @GetMapping(value = "/getApplicationStat/apdexScore/chart")
    public StatChart<?> getApplicationApdexScoreChart(
            @RequestParam("applicationId") @NotBlank String applicationId,
            @RequestParam("serviceTypeCode") Short serviceTypeCode,
            @RequestParam("from") @PositiveOrZero long from,
            @RequestParam("to") @PositiveOrZero long to) {
        final Range range = Range.between(from, to);
        TimeWindow timeWindow = new TimeWindow(range, APDEX_SCORE_TIME_WINDOW_SAMPLER);

        Application application = applicationFactory.createApplication(applicationId, serviceTypeCode);

        return apdexScoreService.selectApplicationChart(application, range, timeWindow);
    }

    @GetMapping(value = "/getApplicationStat/apdexScore/chart", params = "serviceTypeName")
    public StatChart<?> getApplicationApdexScoreChart(
            @RequestParam("applicationId") @NotBlank String applicationId,
            @RequestParam("serviceTypeName") @NotBlank String serviceTypeName,
            @RequestParam("from") @PositiveOrZero long from,
            @RequestParam("to") @PositiveOrZero long to) {
        final Range range = Range.between(from, to);
        TimeWindow timeWindow = new TimeWindow(range, APDEX_SCORE_TIME_WINDOW_SAMPLER);

        Application application = applicationFactory.createApplicationByTypeName(applicationId, serviceTypeName);

        return apdexScoreService.selectApplicationChart(application, range, timeWindow);
    }

    @GetMapping(value = "/getAgentStat/apdexScore/chart")
    public StatChart<?> getAgentApdexScoreChart(
            @RequestParam("applicationId") @NotBlank String applicationId,
            @RequestParam("serviceTypeCode") Short serviceTypeCode,
            @RequestParam("agentId") @NotBlank String agentId,
            @RequestParam("from") @PositiveOrZero long from,
            @RequestParam("to") @PositiveOrZero long to) {
        final Range range = Range.between(from, to);
        TimeWindow timeWindow = new TimeWindow(range, APDEX_SCORE_TIME_WINDOW_SAMPLER);

        Application application = applicationFactory.createApplication(applicationId, serviceTypeCode);

        return apdexScoreService.selectAgentChart(application, range, timeWindow, agentId);
    }

    @GetMapping(value = "/getAgentStat/apdexScore/chart", params = "serviceTypeName")
    public StatChart<?> getAgentApdexScoreChart(
            @RequestParam("applicationId") @NotBlank String applicationId,
            @RequestParam("serviceTypeName") @NotBlank String serviceTypeName,
            @RequestParam("agentId") @NotBlank String agentId,
            @RequestParam("from") @PositiveOrZero long from,
            @RequestParam("to") @PositiveOrZero long to) {
        final Range range = Range.between(from, to);
        TimeWindow timeWindow = new TimeWindow(range, APDEX_SCORE_TIME_WINDOW_SAMPLER);

        Application application = applicationFactory.createApplicationByTypeName(applicationId, serviceTypeName);

        return apdexScoreService.selectAgentChart(application, range, timeWindow, agentId);
    }
}
