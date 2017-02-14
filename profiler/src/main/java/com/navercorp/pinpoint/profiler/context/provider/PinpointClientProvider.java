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
import com.navercorp.pinpoint.rpc.client.PinpointClient;
import com.navercorp.pinpoint.rpc.client.PinpointClientFactory;
import com.navercorp.pinpoint.rpc.util.ClientFactoryUtils;

/**
 * @author Woonduk Kang(emeroad)
 */
public class PinpointClientProvider implements Provider<PinpointClient> {
    private final ProfilerConfig profilerConfig;
    private final PinpointClientFactory clientFactory;

    @Inject
    public PinpointClientProvider(ProfilerConfig profilerConfig, PinpointClientFactory clientFactory) {
        if (profilerConfig == null) {
            throw new NullPointerException("profilerConfig must not be null");
        }
        if (clientFactory == null) {
            throw new NullPointerException("clientFactory must not be null");
        }
        this.profilerConfig = profilerConfig;
        this.clientFactory = clientFactory;
    }

    @Override
    public PinpointClient get() {
        return ClientFactoryUtils.createPinpointClient(profilerConfig.getCollectorTcpServerIp(), profilerConfig.getCollectorTcpServerPort(), clientFactory);
    }
}
