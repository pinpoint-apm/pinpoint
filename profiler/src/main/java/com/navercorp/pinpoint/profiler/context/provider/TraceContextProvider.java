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
import com.navercorp.pinpoint.bootstrap.context.ServerMetaDataHolder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.JdbcContext;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.AgentInformation;
import com.navercorp.pinpoint.profiler.context.DefaultTraceContext;
import com.navercorp.pinpoint.profiler.context.TraceFactory;
import com.navercorp.pinpoint.profiler.context.id.TraceIdFactory;
import com.navercorp.pinpoint.profiler.metadata.ApiMetaDataService;
import com.navercorp.pinpoint.profiler.metadata.SqlMetaDataService;
import com.navercorp.pinpoint.profiler.metadata.StringMetaDataService;

/**
 * @author Woonduk Kang(emeroad)
 */
public class TraceContextProvider implements Provider<TraceContext> {
    private final ProfilerConfig profilerConfig;
    private final Provider<AgentInformation> agentInformationProvider;

    private final TraceIdFactory traceIdFactory;
    private final TraceFactory traceFactory;

    private final ServerMetaDataHolder serverMetaDataHolder;
    private final ApiMetaDataService apiMetaDataService;
    private final StringMetaDataService stringMetaDataService;
    private final SqlMetaDataService sqlMetaDataService;
    private final JdbcContext jdbcContext;

    @Inject
    public TraceContextProvider(ProfilerConfig profilerConfig,
                                final Provider<AgentInformation> agentInformationProvider,
                                TraceIdFactory traceIdFactory,
                                TraceFactory traceFactory,
                                ServerMetaDataHolder serverMetaDataHolder,
                                ApiMetaDataService apiMetaDataService,
                                StringMetaDataService stringMetaDataService,
                                SqlMetaDataService sqlMetaDataService,
                                JdbcContext jdbcContext) {
        this.profilerConfig = Assert.requireNonNull(profilerConfig, "profilerConfig");
        this.agentInformationProvider = Assert.requireNonNull(agentInformationProvider, "agentInformationProvider");

        this.traceIdFactory = Assert.requireNonNull(traceIdFactory, "traceIdFactory");
        this.traceFactory = Assert.requireNonNull(traceFactory, "traceFactory");

        this.serverMetaDataHolder = Assert.requireNonNull(serverMetaDataHolder, "serverMetaDataHolder");
        this.apiMetaDataService = Assert.requireNonNull(apiMetaDataService, "apiMetaDataService");
        this.stringMetaDataService = Assert.requireNonNull(stringMetaDataService, "stringMetaDataService");
        this.sqlMetaDataService = Assert.requireNonNull(sqlMetaDataService, "sqlMetaDataService");
        this.jdbcContext = Assert.requireNonNull(jdbcContext, "jdbcContext");
    }


    @Override
    public TraceContext get() {
        AgentInformation agentInformation = this.agentInformationProvider.get();
        return new DefaultTraceContext(profilerConfig, agentInformation, traceIdFactory, traceFactory,
                serverMetaDataHolder, apiMetaDataService, stringMetaDataService, sqlMetaDataService, jdbcContext);
    }
}
