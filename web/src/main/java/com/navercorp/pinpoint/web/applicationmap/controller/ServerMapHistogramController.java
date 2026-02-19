/*
 * Copyright 2025 NAVER Corp.
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
 */
package com.navercorp.pinpoint.web.applicationmap.controller;

import com.navercorp.pinpoint.common.timeseries.time.ForwardRangeValidator;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.timeseries.time.RangeValidator;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.web.applicationmap.controller.form.ApplicationForm;
import com.navercorp.pinpoint.web.applicationmap.controller.form.RangeForm;
import com.navercorp.pinpoint.web.applicationmap.controller.form.SearchOptionForm;
import com.navercorp.pinpoint.web.applicationmap.link.LinkHistogramSummary;
import com.navercorp.pinpoint.web.applicationmap.nodes.NodeHistogramSummary;
import com.navercorp.pinpoint.web.applicationmap.nodes.NodeName;
import com.navercorp.pinpoint.web.applicationmap.nodes.ServerGroupList;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataDuplexMap;
import com.navercorp.pinpoint.web.applicationmap.service.HistogramService;
import com.navercorp.pinpoint.web.applicationmap.service.MapServiceOption;
import com.navercorp.pinpoint.web.applicationmap.service.ResponseTimeHistogramService;
import com.navercorp.pinpoint.web.applicationmap.service.ResponseTimeHistogramServiceOption;
import com.navercorp.pinpoint.web.applicationmap.view.LinkHistogramSummaryView;
import com.navercorp.pinpoint.web.applicationmap.view.NodeHistogramSummaryView;
import com.navercorp.pinpoint.web.applicationmap.view.ServerGroupListView;
import com.navercorp.pinpoint.web.applicationmap.view.TimeHistogramView;
import com.navercorp.pinpoint.web.component.ApplicationFactory;
import com.navercorp.pinpoint.web.hyperlink.HyperLinkFactory;
import com.navercorp.pinpoint.web.util.ApplicationValidator;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.SearchOption;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * @author intr3p1d
 */
@RestController
@RequestMapping(path = {"/api", "/api/histogram"})
@Validated
public class ServerMapHistogramController {

    private final Logger logger = LogManager.getLogger(this.getClass());
    private static final int DEFAULT_MAX_SEARCH_DEPTH = 4;
    private static final String NODE_DELIMITER = NodeName.NODE_DELIMITER;
    private static final Pattern NODE_DELIMITER_PATTERN = Pattern.compile(Pattern.quote(NODE_DELIMITER));
    private static final Pattern NODE_KEY_VALIDATION_PATTERN = Pattern.compile(
            "^[^" + Pattern.quote(NODE_DELIMITER) + "]+"
                    + Pattern.quote(NODE_DELIMITER) + "[^" + Pattern.quote(NODE_DELIMITER) + "]+$"
    );
    private static final Pattern LINK_DELIMITER_PATTERN = Pattern.compile(Pattern.quote("~"));
    private static final Pattern LINK_KEY_VALIDATION_PATTERN = Pattern.compile(
            "^[^~]+~[^~]+$"
    );


    private final ResponseTimeHistogramService responseTimeHistogramService;
    private final HistogramService histogramService;
    private final ApplicationFactory applicationFactory;
    private final ServiceTypeRegistryService registry;
    private final RangeValidator rangeValidator;
    private final ApplicationValidator applicationValidator;
    private final HyperLinkFactory hyperLinkFactory;

    public ServerMapHistogramController(
            ResponseTimeHistogramService responseTimeHistogramService,
            HistogramService histogramService,
            ApplicationFactory applicationFactory,
            ServiceTypeRegistryService registry,
            ApplicationValidator applicationValidator,
            HyperLinkFactory hyperLinkFactory,
            Duration limitDay
    ) {
        this.responseTimeHistogramService =
                Objects.requireNonNull(responseTimeHistogramService, "responseTimeHistogramService");
        this.histogramService = Objects.requireNonNull(histogramService, "histogramService");
        this.applicationFactory = Objects.requireNonNull(applicationFactory, "applicationFactory");
        this.registry = Objects.requireNonNull(registry, "registry");
        this.applicationValidator = Objects.requireNonNull(applicationValidator, "applicationValidator");
        this.rangeValidator = new ForwardRangeValidator(Objects.requireNonNull(limitDay, "limitDay"));
        this.hyperLinkFactory = Objects.requireNonNull(hyperLinkFactory, "hyperLinkFactory");
    }

