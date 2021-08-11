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
import java.util.Objects;

import com.navercorp.pinpoint.bootstrap.sampler.Sampler;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceRepository;
import com.navercorp.pinpoint.profiler.context.thrift.config.ThriftTransportConfig;
import com.navercorp.pinpoint.profiler.receiver.CommandDispatcher;
import com.navercorp.pinpoint.profiler.receiver.ProfilerCommandLocatorBuilder;
import com.navercorp.pinpoint.profiler.receiver.ProfilerCommandServiceLocator;
import com.navercorp.pinpoint.profiler.receiver.service.ActiveThreadService;
import com.navercorp.pinpoint.profiler.receiver.service.EchoService;
import com.navercorp.pinpoint.profiler.receiver.service.SamplingRateService;

/**
 * @author Woonduk Kang(emeroad)
 * @author yjqg6666
 */
public class CommandDispatcherProvider implements Provider<CommandDispatcher> {

    private final ThriftTransportConfig thriftTransportConfig;
    private final ActiveTraceRepository activeTraceRepository;
    private final Sampler sampler;

    @Inject
    public CommandDispatcherProvider(
        ThriftTransportConfig thriftTransportConfig,
        Provider<ActiveTraceRepository> activeTraceRepositoryProvider,
        Provider<Sampler> samplerProvider
    ) {
        this.thriftTransportConfig = Objects.requireNonNull(thriftTransportConfig, "thriftTransportConfig");
        Objects.requireNonNull(activeTraceRepositoryProvider, "activeTraceRepositoryProvider");
        Objects.requireNonNull(samplerProvider, "samplerProvider");
        this.activeTraceRepository = activeTraceRepositoryProvider.get();
        this.sampler = samplerProvider.get();
    }

    @Override
    public CommandDispatcher get() {
        ProfilerCommandLocatorBuilder builder = new ProfilerCommandLocatorBuilder();
        builder.addService(new EchoService());
        builder.addService(new SamplingRateService(sampler));
        if (activeTraceRepository != null) {
            ActiveThreadService activeThreadService = new ActiveThreadService(thriftTransportConfig, activeTraceRepository);
            builder.addService(activeThreadService);
        }

        ProfilerCommandServiceLocator commandServiceLocator = builder.build();
        CommandDispatcher commandDispatcher = new CommandDispatcher(commandServiceLocator);
        return commandDispatcher;
    }
}
