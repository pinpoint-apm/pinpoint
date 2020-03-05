/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.provider;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceRepository;
import com.navercorp.pinpoint.profiler.receiver.CommandDispatcher;
import com.navercorp.pinpoint.profiler.receiver.ProfilerCommandLocatorBuilder;
import com.navercorp.pinpoint.profiler.receiver.ProfilerCommandServiceLocator;
import com.navercorp.pinpoint.profiler.receiver.service.ActiveThreadService;
import com.navercorp.pinpoint.profiler.receiver.service.EchoService;

/**
 * @author Woonduk Kang(emeroad)
 */
public class CommandDispatcherProvider implements Provider<CommandDispatcher> {

    private final ProfilerConfig profilerConfig;
    private final ActiveTraceRepository activeTraceRepository;

    @Inject
    public CommandDispatcherProvider(ProfilerConfig profilerConfig, Provider<ActiveTraceRepository> activeTraceRepositoryProvider) {
        this.profilerConfig = Assert.requireNonNull(profilerConfig, "profilerConfig");

        Assert.requireNonNull(activeTraceRepositoryProvider, "activeTraceRepositoryProvider");
        this.activeTraceRepository = activeTraceRepositoryProvider.get();
    }

    @Override
    public CommandDispatcher get() {
        ProfilerCommandLocatorBuilder builder = new ProfilerCommandLocatorBuilder();
        builder.addService(new EchoService());
        if (activeTraceRepository != null) {
            ActiveThreadService activeThreadService = new ActiveThreadService(profilerConfig, activeTraceRepository);
            builder.addService(activeThreadService);
        }

        ProfilerCommandServiceLocator commandServiceLocator = builder.build();
        CommandDispatcher commandDispatcher = new CommandDispatcher(commandServiceLocator);
        return commandDispatcher;
    }
}