    @GetMapping(value = "/statistics", params = {
            "bidirectional", "callerRange", "calleeRange", "wasOnly"
    })
    public NodeHistogramSummaryView getStatisticsFromServerMap(
            @Valid @ModelAttribute
            ApplicationForm appForm,
            @Valid @ModelAttribute
            RangeForm rangeForm,
            @Valid @ModelAttribute
            SearchOptionForm searchForm
    ) {
        final Range range = toRange(rangeForm);
        this.rangeValidator.validate(range);
        TimeWindow timeWindow = new TimeWindow(range);
        final Application application = getApplication(appForm);

        final LinkDataDuplexMap map = newLinkDataDuplexMap(
                application, timeWindow, searchForm.getCallerRange(), searchForm.getCalleeRange(),
                searchForm.isBidirectional(), searchForm.isWasOnly()
        );

        final List<Application> fromApplications = this.histogramService.getFromApplications(map);
        final List<Application> toApplications = this.histogramService.getToApplications(map);

        return newNodeHistogramSummaryView(
                application, timeWindow, fromApplications, toApplications,
                TimeHistogramView.TimeseriesHistogram
        );
    }


    @GetMapping(value = "/statistics", params = {
            "bidirectional", "callerRange", "calleeRange", "wasOnly", "nodeKey"
    })
    public NodeHistogramSummaryView getStatisticsFromServerMap(
            @Valid @ModelAttribute
            ApplicationForm appForm,
            @Valid @ModelAttribute
            RangeForm rangeForm,
            @Valid @ModelAttribute
            SearchOptionForm searchForm,
            @RequestParam(value = "nodeKey") String nodeKey
    ) {

        final Application application = getApplication(appForm);
        final Application nodeApplication = this.newApplication(nodeKey);
        if (application.equals(nodeApplication)) {
            return getStatisticsFromServerMap(
                    appForm, rangeForm, searchForm
            );
        }

        final Range range = toRange(rangeForm);
        this.rangeValidator.validate(range);
        TimeWindow timeWindow = new TimeWindow(range);

        final LinkDataDuplexMap map = newLinkDataDuplexMap(
                application, timeWindow, searchForm.getCallerRange(), searchForm.getCalleeRange(),
                searchForm.isBidirectional(), searchForm.isWasOnly()
        );

        // To or From node is the original application
        List<Application> fromApplications;
        List<Application> toApplications;
        if (this.histogramService.isToNode(map, nodeApplication)) {
            fromApplications = Collections.emptyList();
            toApplications = List.of(application);
        } else {
            fromApplications = List.of(application);
            toApplications = Collections.emptyList();
        }

        return newNodeHistogramSummaryView(
                nodeApplication, timeWindow, fromApplications, toApplications,
                TimeHistogramView.TimeseriesHistogram
        );
    }

    private LinkDataDuplexMap newLinkDataDuplexMap(
            Application application,
            TimeWindow timeWindow,
            int callerRange,
            int calleeRange,
            boolean bidirectional,
            boolean wasOnly
    ) {
        final SearchOption searchOption = searchOptionBuilder()
                .build(callerRange, calleeRange, bidirectional, wasOnly);

        final MapServiceOption option = new MapServiceOption
                .Builder(application, timeWindow, searchOption)
                .build();

        logger.info("Select ApplicationMap option={}", option);
        return this.histogramService.selectLinkDataDuplexMap(option);
    }

    private NodeHistogramSummaryView newNodeHistogramSummaryView(
            Application application,
            TimeWindow timeWindow,
            List<Application> fromApplications,
            List<Application> toApplications,
            TimeHistogramView timeHistogramView
    ) {
        final ResponseTimeHistogramServiceOption histogramServiceOption = new ResponseTimeHistogramServiceOption
                .Builder(application, timeWindow, fromApplications, toApplications)
                .build();

        final NodeHistogramSummary nodeHistogramSummary = responseTimeHistogramService.selectNodeHistogramData(histogramServiceOption);

        ServerGroupList serverGroupList = nodeHistogramSummary.getServerGroupList();
        ServerGroupListView serverGroupListView = new ServerGroupListView(serverGroupList, hyperLinkFactory);
        return new NodeHistogramSummaryView(nodeHistogramSummary, timeWindow, serverGroupListView, timeHistogramView);
    }


