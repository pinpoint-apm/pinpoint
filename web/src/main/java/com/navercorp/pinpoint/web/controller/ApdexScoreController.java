package com.navercorp.pinpoint.web.controller;

import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.web.applicationmap.histogram.ApdexScore;
import com.navercorp.pinpoint.web.service.ApdexScoreService;
import com.navercorp.pinpoint.web.service.ApplicationFactory;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.util.TimeWindowSampler;
import com.navercorp.pinpoint.web.util.TimeWindowSlotCentricSampler;
import com.navercorp.pinpoint.web.view.InspectorView;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.stat.chart.InspectorData;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChart;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
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
            @RequestParam("applicationName") String applicationName,
            @RequestParam("serviceTypeCode") Short serviceTypeCode,
            @RequestParam("from") long from,
            @RequestParam("to") long to) {
        final Range range = Range.between(from, to);

        Application application = applicationFactory.createApplication(applicationName, serviceTypeCode);

        return apdexScoreService.selectApdexScoreData(application, range);
    }

    @GetMapping(value = "/getApdexScore", params = "serviceTypeName")
    public ApdexScore getApdexScore(
            @RequestParam("applicationName") String applicationName,
            @RequestParam("serviceTypeName") String serviceTypeName,
            @RequestParam("from") long from,
            @RequestParam("to") long to) {
        final Range range = Range.between(from, to);

        Application application = applicationFactory.createApplicationByTypeName(applicationName, serviceTypeName);

        return apdexScoreService.selectApdexScoreData(application, range);
    }

    @GetMapping(value = "/getApplicationStat/apdexScore/chart")
    public StatChart getApplicationApdexScoreChart(
            @RequestParam("applicationId") String applicationId,
            @RequestParam("serviceTypeCode") Short serviceTypeCode,
            @RequestParam("from") long from,
            @RequestParam("to") long to) {
        final Range range = Range.between(from, to);
        TimeWindow timeWindow = new TimeWindow(range, APDEX_SCORE_TIME_WINDOW_SAMPLER);

        Application application = applicationFactory.createApplication(applicationId, serviceTypeCode);

        return apdexScoreService.selectApplicationChart(application, range, timeWindow);
    }

    @GetMapping(value = "/getApplicationStat/apdexScore/chart", params = "serviceTypeName")
    public StatChart getApplicationApdexScoreChart(
            @RequestParam("applicationId") String applicationId,
            @RequestParam("serviceTypeName") String serviceTypeName,
            @RequestParam("from") long from,
            @RequestParam("to") long to) {
        final Range range = Range.between(from, to);
        TimeWindow timeWindow = new TimeWindow(range, APDEX_SCORE_TIME_WINDOW_SAMPLER);

        Application application = applicationFactory.createApplicationByTypeName(applicationId, serviceTypeName);

        return apdexScoreService.selectApplicationChart(application, range, timeWindow);
    }

    @GetMapping(value = "/getAgentStat/apdexScore/chart")
    public StatChart getAgentApdexScoreChart(
            @RequestParam("applicationId") String applicationId,
            @RequestParam("serviceTypeCode") Short serviceTypeCode,
            @RequestParam("agentId") String agentId,
            @RequestParam("from") long from,
            @RequestParam("to") long to) {
        final Range range = Range.between(from, to);
        TimeWindow timeWindow = new TimeWindow(range, APDEX_SCORE_TIME_WINDOW_SAMPLER);

        Application application = applicationFactory.createApplication(applicationId, serviceTypeCode);

        return apdexScoreService.selectAgentChart(application, range, timeWindow, agentId);
    }

    @GetMapping(value = "/getAgentStat/apdexScore/chart", params = "serviceTypeName")
    public StatChart getAgentApdexScoreChart(
            @RequestParam("applicationId") String applicationId,
            @RequestParam("serviceTypeName") String serviceTypeName,
            @RequestParam("agentId") String agentId,
            @RequestParam("from") long from,
            @RequestParam("to") long to) {
        final Range range = Range.between(from, to);
        TimeWindow timeWindow = new TimeWindow(range, APDEX_SCORE_TIME_WINDOW_SAMPLER);

        Application application = applicationFactory.createApplicationByTypeName(applicationId, serviceTypeName);

        return apdexScoreService.selectAgentChart(application, range, timeWindow, agentId);
    }

    @GetMapping(value = "/getApplicationStat/apdexScore/inspectorView")
    public InspectorView getApplicationApdexScoreView(
            @RequestParam("applicationName") String applicationName,
            @RequestParam("serviceTypeCode") Short serviceTypeCode,
            @RequestParam("from") long from,
            @RequestParam("to") long to) {
        final Range range = Range.between(from, to);
        TimeWindow timeWindow = new TimeWindow(range, APDEX_SCORE_TIME_WINDOW_SAMPLER);

        Application application = applicationFactory.createApplication(applicationName, serviceTypeCode);

        InspectorData inspectorData = apdexScoreService.selectApplicationInspectorData(application, range, timeWindow);
        return new InspectorView(inspectorData);
    }

    @GetMapping(value = "/getApplicationStat/apdexScore/inspectorView", params = "serviceTypeName")
    public InspectorView getApplicationApdexScoreView(
            @RequestParam("applicationName") String applicationName,
            @RequestParam("serviceTypeName") String serviceTypeName,
            @RequestParam("from") long from,
            @RequestParam("to") long to) {
        final Range range = Range.between(from, to);
        TimeWindow timeWindow = new TimeWindow(range, APDEX_SCORE_TIME_WINDOW_SAMPLER);

        Application application = applicationFactory.createApplicationByTypeName(applicationName, serviceTypeName);

        InspectorData inspectorData = apdexScoreService.selectApplicationInspectorData(application, range, timeWindow);
        return new InspectorView(inspectorData);
    }

    @GetMapping(value = "/getAgentStat/apdexScore/inspectorView")
    public InspectorView getAgentApdexScoreView(
            @RequestParam("applicationName") String applicationName,
            @RequestParam("serviceTypeCode") Short serviceTypeCode,
            @RequestParam("agentId") String agentId,
            @RequestParam("from") long from,
            @RequestParam("to") long to) {
        final Range range = Range.between(from, to);
        TimeWindow timeWindow = new TimeWindow(range, APDEX_SCORE_TIME_WINDOW_SAMPLER);

        Application application = applicationFactory.createApplication(applicationName, serviceTypeCode);

        InspectorData inspectorData = apdexScoreService.selectAgentInspectorData(application, range, timeWindow, agentId);
        return new InspectorView(inspectorData);
    }

    @GetMapping(value = "/getAgentStat/apdexScore/inspectorView", params = "serviceTypeName")
    public InspectorView getAgentApdexScoreView(
            @RequestParam("applicationName") String applicationName,
            @RequestParam("serviceTypeName") String serviceTypeName,
            @RequestParam("agentId") String agentId,
            @RequestParam("from") long from,
            @RequestParam("to") long to) {
        final Range range = Range.between(from, to);
        TimeWindow timeWindow = new TimeWindow(range, APDEX_SCORE_TIME_WINDOW_SAMPLER);

        Application application = applicationFactory.createApplicationByTypeName(applicationName, serviceTypeName);

        InspectorData inspectorData = apdexScoreService.selectAgentInspectorData(application, range, timeWindow, agentId);
        return new InspectorView(inspectorData);
    }
}
