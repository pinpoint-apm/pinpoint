/*
 * Copyright 2026 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.pulsar.field.accessor;

/**
 * Holds the ClientConfigurationData of the client that created the target, so that the service URL can be read
 * without linking against the implementation types (the shaded pulsar-client 5.x relocates them).
 */
public interface ClientConfigAccessor {

    void _$PINPOINT$_setClientConfig(Object clientConfig);

    Object _$PINPOINT$_getClientConfig();
}
