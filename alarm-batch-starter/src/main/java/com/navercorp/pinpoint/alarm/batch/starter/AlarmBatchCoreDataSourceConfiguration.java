/*
 * Copyright 2026 NAVER Corp.
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
package com.navercorp.pinpoint.alarm.batch.starter;

import com.navercorp.pinpoint.alarm.core.agentstat.dao.AgentStatAlarmDao;
import com.navercorp.pinpoint.alarm.core.service.AgentEventMetricQueryService;
import com.navercorp.pinpoint.alarm.core.service.AgentIdsResolver;
import com.navercorp.pinpoint.alarm.core.service.AgentStatAgentIdsResolver;
import com.navercorp.pinpoint.alarm.core.service.IndexedAgentIdsResolver;
import com.navercorp.pinpoint.alarm.core.service.AgentStatMetricQueryService;
import com.navercorp.pinpoint.alarm.core.service.ApplicationOutCallMetricQueryService;
import com.navercorp.pinpoint.alarm.core.service.ApplicationResponseMetricQueryService;
import com.navercorp.pinpoint.alarm.core.service.CoreAlarmDataSourceProvider;
import com.navercorp.pinpoint.alarm.core.service.RuleApplicationResolver;
import com.navercorp.pinpoint.applicationmap.dao.MapOutLinkDao;
import com.navercorp.pinpoint.applicationmap.dao.MapResponseDao;
import com.navercorp.pinpoint.common.server.bo.ApplicationFactory;
import com.navercorp.pinpoint.common.server.dao.AgentEventDao;
import com.navercorp.pinpoint.common.server.dao.AgentIdDao;
import com.navercorp.pinpoint.service.service.ServiceModelResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * What a rule may measure in this deployable.
 *
 * <p>This configuration is the whole of what the process can alarm on, and -- because the
 * sweep asks the database only for the data sources it can evaluate -- the whole of what it
 * even reads; {@link com.navercorp.pinpoint.alarm.batch.config.AlarmJobConfiguration} has
 * the reasoning. Without it the process installs no data source at all and is rejected at
 * startup rather than sweeping for nothing.
 *
 * <p>Adding a data source is a bean here and a module on the classpath -- nothing in the job
 * or the evaluation changes.
 *
 * <p>The stores are taken as parameters rather than imported, so this half can be stood up
 * against stubs: what is worth checking without a cluster is that every data source has a
 * service and that no two claim the same one.
 */
@Configuration(proxyBeanMethods = false)
public class AlarmBatchCoreDataSourceConfiguration {

    @Bean
    public CoreAlarmDataSourceProvider coreAlarmDataSourceProvider() {
        return new CoreAlarmDataSourceProvider();
    }

    /** The sweep's delay doubles as the memo window; see the resolver for why not the sweep. */
    @Bean
    public RuleApplicationResolver ruleApplicationResolver(
            ApplicationFactory applicationFactory,
            ServiceModelResolver serviceModelResolver,
            @Value("${pinpoint.modules.batch.alarm.fixedDelayMs}") long sweepDelayMillis) {
        return new RegistryRuleApplicationResolver(applicationFactory, serviceModelResolver,
                Duration.ofMillis(sweepDelayMillis));
    }

    /**
     * Two lists: a usage metric wants the agents that reported something to compare, an event
     * metric wants the agents that exist, including the ones that went quiet. Declared and
     * injected by concrete type because picking the wrong one compiles, boots, and then
     * silently stops finding deadlocks.
     */
    @Bean
    public AgentStatAgentIdsResolver agentStatAgentIdsResolver(AgentStatAlarmDao agentStatAlarmDao) {
        return new AgentStatAgentIdsResolver(agentStatAlarmDao);
    }

    @Bean
    public IndexedAgentIdsResolver indexedAgentIdsResolver(AgentIdDao agentIdDao) {
        return new IndexedAgentIdsResolver(agentIdDao);
    }

    @Bean
    public ApplicationResponseMetricQueryService applicationResponseMetricQueryService(
            MapResponseDao mapResponseDao, RuleApplicationResolver applicationResolver) {
        return new ApplicationResponseMetricQueryService(mapResponseDao, applicationResolver);
    }

    @Bean
    public ApplicationOutCallMetricQueryService applicationOutCallMetricQueryService(
            MapOutLinkDao mapOutLinkDao, RuleApplicationResolver applicationResolver) {
        return new ApplicationOutCallMetricQueryService(mapOutLinkDao, applicationResolver);
    }

    @Bean
    public AgentStatMetricQueryService agentStatMetricQueryService(
            AgentStatAlarmDao agentStatAlarmDao,
            AgentStatAgentIdsResolver agentIdsResolver,
            RuleApplicationResolver applicationResolver) {
        return new AgentStatMetricQueryService(agentStatAlarmDao, agentIdsResolver, applicationResolver);
    }

    @Bean
    public AgentEventMetricQueryService agentEventMetricQueryService(
            AgentEventDao agentEventDao,
            IndexedAgentIdsResolver agentIdsResolver,
            RuleApplicationResolver applicationResolver) {
        return new AgentEventMetricQueryService(agentEventDao, agentIdsResolver, applicationResolver);
    }
}
