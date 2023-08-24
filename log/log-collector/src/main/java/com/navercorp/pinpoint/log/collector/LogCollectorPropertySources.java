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
package com.navercorp.pinpoint.log.collector;

import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

import static com.navercorp.pinpoint.log.collector.LogCollectorPropertySources.COLLECTOR;
import static com.navercorp.pinpoint.log.collector.LogCollectorPropertySources.GRPC_PROFILE;
import static com.navercorp.pinpoint.log.collector.LogCollectorPropertySources.GRPC_ROOT;

/**
 * @author youngjin.kim2
 */
@PropertySources({
        @PropertySource(name = "CollectorLogAppPropertySources-GRPC", value = { GRPC_ROOT, GRPC_PROFILE }),
        @PropertySource(name = "CollectorLogAppPropertySources", value = { COLLECTOR })
})
public class LogCollectorPropertySources {

    private static final String PROFILE = "classpath:log/profiles/${pinpoint.profiles.active:local}/";

    public static final String GRPC_ROOT = "classpath:log/pinpoint-collector-log-grpc-root.properties";
    public static final String GRPC_PROFILE = PROFILE + "pinpoint-collector-log-grpc.properties";

    public static final String COLLECTOR = "classpath:log/pinpoint-collector-log-root.properties";

}
