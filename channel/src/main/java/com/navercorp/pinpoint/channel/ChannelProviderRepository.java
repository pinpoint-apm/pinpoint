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
package com.navercorp.pinpoint.channel;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author youngjin.kim2
 */
public class ChannelProviderRepository {

    private final Map<String, ChannelProvider> providerMap;

    public ChannelProviderRepository(Iterable<ChannelProviderRegistry> registries) {
        Map<String, ChannelProvider> providerMap = new LinkedHashMap<>();
        for (ChannelProviderRegistry registry: registries) {
            providerMap.put(registry.getScheme(), registry.getProvider());
        }
        this.providerMap = providerMap;
    }

    public PubChannel getPubChannel(URI uri) {
        return getChannelProvider(uri).getPubChannel(uri.getSchemeSpecificPart());
    }

    public SubChannel getSubChannel(URI uri) {
        return getChannelProvider(uri).getSubChannel(uri.getSchemeSpecificPart());
    }

    private ChannelProvider getChannelProvider(URI uri) {
        ChannelProvider provider = this.providerMap.get(uri.getScheme());
        if (provider == null) {
            throw new IllegalArgumentException("Scheme not found: " + uri.getScheme());
        }
        return provider;
    }

}
