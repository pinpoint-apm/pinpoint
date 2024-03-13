/*
 * Copyright 2023 NAVER Corp.
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
import com.navercorp.pinpoint.common.server.util.time.RangeValidator;
import com.navercorp.pinpoint.web.applicationmap.histogram.Histogram;
import com.navercorp.pinpoint.web.applicationmap.link.LinkHistogramSummary;
import com.navercorp.pinpoint.web.applicationmap.nodes.NodeHistogramSummary;
import com.navercorp.pinpoint.web.applicationmap.service.ResponseTimeHistogramService;
import com.navercorp.pinpoint.web.applicationmap.service.ResponseTimeHistogramServiceOption;
import com.navercorp.pinpoint.web.component.ApplicationFactory;
import com.navercorp.pinpoint.web.view.TimeSeries.TimeSeriesView;
import com.navercorp.pinpoint.web.view.histogram.HistogramView;
import com.navercorp.pinpoint.web.view.histogram.ServerHistogramView;
import com.navercorp.pinpoint.web.view.histogram.TimeHistogramType;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.ApplicationPair;
import com.navercorp.pinpoint.web.vo.ApplicationPairs;
import com.navercorp.pinpoint.web.vo.ResponseTimeStatics;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@RestController
public class ResponseTimeController {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ResponseTimeHistogramService responseTimeHistogramService;
    private final RangeValidator rangeValidator;
    private final ApplicationFactory applicationFactory;

    public ResponseTimeController(ResponseTimeHistogramService responseTimeHistogramService,
                                  RangeValidator rangeValidator, ApplicationFactory applicationFactory) {
        this.responseTimeHistogramService = Objects.requireNonNull(responseTimeHistogramService, "responseTimeHistogramService");
        this.rangeValidator = Objects.requireNonNull(rangeValidator, "dateLimit");
        this.applicationFactory = Objects.requireNonNull(applicationFactory, "applicationFactory");
    }

    @GetMapping(value = "/getWas/serverHistogramData")
    public ServerHistogramView getWasServerHistogramData(
            @RequestParam("applicationName") @NotBlank String applicationName,
            @RequestParam(value = "serviceTypeCode", required = false) Short serviceTypeCode,
            @RequestParam(value = "serviceTypeName", required = false) @NotBlank String serviceTypeName,
            @RequestParam("from") @PositiveOrZero long from,
            @RequestParam("to") @PositiveOrZero long to
    ) {
        final Range range = Range.between(from, to);
        this.rangeValidator.validate(range);
        Application application = createApplication(applicationName, serviceTypeCode, serviceTypeName);

        ResponseTimeHistogramServiceOption option = createWasOptionBuilder(application, range)
                .setUseStatisticsAgentState(false) //set useStatisticsAgentState to false for agent data
                .build();
        NodeHistogramSummary nodeHistogramSummary = responseTimeHistogramService.selectNodeHistogramData(option);
        return nodeHistogramSummary.getAgentHistogramView();
    }

    @GetMapping(value = "/getWas/histogram")
    public Histogram getWasHistogram(
            @RequestParam("applicationName") @NotBlank String applicationName,
            @RequestParam(value = "serviceTypeCode", required = false) Short serviceTypeCode,
            @RequestParam(value = "serviceTypeName", required = false) @NotBlank String serviceTypeName,
            @RequestParam("from") @PositiveOrZero long from,
            @RequestParam("to") @PositiveOrZero long to
    ) {
        final Range range = Range.between(from, to);
        this.rangeValidator.validate(range);
        Application application = createApplication(applicationName, serviceTypeCode, serviceTypeName);

        ResponseTimeHistogramServiceOption option = createWasOptionBuilder(application, range)
                .setUseStatisticsAgentState(true)
                .build();
        NodeHistogramSummary nodeHistogramSummary = responseTimeHistogramService.selectNodeHistogramData(option);
        return nodeHistogramSummary.getHistogram();
    }

    @GetMapping(value = "/getWas/responseStatistics")
    public ResponseTimeStatics getWasResponseTimeStatistics(
            @RequestParam("applicationName") @NotBlank String applicationName,
            @RequestParam(value = "serviceTypeCode", required = false) Short serviceTypeCode,
            @RequestParam(value = "serviceTypeName", required = false) @NotBlank String serviceTypeName,
            @RequestParam("from") @PositiveOrZero long from,
            @RequestParam("to") @PositiveOrZero long to
    ) {
        Histogram histogram = getWasHistogram(applicationName, serviceTypeCode, serviceTypeName, from, to);
        return ResponseTimeStatics.fromHistogram(histogram);
    }

    @GetMapping(value = "/getWas/{type}/chart")
    public TimeSeriesView getWasTimeHistogramChart(
            @RequestParam("applicationName") @NotBlank String applicationName,
            @RequestParam(value = "serviceTypeCode", required = false) Short serviceTypeCode,
            @RequestParam(value = "serviceTypeName", required = false) @NotBlank String serviceTypeName,
            @RequestParam("from") @PositiveOrZero long from,
            @RequestParam("to") @PositiveOrZero long to,
            @PathVariable("type") String type
    ) {
        final Range range = Range.between(from, to);
        this.rangeValidator.validate(range);
        Application application = createApplication(applicationName, serviceTypeCode, serviceTypeName);
        TimeHistogramType timeHistogramType = TimeHistogramType.valueOf(type);

        ResponseTimeHistogramServiceOption option = createWasOptionBuilder(application, range)
                .setUseStatisticsAgentState(true)
                .build();
        NodeHistogramSummary nodeHistogramSummary = responseTimeHistogramService.selectNodeHistogramData(option);
        return nodeHistogramSummary.getNodeTimeHistogram(timeHistogramType);
    }

    @GetMapping(value = "/getWas/histogramData")
    public HistogramView getWasHistogramData(
            @RequestParam("applicationName") @NotBlank String applicationName,
            @RequestParam(value = "serviceTypeCode", required = false) Short serviceTypeCode,
            @RequestParam(value = "serviceTypeName", required = false) @NotBlank String serviceTypeName,
            @RequestParam("from") @PositiveOrZero long from,
            @RequestParam("to") @PositiveOrZero long to
    ) {
        final Range range = Range.between(from, to);
        this.rangeValidator.validate(range);
        Application application = createApplication(applicationName, serviceTypeCode, serviceTypeName);

        ResponseTimeHistogramServiceOption option = createWasOptionBuilder(application, range)
                .setUseStatisticsAgentState(true)
                .build();
        NodeHistogramSummary nodeHistogramSummary = responseTimeHistogramService.selectNodeHistogramData(option);
        return nodeHistogramSummary.getHistogramView();
    }

    private ResponseTimeHistogramServiceOption.Builder createWasOptionBuilder(Application application, Range range) {
        if (!application.serviceType().isWas()) {
            throw new IllegalArgumentException("application is not WAS. application:" + application + ", serviceTypeCode:" + application.serviceType());
        }
        return new ResponseTimeHistogramServiceOption.Builder(application, range, Collections.emptyList(), Collections.emptyList());
    }

    @PostMapping(value = "/getNode/serverHistogramData")
    public ServerHistogramView postNodeServerHistogramData(
            @RequestParam("applicationName") @NotBlank String applicationName,
            @RequestParam(value = "serviceTypeCode", required = false) Short serviceTypeCode,
            @RequestParam(value = "serviceTypeName", required = false) @NotBlank String serviceTypeName,
            @RequestParam("from") @PositiveOrZero long from,
            @RequestParam("to") @PositiveOrZero long to,
            @RequestBody ApplicationPairs applicationPairs
    ) {
        final Range range = Range.between(from, to);
        this.rangeValidator.validate(range);
        Application application = createApplication(applicationName, serviceTypeCode, serviceTypeName);

        ResponseTimeHistogramServiceOption option = createOptionBuilder(application, range, applicationPairs)
                .setUseStatisticsAgentState(false) //set useStatisticsAgentState to false for agent data
                .build();
        NodeHistogramSummary nodeHistogramSummary = responseTimeHistogramService.selectNodeHistogramData(option);
        return nodeHistogramSummary.getAgentHistogramView();
    }

    @PostMapping(value = "/getNode/histogramData")
    public HistogramView postNodeHistogramData(
            @RequestParam("applicationName") @NotBlank String applicationName,
            @RequestParam(value = "serviceTypeCode", required = false) Short serviceTypeCode,
            @RequestParam(value = "serviceTypeName", required = false) @NotBlank String serviceTypeName,
            @RequestParam("from") @PositiveOrZero long from,
            @RequestParam("to") @PositiveOrZero long to,
            @RequestBody ApplicationPairs applicationPairs
    ) {
        final Range range = Range.between(from, to);
        this.rangeValidator.validate(range);
        Application application = createApplication(applicationName, serviceTypeCode, serviceTypeName);

        ResponseTimeHistogramServiceOption option = createOptionBuilder(application, range, applicationPairs)
                .setUseStatisticsAgentState(true)
                .build();
        NodeHistogramSummary nodeHistogramSummary = responseTimeHistogramService.selectNodeHistogramData(option);

        return nodeHistogramSummary.getHistogramView();
    }

    @PostMapping(value = "/getNode/{type}/chart")
    public TimeSeriesView postNodeTimeHistogramChart(
            @RequestParam("applicationName") @NotBlank String applicationName,
            @RequestParam(value = "serviceTypeCode", required = false) Short serviceTypeCode,
            @RequestParam(value = "serviceTypeName", required = false) @NotBlank String serviceTypeName,
            @RequestParam("from") @PositiveOrZero long from,
            @RequestParam("to") @PositiveOrZero long to,
            @PathVariable("type") String type,
            @RequestBody ApplicationPairs applicationPairs
    ) {
        final Range range = Range.between(from, to);
        this.rangeValidator.validate(range);
        Application application = createApplication(applicationName, serviceTypeCode, serviceTypeName);
        TimeHistogramType timeHistogramType = TimeHistogramType.valueOf(type);

        ResponseTimeHistogramServiceOption option = createOptionBuilder(application, range, applicationPairs)
                .setUseStatisticsAgentState(true)
                .build();
        NodeHistogramSummary nodeHistogramSummary = responseTimeHistogramService.selectNodeHistogramData(option);

        return nodeHistogramSummary.getNodeTimeHistogram(timeHistogramType);
    }

    @GetMapping(value = "/getLink/histogramData")
    public HistogramView getLinkHistogramData(
            @RequestParam("fromApplicationName") @NotBlank String fromApplicationName,
            @RequestParam(value = "fromServiceTypeCode", required = false) Short fromServiceTypeCode,
            @RequestParam(value = "fromServiceTypeName", required = false) @NotBlank String fromServiceTypeName,
            @RequestParam("toApplicationName") String toApplicationName,
            @RequestParam(value = "toServiceTypeCode", required = false) Short toServiceTypeCode,
            @RequestParam(value = "toServiceTypeName", required = false) @NotBlank String toServiceTypeName,
            @RequestParam("from") @PositiveOrZero long from,
            @RequestParam("to") @PositiveOrZero long to
    ) {
        final Range range = Range.between(from, to);
        this.rangeValidator.validate(range);

        Application fromApplication = createApplication(fromApplicationName, fromServiceTypeCode, fromServiceTypeName);
        Application toApplication = createApplication(toApplicationName, toServiceTypeCode, toServiceTypeName);

        LinkHistogramSummary linkHistogramSummary =
                responseTimeHistogramService.selectLinkHistogramData(fromApplication, toApplication, range);

        return linkHistogramSummary.getHistogramView();
    }

    @GetMapping(value = "/getLink/{type}/chart")
    public TimeSeriesView getLinkTimeHistogramChart(
            @RequestParam("fromApplicationName") @NotBlank String fromApplicationName,
            @RequestParam(value = "fromServiceTypeCode", required = false) Short fromServiceTypeCode,
            @RequestParam(value = "fromServiceTypeName", required = false) @NotBlank String fromServiceTypeName,
            @RequestParam("toApplicationName") String toApplicationName,
            @RequestParam(value = "toServiceTypeCode", required = false) Short toServiceTypeCode,
            @RequestParam(value = "toServiceTypeName", required = false) @NotBlank String toServiceTypeName,
            @RequestParam("from") @PositiveOrZero long from,
            @RequestParam("to") @PositiveOrZero long to,
            @PathVariable("type") String type
    ) {
        final Range range = Range.between(from, to);
        this.rangeValidator.validate(range);

        Application fromApplication = createApplication(fromApplicationName, fromServiceTypeCode, fromServiceTypeName);
        Application toApplication = createApplication(toApplicationName, toServiceTypeCode, toServiceTypeName);
        TimeHistogramType timeHistogramType = TimeHistogramType.valueOf(type);

        LinkHistogramSummary linkHistogramSummary =
                responseTimeHistogramService.selectLinkHistogramData(fromApplication, toApplication, range);

        return linkHistogramSummary.getTimeHistogram(timeHistogramType);
    }

    private ResponseTimeHistogramServiceOption.Builder createOptionBuilder(Application application, Range range,
                                                                           ApplicationPairs applicationPairs) {
        List<Application> fromApplications = mapApplicationPairsToApplications(applicationPairs.getFromApplications());
        List<Application> toApplications = mapApplicationPairsToApplications(applicationPairs.getToApplications());
        return new ResponseTimeHistogramServiceOption.Builder(application, range, fromApplications, toApplications);
    }

    private List<Application> mapApplicationPairsToApplications(List<ApplicationPair> applicationPairs) {
        if (CollectionUtils.isEmpty(applicationPairs)) {
            return Collections.emptyList();
        }
        List<Application> applications = new ArrayList<>(applicationPairs.size());
        for (ApplicationPair applicationPair : applicationPairs) {
            String applicationName = applicationPair.getApplicationName();
            short serviceTypeCode = applicationPair.getServiceTypeCode();
            Application application = applicationFactory.createApplication(applicationName, serviceTypeCode);
            applications.add(application);
        }
        return applications;
    }

    private Application createApplication(String applicationName, Short serviceTypeCode, String serviceTypeName) {
        if (StringUtils.hasLength(applicationName)) {
            if (serviceTypeCode != null) {
                return applicationFactory.createApplication(applicationName, serviceTypeCode);
            } else if (serviceTypeName != null) {
                return applicationFactory.createApplicationByTypeName(applicationName, serviceTypeName);
            }
        }
        logger.error("can not create application. applicationName: {}, serviceTypeCode: {}, serviceTypeName: {}", applicationName, serviceTypeCode, serviceTypeName);
        throw new IllegalArgumentException("can not create application. applicationName: " + serviceTypeName);
    }
}
