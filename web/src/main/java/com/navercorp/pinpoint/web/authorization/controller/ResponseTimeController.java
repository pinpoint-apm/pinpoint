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
import com.navercorp.pinpoint.web.applicationmap.link.LinkHistogramSummary;
import com.navercorp.pinpoint.web.applicationmap.nodes.NodeAgentHistogramList;
import com.navercorp.pinpoint.web.applicationmap.nodes.NodeHistogramSummary;
import com.navercorp.pinpoint.web.service.ApplicationFactory;
import com.navercorp.pinpoint.web.service.ResponseTimeHistogramService;
import com.navercorp.pinpoint.web.service.ResponseTimeHistogramServiceOption;
import com.navercorp.pinpoint.web.util.Limiter;
import com.navercorp.pinpoint.web.view.histogram.TimeHistogramType;
import com.navercorp.pinpoint.web.view.TimeSeries.TimeSeriesView;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.ApplicationPair;
import com.navercorp.pinpoint.web.vo.ApplicationPairs;
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

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PositiveOrZero;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@RestController
public class ResponseTimeController {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ResponseTimeHistogramService responseTimeHistogramService;
    private final Limiter dateLimit;
    private final ApplicationFactory applicationFactory;

    public ResponseTimeController(ResponseTimeHistogramService responseTimeHistogramService,
                                  Limiter dateLimit, ApplicationFactory applicationFactory) {
        this.responseTimeHistogramService = Objects.requireNonNull(responseTimeHistogramService, "responseTimeHistogramService");
        this.dateLimit = Objects.requireNonNull(dateLimit, "dateLimit");
        this.applicationFactory = Objects.requireNonNull(applicationFactory, "applicationFactory");
    }

    @PostMapping(value = "/getResponseTimeHistogramDataV3")
    public NodeAgentHistogramList postResponseTimeHistogramDataV3(
            @RequestParam("applicationName") @NotBlank String applicationName,
            @RequestParam("serviceTypeCode") Short serviceTypeCode,
            @RequestParam("from") @PositiveOrZero long from,
            @RequestParam("to") @PositiveOrZero long to,
            @RequestBody ApplicationPairs applicationPairs,
            @RequestParam(value = "useStatisticsAgentState", defaultValue = "false", required = false)
                    boolean useStatisticsAgentState) {
        final Range range = Range.between(from, to);
        dateLimit.limit(range);
        Application application = applicationFactory.createApplication(applicationName, serviceTypeCode);

        ResponseTimeHistogramServiceOption option = buildOption(application, range, applicationPairs, useStatisticsAgentState);
        NodeHistogramSummary nodeHistogramSummary = responseTimeHistogramService.selectNodeHistogramData(option);

        return new NodeAgentHistogramList(application, nodeHistogramSummary.getNodeHistogram(), nodeHistogramSummary.getServerGroupList());
    }

    @GetMapping(value = "/getWas/{type}/chart")
    public TimeSeriesView getWasTimeHistogramChart(
            @RequestParam("applicationName") @NotBlank String applicationName,
            @RequestParam(value = "serviceTypeCode", required = false) Short serviceTypeCode,
            @RequestParam(value = "serviceTypeName", required = false) @NotBlank String serviceTypeName,
            @RequestParam("from") @PositiveOrZero long from,
            @RequestParam("to") @PositiveOrZero long to,
            @PathVariable("type") String type) {
        final Range range = Range.between(from, to);
        dateLimit.limit(range);
        Application application = createApplication(applicationName, serviceTypeCode, serviceTypeName);
        if (!application.getServiceType().isWas()) {
            throw new IllegalArgumentException("application is not WAS. application:" + application + ", serviceTypeCode:" + serviceTypeCode);
        }
        TimeHistogramType timeHistogramType = TimeHistogramType.valueOf(type);

        ResponseTimeHistogramServiceOption option = buildOption(application, range, ApplicationPairs.empty(), false);
        NodeHistogramSummary nodeHistogramSummary = responseTimeHistogramService.selectNodeHistogramData(option);

        return nodeHistogramSummary.getNodeTimeHistogram(timeHistogramType);
    }

    @PostMapping(value = "/getNode/{type}/chart")
    public TimeSeriesView postNodeTimeHistogramChart(
            @RequestParam("applicationName") @NotBlank String applicationName,
            @RequestParam(value = "serviceTypeCode", required = false) Short serviceTypeCode,
            @RequestParam(value = "serviceTypeName", required = false) @NotBlank String serviceTypeName,
            @RequestParam("from") @PositiveOrZero long from,
            @RequestParam("to") @PositiveOrZero long to,
            @PathVariable("type") String type,
            @RequestBody ApplicationPairs applicationPairs) {
        final Range range = Range.between(from, to);
        dateLimit.limit(range);
        Application application = createApplication(applicationName, serviceTypeCode, serviceTypeName);
        TimeHistogramType timeHistogramType = TimeHistogramType.valueOf(type);

        ResponseTimeHistogramServiceOption option = buildOption(application, range, applicationPairs, true);
        NodeHistogramSummary nodeHistogramSummary = responseTimeHistogramService.selectNodeHistogramData(option);

        return nodeHistogramSummary.getNodeTimeHistogram(timeHistogramType);
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
            @PathVariable("type") String type) {
        final Range range = Range.between(from, to);
        dateLimit.limit(range);

        Application fromApplication = createApplication(fromApplicationName, fromServiceTypeCode, fromServiceTypeName);
        Application toApplication = createApplication(toApplicationName, toServiceTypeCode, toServiceTypeName);
        TimeHistogramType timeHistogramType = TimeHistogramType.valueOf(type);

        LinkHistogramSummary linkHistogramSummary =
                responseTimeHistogramService.selectLinkHistogramData(fromApplication, toApplication, range);

        return linkHistogramSummary.getTimeHistogram(timeHistogramType);
    }

    public ResponseTimeHistogramServiceOption buildOption(Application application, Range range,
                                                          ApplicationPairs applicationPairs, boolean useStatisticsAgentState) {
        List<Application> fromApplications = mapApplicationPairsToApplications(applicationPairs.getFromApplications());
        List<Application> toApplications = mapApplicationPairsToApplications(applicationPairs.getToApplications());
        return new ResponseTimeHistogramServiceOption.Builder(application, range, fromApplications, toApplications)
                .setUseStatisticsAgentState(useStatisticsAgentState).build();
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
