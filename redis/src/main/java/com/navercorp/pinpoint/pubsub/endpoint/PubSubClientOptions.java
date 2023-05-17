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
package com.navercorp.pinpoint.pubsub.endpoint;

import java.time.Duration;

/**
 * @author youngjin.kim2
 */
public class PubSubClientOptions {

    private final Duration requestTimeout;

    private PubSubClientOptions(
            Duration requestTimeout
    ) {
        this.requestTimeout = requestTimeout;
    }

    public static PubSubEndpointOptionsBuilder builder() {
        return new PubSubEndpointOptionsBuilder();
    }

    public Duration getRequestTimeout() {
        return requestTimeout;
    }

    public static class PubSubEndpointOptionsBuilder {

        private Duration requestTimeout = Duration.ofSeconds(5);

        private PubSubEndpointOptionsBuilder() {}

        public PubSubClientOptions build() {
            return new PubSubClientOptions(
                    requestTimeout
            );
        }

        public PubSubEndpointOptionsBuilder setRequestTimeout(Duration requestTimeout) {
            this.requestTimeout = requestTimeout;
            return this;
        }

    }

}