    private Range toRange(RangeForm rangeForm) {
        Range between = Range.between(rangeForm.getFrom(), rangeForm.getTo());
        this.rangeValidator.validate(between);
        return between;
    }

    private SearchOption.Builder searchOptionBuilder() {
        return SearchOption.newBuilder(DEFAULT_MAX_SEARCH_DEPTH);
    }

    private Application getApplication(ApplicationForm appForm) {
        return applicationValidator.newApplication(appForm.getApplicationName(), appForm.getServiceTypeCode(), appForm.getServiceTypeName());
    }

    private Application newApplication(String nodeKey) {
        if (nodeKey == null || nodeKey.isEmpty()) {
            throw new IllegalArgumentException("Node key must not be null or empty");
        }
        if (!NODE_KEY_VALIDATION_PATTERN.matcher(nodeKey).matches()) {
            throw new IllegalArgumentException("Invalid node key format: " + nodeKey);
        }
        String[] parts = NODE_DELIMITER_PATTERN.split(nodeKey, 2);
        String applicationName = parts[0];
        String serviceTypeName = parts[1];

        ServiceType serviceType = null;
        if (StringUtils.hasLength(serviceTypeName)) {
            serviceType = registry.findServiceTypeByName(serviceTypeName);
        }
        if (serviceType != null && serviceType.getCode() != ServiceType.UNDEFINED.getCode()) {
            return new Application(applicationName, serviceType);
        }
        throw new IllegalArgumentException("Invalid or undefined service type for application: " + nodeKey);
    }

    @GetMapping(value = "/statistics", params = {
            "fromApplicationNames", "fromServiceTypeCodes", "toApplicationNames", "toServiceTypeCodes"
    })
    public NodeHistogramSummaryView getNodeHistogramData(
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
            List<Short> toServiceTypeCodes
    ) {
        final Range range = toRange(rangeForm);
        this.rangeValidator.validate(range);
        TimeWindow timeWindow = new TimeWindow(range);

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
                .Builder(application, timeWindow, fromApplications, toApplications)
                .build();

        final NodeHistogramSummary nodeHistogramSummary = responseTimeHistogramService.selectNodeHistogramData(option);

        ServerGroupList serverGroupList = nodeHistogramSummary.getServerGroupList();
        ServerGroupListView serverGroupListView = new ServerGroupListView(serverGroupList, hyperLinkFactory);
        return new NodeHistogramSummaryView(nodeHistogramSummary, timeWindow, serverGroupListView, TimeHistogramView.TimeseriesHistogram);
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

    @GetMapping(value = "/statistics/links")
    public LinkHistogramSummaryView getLinkTimeHistogramData(
            @Valid @ModelAttribute
            ApplicationForm appForm,
            @Valid @ModelAttribute
            RangeForm rangeForm,
            @Valid @ModelAttribute
            SearchOptionForm searchForm,
            @RequestParam("linkKey") @NotBlank String linkKey
    ) {
        final Range range = toRange(rangeForm);
        this.rangeValidator.validate(range);
        TimeWindow timeWindow = new TimeWindow(range);

        if (!LINK_KEY_VALIDATION_PATTERN.matcher(linkKey).matches()) {
            throw new IllegalArgumentException("Invalid linkKey format: expected 'fromApp~toApp' but got: " + linkKey);
        }
        String[] parts = LINK_DELIMITER_PATTERN.split(linkKey, 2);
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid linkKey format: expected 'fromApp~toApp' but got: " + linkKey);
        }
        final Application fromApplication = this.newApplication(parts[0]);
        final Application toApplication = this.newApplication(parts[1]);

        final LinkHistogramSummary linkHistogramSummary =
                histogramService.selectLinkHistogramData(fromApplication, toApplication, timeWindow);

        return new LinkHistogramSummaryView(linkHistogramSummary, timeWindow, TimeHistogramView.TimeseriesHistogram);
    }
}
