/*
 * Copyright 2023 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.navercorp.pinpoint.web.applicationmap.controller;

import com.navercorp.pinpoint.common.timeseries.time.ForwardRangeValidator;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.timeseries.time.RangeValidator;
import com.navercorp.pinpoint.web.applicationmap.controller.form.ApplicationForm;
import com.navercorp.pinpoint.web.applicationmap.controller.form.RangeForm;
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogramFormat;
import com.navercorp.pinpoint.web.applicationmap.link.LinkHistogramSummary;
import com.navercorp.pinpoint.web.applicationmap.nodes.NodeHistogramSummary;
import com.navercorp.pinpoint.web.applicationmap.nodes.ServerGroupList;
import com.navercorp.pinpoint.web.applicationmap.rawdata.AgentHistogramList;
import com.navercorp.pinpoint.web.applicationmap.service.ResponseTimeHistogramService;
import com.navercorp.pinpoint.web.applicationmap.service.ResponseTimeHistogramServiceOption;
import com.navercorp.pinpoint.web.applicationmap.view.LinkHistogramSummaryView;
import com.navercorp.pinpoint.web.applicationmap.view.NodeHistogramSummaryView;
import com.navercorp.pinpoint.web.applicationmap.view.ServerGroupListView;
import com.navercorp.pinpoint.web.component.ApplicationFactory;
import com.navercorp.pinpoint.web.hyperlink.HyperLinkFactory;
import com.navercorp.pinpoint.web.util.ApplicationValidator;
import com.navercorp.pinpoint.web.validation.NullOrNotBlank;
import com.navercorp.pinpoint.web.view.ApplicationTimeHistogramViewModel;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.ApplicationPair;
import com.navercorp.pinpoint.web.vo.ApplicationPairs;
import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Nullable;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author emeroad
 * @author netspider
 * @author jaehong.kim
 * @author HyunGil Jeong
 */
@RestController
@RequestMapping(path = {"/api", "/api/servermap"})
@Validated
public class MapHistogramController {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ResponseTimeHistogramService responseTimeHistogramService;
    private final RangeValidator rangeValidator;
    private final ApplicationFactory applicationFactory;
    private final ApplicationValidator applicationValidator;
    private final HyperLinkFactory hyperLinkFactory;

    public MapHistogramController(
            ResponseTimeHistogramService responseTimeHistogramService,
            ApplicationFactory applicationFactory,
            ApplicationValidator applicationValidator,
            HyperLinkFactory hyperLinkFactory,
            Duration limitDay
    ) {
        this.responseTimeHistogramService =
                Objects.requireNonNull(responseTimeHistogramService, "responseTimeHistogramService");
        this.applicationFactory = Objects.requireNonNull(applicationFactory, "applicationFactory");
        this.applicationValidator = Objects.requireNonNull(applicationValidator, "applicationFactory");
        this.rangeValidator = new ForwardRangeValidator(Objects.requireNonNull(limitDay, "limitDay"));
        this.hyperLinkFactory = Objects.requireNonNull(hyperLinkFactory, "hyperLinkFactory");
    }

    @GetMapping(value = "/getResponseTimeHistogramData")
    public ApplicationTimeHistogramViewModel getResponseTimeHistogramData(
            @Valid @ModelAttribute
            ApplicationForm appForm,
            @Valid @ModelAttribute
            RangeForm rangeForm) {
        final Range range = toRange(rangeForm);

        final Application application = getApplication(appForm);

        AgentHistogramList responseTimes = responseTimeHistogramService.selectResponseTimeHistogramData(application, range);
        return new ApplicationTimeHistogramViewModel(TimeHistogramFormat.V1, application, responseTimes);
    }

    private Range toRange(RangeForm rangeForm) {
        Range between = Range.between(rangeForm.getFrom(), rangeForm.getTo());
        this.rangeValidator.validate(between);
        return between;
    }

    @PostMapping(value = "/getResponseTimeHistogramDataV2")
    public NodeHistogramSummaryView postResponseTimeHistogramDataV2(
            @Valid @ModelAttribute
            ApplicationForm appForm,
            @Valid @ModelAttribute
            RangeForm rangeForm,
            @RequestBody ApplicationPairs applicationPairs,
            @RequestParam(value = "useStatisticsAgentState", defaultValue = "true", required = false)
            boolean useStatisticsAgentState,
            @RequestParam(value = "useLoadHistogramFormat", defaultValue = "false", required = false)
            boolean useLoadHistogramFormat
    ) {
        final Range range = toRange(rangeForm);

        final Application application = getApplication(appForm);

        final List<Application> fromApplications =
                mapApplicationPairsToApplications(applicationPairs.getFromApplications());
        final List<Application> toApplications =
                mapApplicationPairsToApplications(applicationPairs.getToApplications());
        final ResponseTimeHistogramServiceOption option = new ResponseTimeHistogramServiceOption
                .Builder(application, range, fromApplications, toApplications)
                .setUseStatisticsAgentState(useStatisticsAgentState)
                .build();
        final NodeHistogramSummary nodeHistogramSummary = responseTimeHistogramService.selectNodeHistogramData(option);

        TimeHistogramFormat format = TimeHistogramFormat.format(useLoadHistogramFormat);
        ServerGroupList serverGroupList = nodeHistogramSummary.getServerGroupList();
        ServerGroupListView serverGroupListView = new ServerGroupListView(serverGroupList, hyperLinkFactory);
        return new NodeHistogramSummaryView(nodeHistogramSummary, serverGroupListView, format);
    }


