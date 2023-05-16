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
import com.navercorp.pinpoint.bootstrap.context.ServerMetaDataHolder;
import java.util.Objects;
import com.navercorp.pinpoint.profiler.context.DefaultServerMetaDataHolder;
import com.navercorp.pinpoint.profiler.context.ServerMetaDataRegistryService;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ServerMetaDataHolderProvider implements Provider<ServerMetaDataHolder> {

    private final ServerMetaDataRegistryService serverMetaDataRegistryService;

    @Inject
    public ServerMetaDataHolderProvider(ServerMetaDataRegistryService serverMetaDataRegistryService) {
        this.serverMetaDataRegistryService = Objects.requireNonNull(serverMetaDataRegistryService, "serverMetaDataRegistryService");
    }

    @Override
    public ServerMetaDataHolder get() {
        ServerMetaDataHolder serverMetaDataHolder = new DefaultServerMetaDataHolder(serverMetaDataRegistryService);
        return serverMetaDataHolder;
    }

}
