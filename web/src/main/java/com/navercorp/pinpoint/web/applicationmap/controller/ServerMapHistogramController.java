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
package com.navercorp.pinpoint.web.applicationmap.controller;

import com.navercorp.pinpoint.common.timeseries.time.ForwardRangeValidator;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.timeseries.time.RangeValidator;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import com.navercorp.pinpoint.web.applicationmap.controller.form.ApplicationForm;
import com.navercorp.pinpoint.web.applicationmap.controller.form.RangeForm;
import com.navercorp.pinpoint.web.applicationmap.controller.form.SearchDepthForm;
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogramFormat;
import com.navercorp.pinpoint.web.applicationmap.nodes.NodeHistogramSummary;
import com.navercorp.pinpoint.web.applicationmap.nodes.ServerGroupList;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataDuplexMap;
import com.navercorp.pinpoint.web.applicationmap.service.HistogramService;
import com.navercorp.pinpoint.web.applicationmap.service.MapServiceOption;
import com.navercorp.pinpoint.web.applicationmap.service.ResponseTimeHistogramService;
import com.navercorp.pinpoint.web.applicationmap.service.ResponseTimeHistogramServiceOption;
import com.navercorp.pinpoint.web.applicationmap.view.NodeHistogramSummaryView;
import com.navercorp.pinpoint.web.applicationmap.view.ServerGroupListView;
import com.navercorp.pinpoint.web.component.ApplicationFactory;
import com.navercorp.pinpoint.web.hyperlink.HyperLinkFactory;
import com.navercorp.pinpoint.web.util.ApplicationValidator;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.SearchOption;
import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

/**
 * @author intr3p1d
 */
@RestController
@RequestMapping(path = {"/api", "/api/histogram"})
@Validated
public class ServerMapHistogramController {

    private final Logger logger = LogManager.getLogger(this.getClass());
    private static final int DEFAULT_MAX_SEARCH_DEPTH = 4;

    private final ResponseTimeHistogramService responseTimeHistogramService;
    private final HistogramService histogramService;
    private final RangeValidator rangeValidator;
    private final ApplicationValidator applicationValidator;
    private final HyperLinkFactory hyperLinkFactory;

    public ServerMapHistogramController(
            ResponseTimeHistogramService responseTimeHistogramService,
            HistogramService histogramService,
            ApplicationValidator applicationValidator,
            HyperLinkFactory hyperLinkFactory,
            Duration limitDay
    ) {
        this.responseTimeHistogramService =
                Objects.requireNonNull(responseTimeHistogramService, "responseTimeHistogramService");
        this.histogramService = Objects.requireNonNull(histogramService, "histogramService");
        this.applicationValidator = Objects.requireNonNull(applicationValidator, "applicationValidator");
        this.rangeValidator = new ForwardRangeValidator(Objects.requireNonNull(limitDay, "limitDay"));
        this.hyperLinkFactory = Objects.requireNonNull(hyperLinkFactory, "hyperLinkFactory");
    }

    @GetMapping(value = "/statistics", params = {
            "bidirectional", "callerRange", "calleeRange", "wasOnly"
    })
    public NodeHistogramSummaryView getResponseTimeHistogramDataV2(
            @Valid @ModelAttribute
            ApplicationForm appForm,
            @Valid @ModelAttribute
            RangeForm rangeForm,
            @Valid @ModelAttribute
            SearchDepthForm depthForm,
            @RequestParam(value = "nodeName", required = false) String nodeName,
            @RequestParam(value = "bidirectional", defaultValue = "true", required = false) boolean bidirectional,
            @RequestParam(value = "wasOnly", defaultValue = "false", required = false) boolean wasOnly,
            @RequestParam(value = "useStatisticsAgentState", defaultValue = "false", required = false)
            boolean useStatisticsAgentState,
            @RequestParam(value = "useLoadHistogramFormat", defaultValue = "false", required = false)
            boolean useLoadHistogramFormat
    ) {
        String focusedNodeName = nodeName != null ? nodeName : appForm.getApplicationName();

        final Range range = toRange(rangeForm);
        TimeWindow timeWindow = new TimeWindow(range);

        final SearchOption searchOption = searchOptionBuilder()
                .build(depthForm.getCallerRange(), depthForm.getCalleeRange(), bidirectional, wasOnly);

        final Application application = getApplication(appForm);

        final MapServiceOption option = new MapServiceOption
                .Builder(application, timeWindow, searchOption)
                .setUseStatisticsAgentState(useStatisticsAgentState)
                .build();

        final TimeHistogramFormat format = TimeHistogramFormat.format(useLoadHistogramFormat);
        logger.info("Select ApplicationMap {} option={}", format, option);
        final LinkDataDuplexMap map = this.histogramService.selectLinkDataDuplexMap(option);

        final List<Application> fromApplications = this.histogramService.getFromApplications(map);
        final List<Application> toApplications = this.histogramService.getToApplications(map);
        final Application nodeApplication = this.histogramService.findApplicationByName(fromApplications, toApplications, focusedNodeName);

        if (nodeApplication == null) {
            logger.error("No matching application found for node name: {}", focusedNodeName);
            throw new IllegalArgumentException("No matching application found for node name: " + focusedNodeName);
        }

        final ResponseTimeHistogramServiceOption histogramServiceOption = new ResponseTimeHistogramServiceOption
                .Builder(nodeApplication, timeWindow, fromApplications, toApplications)
                .setUseStatisticsAgentState(useStatisticsAgentState)
                .build();

        final NodeHistogramSummary nodeHistogramSummary = responseTimeHistogramService.selectNodeHistogramData(histogramServiceOption);

        ServerGroupList serverGroupList = nodeHistogramSummary.getServerGroupList();
        ServerGroupListView serverGroupListView = new ServerGroupListView(serverGroupList, hyperLinkFactory);
        return new NodeHistogramSummaryView(nodeHistogramSummary, serverGroupListView, format);
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


}
