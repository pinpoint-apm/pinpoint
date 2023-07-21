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
package com.navercorp.pinpoint.web.realtime.activethread.count.service;

import com.navercorp.pinpoint.web.realtime.activethread.count.dao.ActiveThreadCountDao;
import com.navercorp.pinpoint.web.realtime.service.AgentLookupService;
import com.navercorp.pinpoint.web.task.TimerTaskDecoratorFactory;

import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author youngjin.kim2
 */
public class ActiveThreadCountServiceImpl implements ActiveThreadCountService {

    private final ActiveThreadCountDao atcDao;
    private final AgentLookupService agentLookupService;
    private final ScheduledExecutorService scheduledExecutor;
    private final ActiveThreadCountSessionImpl.ATCPeriods periods;
    private final TimerTaskDecoratorFactory timerTaskDecoratorFactory;

    public ActiveThreadCountServiceImpl(
            ActiveThreadCountDao atcDao,
            AgentLookupService agentLookupService,
            ScheduledExecutorService scheduledExecutor,
            ActiveThreadCountSessionImpl.ATCPeriods periods,
            TimerTaskDecoratorFactory timerTaskDecoratorFactory
    ) {
        this.atcDao = Objects.requireNonNull(atcDao, "atcDao");
        this.agentLookupService = Objects.requireNonNull(agentLookupService, "agentLookupService");
        this.scheduledExecutor = Objects.requireNonNull(scheduledExecutor, "scheduledExecutor");
        this.periods = periods;
        this.timerTaskDecoratorFactory = timerTaskDecoratorFactory;
    }

    @Override
    public ActiveThreadCountSession getSession(String applicationName) {
        return new ActiveThreadCountSessionImpl(
                applicationName,
                this.atcDao,
                this.agentLookupService,
                this.scheduledExecutor,
                this.periods,
                this.timerTaskDecoratorFactory.createTimerTaskDecorator()
        );
    }

}
