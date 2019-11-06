/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.grpc.server;

import com.navercorp.pinpoint.grpc.Header;
import io.grpc.Context;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ServerContext {

    private static final Context.Key<Header> AGENT_INFO_KEY = Context.key("agentinfo");

    private static final Context.Key<TransportMetadata> TRANSPORT_METADATA_KEY = Context.key("transportmetadata");

    public static Context.Key<Header> getAgentInfoKey() {
        return AGENT_INFO_KEY;
    }

    public static Context.Key<TransportMetadata> getTransportMetadataKey() {
        return TRANSPORT_METADATA_KEY;
    }

    public static Header getAgentInfo() {
        final Context current = Context.current();
        return getAgentInfo(current);
    }

    public static Header getAgentInfo(Context context) {
        return AGENT_INFO_KEY.get(context);
    }

    public static TransportMetadata getTransportMetadata() {
        final Context current = Context.current();
        return getTransportMetadata(current);
    }

    public static TransportMetadata getTransportMetadata(Context context) {
        return TRANSPORT_METADATA_KEY.get(context);
    }
}
