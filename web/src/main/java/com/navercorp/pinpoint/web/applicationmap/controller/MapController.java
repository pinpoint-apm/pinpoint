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
import com.navercorp.pinpoint.web.applicationmap.ApplicationMap;
import com.navercorp.pinpoint.web.applicationmap.ApplicationMapView;
import com.navercorp.pinpoint.web.applicationmap.ApplicationMapViewV3;
import com.navercorp.pinpoint.web.applicationmap.MapWrap;
import com.navercorp.pinpoint.web.applicationmap.SimpleApplicationMap;
import com.navercorp.pinpoint.web.applicationmap.config.MapProperties;
import com.navercorp.pinpoint.web.applicationmap.controller.form.ApplicationForm;
import com.navercorp.pinpoint.web.applicationmap.controller.form.RangeForm;
import com.navercorp.pinpoint.web.applicationmap.controller.form.SearchOptionForm;
import com.navercorp.pinpoint.web.applicationmap.link.LinkList;
import com.navercorp.pinpoint.web.applicationmap.nodes.NodeList;
import com.navercorp.pinpoint.web.applicationmap.service.MapService;
import com.navercorp.pinpoint.web.applicationmap.service.MapServiceOption;
import com.navercorp.pinpoint.web.applicationmap.servicemap.ServiceMapView;
import com.navercorp.pinpoint.web.applicationmap.servicemap.ServiceMapViewBuilder;
import com.navercorp.pinpoint.service.web.resolver.ServiceParam;
import com.navercorp.pinpoint.service.web.vo.ServiceName;
import com.navercorp.pinpoint.web.applicationmap.view.LinkRender;
import com.navercorp.pinpoint.web.applicationmap.view.NodeRender;
import com.navercorp.pinpoint.web.service.CommonService;
import com.navercorp.pinpoint.web.service.ServiceModelResolver;
import com.navercorp.pinpoint.web.util.ApplicationValidator;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.SearchOption;
import com.navercorp.pinpoint.web.vo.Service;
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
import java.util.Set;

/**
 * @author emeroad
 * @author netspider
 * @author jaehong.kim
 * @author HyunGil Jeong
 */
@RestController
@RequestMapping(path = {"/api", "/api/servermap"})
@Validated
public class MapController {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final MapProperties mapProperties;
    private final MapService mapService;
    private final RangeValidator rangeValidator;
    private final ApplicationValidator applicationValidator;
    private final ServiceModelResolver serviceModelResolver;
    private final CommonService commonService;

    private static final int DEFAULT_MAX_SEARCH_DEPTH = 4;

    public MapController(
            MapProperties mapProperties,
            MapService mapService,
            ApplicationValidator applicationValidator,
            ServiceModelResolver serviceModelResolver,
            CommonService commonService,
            Duration limitDay) {
        this.mapProperties = Objects.requireNonNull(mapProperties, "mapProperties");
        this.mapService = Objects.requireNonNull(mapService, "mapService");
        this.applicationValidator = Objects.requireNonNull(applicationValidator, "applicationValidator");
        this.serviceModelResolver = Objects.requireNonNull(serviceModelResolver, "serviceModelResolver");
        this.commonService = Objects.requireNonNull(commonService, "commonService");
        this.rangeValidator = new ForwardRangeValidator(Objects.requireNonNull(limitDay, "limitDay"));
    }

    private SearchOption.Builder searchOptionBuilder() {
        return SearchOption.newBuilder(DEFAULT_MAX_SEARCH_DEPTH);
    }

