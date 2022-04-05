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
import com.navercorp.pinpoint.web.authorization.controller.AdminController;
import com.navercorp.pinpoint.web.authorization.controller.AgentCommandController;
import com.navercorp.pinpoint.web.authorization.controller.AgentInfoController;
import com.navercorp.pinpoint.web.authorization.controller.AgentStatController.*;
import com.navercorp.pinpoint.web.authorization.controller.AlarmController;
import com.navercorp.pinpoint.web.authorization.controller.ApplicationStatController.*;

import com.navercorp.pinpoint.web.authorization.controller.HeatMapController;
import com.navercorp.pinpoint.web.authorization.controller.ScatterChartController;
import com.navercorp.pinpoint.web.authorization.controller.UserController;
import com.navercorp.pinpoint.web.authorization.controller.UserGroupController;
import com.navercorp.pinpoint.web.config.ConfigProperties;
import com.navercorp.pinpoint.web.filter.FilterBuilder;
import com.navercorp.pinpoint.web.service.AdminService;
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
import com.navercorp.pinpoint.web.service.stat.ActiveTraceChartService;
import com.navercorp.pinpoint.web.service.stat.ActiveTraceService;
import com.navercorp.pinpoint.web.service.stat.AgentUriStatChartService;
import com.navercorp.pinpoint.web.service.stat.AgentUriStatService;
import com.navercorp.pinpoint.web.service.appmetric.ApplicationActiveTraceService;
import com.navercorp.pinpoint.web.service.appmetric.ApplicationCpuLoadService;
import com.navercorp.pinpoint.web.service.appmetric.ApplicationDataSourceService;
import com.navercorp.pinpoint.web.service.appmetric.ApplicationDirectBufferService;
import com.navercorp.pinpoint.web.service.appmetric.ApplicationFileDescriptorService;
import com.navercorp.pinpoint.web.service.appmetric.ApplicationLoadedClassService;
import com.navercorp.pinpoint.web.service.appmetric.ApplicationMemoryService;
import com.navercorp.pinpoint.web.service.appmetric.ApplicationResponseTimeService;
import com.navercorp.pinpoint.web.service.appmetric.ApplicationTotalThreadCountService;
import com.navercorp.pinpoint.web.service.appmetric.ApplicationTransactionService;
import com.navercorp.pinpoint.web.service.stat.CpuLoadChartService;
import com.navercorp.pinpoint.web.service.stat.CpuLoadService;
import com.navercorp.pinpoint.web.service.stat.DataSourceChartService;
import com.navercorp.pinpoint.web.service.stat.DataSourceService;
import com.navercorp.pinpoint.web.service.stat.DeadlockChartService;
import com.navercorp.pinpoint.web.service.stat.DeadlockService;
import com.navercorp.pinpoint.web.service.stat.DirectBufferChartService;
import com.navercorp.pinpoint.web.service.stat.DirectBufferService;
import com.navercorp.pinpoint.web.service.stat.FileDescriptorChartService;
import com.navercorp.pinpoint.web.service.stat.FileDescriptorService;
import com.navercorp.pinpoint.web.service.stat.JvmGcChartService;
import com.navercorp.pinpoint.web.service.stat.JvmGcDetailedChartService;
import com.navercorp.pinpoint.web.service.stat.JvmGcDetailedService;
import com.navercorp.pinpoint.web.service.stat.JvmGcService;
import com.navercorp.pinpoint.web.service.stat.LoadedClassCountChartService;
import com.navercorp.pinpoint.web.service.stat.LoadedClassCountService;
import com.navercorp.pinpoint.web.service.stat.ResponseTimeChartService;
import com.navercorp.pinpoint.web.service.stat.ResponseTimeService;
import com.navercorp.pinpoint.web.service.stat.TotalThreadCountChartService;
import com.navercorp.pinpoint.web.service.stat.TotalThreadCountService;
import com.navercorp.pinpoint.web.service.stat.TransactionChartService;
import com.navercorp.pinpoint.web.service.stat.TransactionService;
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
    public JvmGcController createJvmGcController(JvmGcService jvmGcService, JvmGcChartService jvmGcChartService) {
        return new JvmGcController(jvmGcService, jvmGcChartService);
    }

    @Bean
    JvmGcDetailedController createJvmGcDetailedController(JvmGcDetailedService jvmGcDetailedService, JvmGcDetailedChartService jvmGcDetailedChartService) {
        return new JvmGcDetailedController(jvmGcDetailedService, jvmGcDetailedChartService);
    }

    @Bean
    CpuLoadController createCpuLoadController(CpuLoadService cpuLoadService, CpuLoadChartService cpuLoadChartService) {
        return new CpuLoadController(cpuLoadService, cpuLoadChartService);
    }

    @Bean
    TransactionController createTransactionController(TransactionService transactionService, TransactionChartService transactionChartService) {
        return new TransactionController(transactionService, transactionChartService);
    }

    @Bean
    ActiveTraceController createActiveTraceController(ActiveTraceService activeTraceService, ActiveTraceChartService activeTraceChartService) {
        return new ActiveTraceController(activeTraceService, activeTraceChartService);
    }

    @Bean
    DataSourceController createDataSourceController(DataSourceService dataSourceService, DataSourceChartService dataSourceChartService) {
        return new DataSourceController(dataSourceService, dataSourceChartService);
    }

    @Bean
    ResponseTimeController createResponseTimeController(ResponseTimeService responseTimeService, ResponseTimeChartService responseTimeChartService) {
        return new ResponseTimeController(responseTimeService, responseTimeChartService);
    }

    @Bean
    DeadlockController createDeadlockController(DeadlockService deadlockService, DeadlockChartService deadlockChartService) {
        return new DeadlockController(deadlockService, deadlockChartService);
    }

    @Bean
    FileDescriptorController createFileDescriptorController(FileDescriptorService fileDescriptorService, FileDescriptorChartService fileDescriptorChartService) {
        return new FileDescriptorController(fileDescriptorService, fileDescriptorChartService);
    }

    @Bean
    DirectBufferController createDirectBufferController(DirectBufferService directBufferService, DirectBufferChartService directBufferChartService) {
        return new DirectBufferController(directBufferService, directBufferChartService);
    }

    @Bean
    TotalThreadCountController createTotalThreadCountController(TotalThreadCountService totalThreadCountService, TotalThreadCountChartService totalThreadCountChartService) {
        return new TotalThreadCountController(totalThreadCountService, totalThreadCountChartService);
    }

    @Bean
    LoadedClassCountController createLoadedClassCountController(LoadedClassCountService loadedClassCountService, LoadedClassCountChartService loadedClassCountChartService) {
        return new LoadedClassCountController(loadedClassCountService, loadedClassCountChartService);
    }

    @Bean
    UriStatController createUriStatController(AgentUriStatService agentUriStatService, AgentUriStatChartService agentUriStatChartService) {
        return new UriStatController(agentUriStatService, agentUriStatChartService);
    }

    @Bean
    AgentCommandController createAgentCommandController(ConfigProperties webProperties, AgentService agentService) {
        return new AgentCommandController(webProperties, agentService);
    }

    @Bean
    HeatMapController createHeatMapController(HeatMapService heatMap) {
        return new HeatMapController(heatMap);
    }

    @Bean
    ScatterChartController createScatterChartController(ScatterChartService scatter, FilteredMapService flow, FilterBuilder<List<SpanBo>> filterBuilder) {
        return new ScatterChartController(scatter, flow, filterBuilder);
    }

    @Bean
    AdminController createAdminController(AdminService adminService) {
        return new AdminController(adminService);
    }

    @Bean
    ApplicationCpuLoadController createApplicationCpuLoadController(ApplicationCpuLoadService applicationCpuLoadService) {
        return new ApplicationCpuLoadController(applicationCpuLoadService);
    }

    @Bean
    ApplicationMemoryController createApplicationMemoryController(ApplicationMemoryService applicationMemoryService) {
        return new ApplicationMemoryController(applicationMemoryService);
    }

    @Bean
    ApplicationTransactionController createApplicationTransactionController(ApplicationTransactionService applicationTransactionService) {
        return new ApplicationTransactionController(applicationTransactionService);
    }

    @Bean
    ApplicationActiveTraceController crateApplicationActiveTraceController(ApplicationActiveTraceService applicationActiveTraceService) {
        return new ApplicationActiveTraceController(applicationActiveTraceService);
    }

    @Bean
    ApplicationResponseTimeController createApplicationResponseTimeController(ApplicationResponseTimeService applicationResponseTimeService) {
        return new ApplicationResponseTimeController(applicationResponseTimeService);
    }

    @Bean
    ApplicationDataSourceController createApplicationDataSourceController(ApplicationDataSourceService applicationDataSourceService) {
        return new ApplicationDataSourceController(applicationDataSourceService);
    }

    @Bean
    ApplicationFileDescriptorController createApplicationFileDescriptorController(ApplicationFileDescriptorService applicationFileDescriptorService) {
        return new ApplicationFileDescriptorController(applicationFileDescriptorService);
    }

    @Bean
    ApplicationDirectBufferController createApplicationDirectBufferController(ApplicationDirectBufferService applicationDirectBufferService) {
        return new ApplicationDirectBufferController(applicationDirectBufferService);
    }

    @Bean
    ApplicationTotalThreadCountController createApplicationTotalThreadCountController(ApplicationTotalThreadCountService applicationTotalThreadCountService) {
        return new ApplicationTotalThreadCountController(applicationTotalThreadCountService);
    }

    @Bean
    ApplicationLoadedClassController createApplicationLoadedClassController(ApplicationLoadedClassService applicationLoadedClassService) {
        return new ApplicationLoadedClassController(applicationLoadedClassService);
    }

    @Bean
    AlarmController createAlarmController(AlarmService alarmService, WebhookSendInfoService webhookSendInfoService) {
        return new AlarmController(alarmService, webhookSendInfoService);
    }

    @Bean
    UserGroupController createUserGroupController(UserGroupService userGroupService) {
        return new UserGroupController(userGroupService);
    }


}
