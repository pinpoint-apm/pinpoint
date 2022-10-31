/*
 * Copyright 2022 NAVER Corp.
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

package com.navercorp.pinpoint.web;

import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatDataPoint;
import com.navercorp.pinpoint.web.authorization.controller.AdminController;
import com.navercorp.pinpoint.web.authorization.controller.AgentCommandController;
import com.navercorp.pinpoint.web.authorization.controller.AgentDownloadController;
import com.navercorp.pinpoint.web.authorization.controller.AgentInfoController;
import com.navercorp.pinpoint.web.authorization.controller.AgentListController;
import com.navercorp.pinpoint.web.authorization.controller.AgentStatController;
import com.navercorp.pinpoint.web.authorization.controller.AlarmController;
import com.navercorp.pinpoint.web.authorization.controller.ApplicationDataSourceController;
import com.navercorp.pinpoint.web.authorization.controller.ApplicationStatController;
import com.navercorp.pinpoint.web.authorization.controller.HeatMapController;
import com.navercorp.pinpoint.web.authorization.controller.ScatterChartController;
import com.navercorp.pinpoint.web.authorization.controller.UserController;
import com.navercorp.pinpoint.web.authorization.controller.UserGroupController;
import com.navercorp.pinpoint.web.config.ConfigProperties;
import com.navercorp.pinpoint.web.filter.FilterBuilder;
import com.navercorp.pinpoint.web.service.AdminService;
import com.navercorp.pinpoint.web.service.AgentDownLoadService;
import com.navercorp.pinpoint.web.service.AgentEventService;
import com.navercorp.pinpoint.web.service.AgentInfoService;
import com.navercorp.pinpoint.web.service.AgentService;
import com.navercorp.pinpoint.web.service.AlarmService;
import com.navercorp.pinpoint.web.service.FilteredMapService;
import com.navercorp.pinpoint.web.service.HeatMapService;
import com.navercorp.pinpoint.web.service.ScatterChartService;
import com.navercorp.pinpoint.web.service.UserGroupService;
import com.navercorp.pinpoint.web.service.UserService;
import com.navercorp.pinpoint.web.service.WebhookSendInfoService;
import com.navercorp.pinpoint.web.service.appmetric.ApplicationDataSourceService;
import com.navercorp.pinpoint.web.service.appmetric.ApplicationStatChartService;
import com.navercorp.pinpoint.web.service.stat.AgentStatChartService;
import com.navercorp.pinpoint.web.service.stat.AgentStatService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author minwoo.jung
 */
@Configuration
public class AuthorizationConfig {

    @Bean
    public UserController createUserController(UserService userService) {
        return new UserController(userService);
    }

    @Bean
    public AgentInfoController createAgentInfoController(AgentInfoService agentInfoService, AgentEventService agentEventService) {
        return new AgentInfoController(agentInfoService, agentEventService);
    }

    @Bean
    public AgentListController createAgentListController(AgentInfoService agentInfoService) {
        return new AgentListController(agentInfoService);
    }

    @Bean
    public AgentStatController<AgentStatDataPoint> createAgentStatController(List<AgentStatService> agentStatServiceList,
                                                                             List<AgentStatChartService> agentStatChartServiceList) {

        List<AgentStatService<AgentStatDataPoint>> service = (List<AgentStatService<AgentStatDataPoint>>) (List<?>) agentStatServiceList;
        return new AgentStatController<>(service, agentStatChartServiceList);
    }

    @Bean
    public AgentCommandController createAgentCommandController(ConfigProperties webProperties, AgentService agentService) {
        return new AgentCommandController(webProperties, agentService);
    }

    @Bean
    public HeatMapController createHeatMapController(HeatMapService heatMap) {
        return new HeatMapController(heatMap);
    }

    @Bean
    public ScatterChartController createScatterChartController(ScatterChartService scatter, FilteredMapService flow, FilterBuilder<List<SpanBo>> filterBuilder) {
        return new ScatterChartController(scatter, flow, filterBuilder);
    }

    @Bean
    public AdminController createAdminController(AdminService adminService) {
        return new AdminController(adminService);
    }

    @Bean
    public ApplicationDataSourceController createApplicationDataSourceController(ApplicationDataSourceService applicationDataSourceService) {
        return new ApplicationDataSourceController(applicationDataSourceService);
    }

    @Bean
    public ApplicationStatController getApplicationStatControllerV2(List<ApplicationStatChartService> list) {
        return new ApplicationStatController(list);
    }


    @Bean
    public AlarmController createAlarmController(AlarmService alarmService, WebhookSendInfoService webhookSendInfoService) {
        return new AlarmController(alarmService, webhookSendInfoService);
    }

    @Bean
    public UserGroupController createUserGroupController(UserGroupService userGroupService) {
        return new UserGroupController(userGroupService);
    }

    @Bean
    public AgentDownloadController createAgentDownloadController(AgentDownLoadService agentDownLoadService) {
        return new AgentDownloadController(agentDownLoadService);
    }

}