    /**
     * Server map data query within from ~ to timeframe
     *
     * @param appForm   applicationForm
     * @param rangeForm rangeForm
     * @return MapWrap
     */
    @GetMapping(value = "/serverMap")
    public MapWrap<ApplicationMapView> getServerMapData(
            @Valid @ModelAttribute
            ApplicationForm appForm,
            @Valid @ModelAttribute
            RangeForm rangeForm,
            @Valid @ModelAttribute
            SearchOptionForm searchForm,
            @RequestParam(value = "useStatisticsAgentState", defaultValue = "false", required = false)
            boolean useStatisticsAgentState
    ) {
        final TimeWindow timeWindow = newTimeWindow(rangeForm);

        final SearchOption searchOption = searchOptionBuilder()
                .build(searchForm.getCallerRange(), searchForm.getCalleeRange(), searchForm.isBidirectional(), searchForm.isWasOnly());

        final Application application = getApplication(appForm);

        final MapServiceOption option = new MapServiceOption
                .Builder(application, timeWindow, searchOption)
                .setUseStatisticsAgentState(useStatisticsAgentState)
                .build();

        logger.info("Select applicationMap option={}", option);
        final ApplicationMap map = this.mapService.selectApplicationMap(option);

        NodeRender nodeRender = NodeRender.forServerMap();
        LinkRender linkRender = LinkRender.forServerMap();
        ApplicationMapView applicationMapView = new ApplicationMapViewV3(map, timeWindow, nodeRender, linkRender);

        return new MapWrap<>(applicationMapView);
    }

    @GetMapping(value = "/serviceMap")
    public MapWrap<ServiceMapView> getServiceMapData(
            @ServiceParam ServiceName serviceName,
            @ModelAttribute
            ApplicationForm appForm,
            @Valid @ModelAttribute
            RangeForm rangeForm,
            @RequestParam(value = "useStatisticsAgentState", defaultValue = "false", required = false)
            boolean useStatisticsAgentState
    ) {
        if (!mapProperties.isEnableServiceMap()) {
            logger.warn("Service map is not enabled");
            throw new UnsupportedOperationException("Service map is not enabled");
        }
        final TimeWindow timeWindow = newTimeWindow(rangeForm);
        final SearchOption searchOption = searchOptionBuilder().build(SearchOptionForm.DEFAULT_SEARCH_DEPTH, SearchOptionForm.DEFAULT_SEARCH_DEPTH, false, false);
        final Service service = serviceModelResolver.getService(serviceName.getName());
        final List<Application> sourceApplications = getSourceApplications(service, appForm);
        final ApplicationMap map = selectServiceMap(service, sourceApplications, timeWindow, searchOption, useStatisticsAgentState);

        NodeRender nodeRender = NodeRender.forServiceMap();
        LinkRender linkRender = LinkRender.forServiceMap();
        final Set<String> expandedServiceNames = Set.of(serviceName.getName());
        ServiceMapViewBuilder builder = new ServiceMapViewBuilder(map, timeWindow, nodeRender, linkRender, expandedServiceNames);
        return new MapWrap<>(builder.build());
    }

    private ApplicationMap selectServiceMap(Service service, List<Application> sourceApplications, TimeWindow timeWindow, SearchOption searchOption, boolean useStatisticsAgentState) {
        if (sourceApplications.isEmpty()) {
            logger.info("Select serviceMap. no applications found. service={}", service);
            return new SimpleApplicationMap(NodeList.EMPTY, LinkList.EMPTY, timeWindow.getWindowRange());
        }

        final MapServiceOption option = new MapServiceOption.Builder(sourceApplications, timeWindow, searchOption).setUseStatisticsAgentState(useStatisticsAgentState).build();
        logger.info("Select serviceMap. option={}", option);
        return this.mapService.selectApplicationMap(option);
    }

    private List<Application> getSourceApplications(Service service, ApplicationForm appForm) {
        if (Service.DEFAULT.equals(service)) {
            Application application = applicationValidator.newApplication(service, appForm.getApplicationName(), appForm.getServiceTypeCode(), appForm.getServiceTypeName());
            return List.of(application);
        }
        return commonService.selectAllApplicationNames(service);
    }

    private Application getApplication(ApplicationForm appForm) {
        return applicationValidator.newApplication(appForm.getApplicationName(), appForm.getServiceTypeCode(), appForm.getServiceTypeName());
    }

    private TimeWindow newTimeWindow(RangeForm rangeForm) {
        Range between = Range.between(rangeForm.getFrom(), rangeForm.getTo());
        this.rangeValidator.validate(between);
        return new TimeWindow(between);
    }
}