    @GetMapping(value = "/getResponseTimeHistogramDataV2")
    public NodeHistogramSummaryView getResponseTimeHistogramDataV2(
            @Valid @ModelAttribute
            ApplicationForm appForm,
            @Valid @ModelAttribute
            RangeForm rangeForm,
            @RequestParam(value = "fromApplicationNames", defaultValue = "", required = false)
            List<String> fromApplicationNames,
            @RequestParam(value = "fromServiceTypeCodes", defaultValue = "", required = false)
            List<Short> fromServiceTypeCodes,
            @RequestParam(value = "toApplicationNames", defaultValue = "", required = false)
            List<String> toApplicationNames,
            @RequestParam(value = "toServiceTypeCodes", defaultValue = "", required = false)
            List<Short> toServiceTypeCodes,
            @RequestParam(value = "useStatisticsAgentState", defaultValue = "true", required = false)
            boolean useStatisticsAgentState,
            @RequestParam(value = "useLoadHistogramFormat", defaultValue = "false", required = false)
            boolean useLoadHistogramFormat
    ) {
        final Range range = toRange(rangeForm);

        if (fromApplicationNames.size() != fromServiceTypeCodes.size()) {
            throw new IllegalArgumentException(
                    "fromApplicationNames and fromServiceTypeCodes must have the same number of elements");
        }
        if (toApplicationNames.size() != toServiceTypeCodes.size()) {
            throw new IllegalArgumentException(
                    "toApplicationNames and toServiceTypeCodes must have the same number of elements");
        }

        final Application application = getApplication(appForm);

        final List<Application> fromApplications = toApplications(fromApplicationNames, fromServiceTypeCodes);
        final List<Application> toApplications = toApplications(toApplicationNames, toServiceTypeCodes);
        final ResponseTimeHistogramServiceOption option = new ResponseTimeHistogramServiceOption
                .Builder(application, range, fromApplications, toApplications)
                .setUseStatisticsAgentState(useStatisticsAgentState)
                .build();

        final NodeHistogramSummary nodeHistogramSummary = responseTimeHistogramService.selectNodeHistogramData(option);

        final TimeHistogramFormat format = TimeHistogramFormat.format(useLoadHistogramFormat);
        ServerGroupList serverGroupList = nodeHistogramSummary.getServerGroupList();
        ServerGroupListView serverGroupListView = new ServerGroupListView(serverGroupList, hyperLinkFactory);
        return new NodeHistogramSummaryView(nodeHistogramSummary, serverGroupListView, format);
    }

    private List<Application> toApplications(List<String> applicationNames, List<Short> serviceTypeCodes) {
        final List<Application> result = new ArrayList<>(applicationNames.size());
        for (int i = 0; i < applicationNames.size(); i++) {
            final Application application =
                    this.applicationFactory.createApplication(applicationNames.get(i), serviceTypeCodes.get(i));
            result.add(application);
        }
        return result;
    }

    private List<Application> mapApplicationPairsToApplications(List<ApplicationPair> applicationPairs) {
        if (CollectionUtils.isEmpty(applicationPairs)) {
            return Collections.emptyList();
        }
        final List<Application> applications = new ArrayList<>(applicationPairs.size());
        for (ApplicationPair applicationPair : applicationPairs) {
            final String applicationName = applicationPair.getApplicationName();
            final short serviceTypeCode = applicationPair.getServiceTypeCode();
            final Application application = this.applicationFactory.createApplication(applicationName, serviceTypeCode);
            applications.add(application);
        }
        return applications;
    }

    @GetMapping(value = "/getLinkTimeHistogramData")
    public LinkHistogramSummaryView getLinkTimeHistogramData(
            @RequestParam(value = "fromApplicationName", required = false) @NullOrNotBlank String fromApplicationName,
            @RequestParam(value = "fromServiceTypeCode", required = false) Short fromServiceTypeCode,
            @RequestParam(value = "toApplicationName", required = false) @NullOrNotBlank String toApplicationName,
            @RequestParam(value = "toServiceTypeCode", required = false) Short toServiceTypeCode,
            @Valid @ModelAttribute
            RangeForm rangeForm,
            @RequestParam(value = "useLoadHistogramFormat", defaultValue = "false", required = false)
            boolean useLoadHistogramFormat
    ) {
        final Range range = toRange(rangeForm);
        this.rangeValidator.validate(range);

        final Application fromApplication = this.createApplication(fromApplicationName, fromServiceTypeCode);
        final Application toApplication = this.createApplication(toApplicationName, toServiceTypeCode);
        final LinkHistogramSummary linkHistogramSummary =
                responseTimeHistogramService.selectLinkHistogramData(fromApplication, toApplication, range);

        TimeHistogramFormat format = TimeHistogramFormat.format(useLoadHistogramFormat);
        return new LinkHistogramSummaryView(linkHistogramSummary, format);
    }

    @Nullable
    private Application createApplication(@Nullable String name, Short serviceTypeCode) {
        if (name == null) {
            return null;
        }
        return this.applicationFactory.createApplication(name, serviceTypeCode);
    }

    private Application getApplication(ApplicationForm appForm) {
        return applicationValidator.newApplication(appForm.getApplicationName(), appForm.getServiceTypeCode(), appForm.getServiceTypeName());
    }
}
