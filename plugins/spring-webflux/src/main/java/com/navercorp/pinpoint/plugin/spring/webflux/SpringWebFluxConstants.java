/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.spring.webflux;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeProvider;

/**
 * @author jaehong.kim
 */
public class SpringWebFluxConstants {
    public static final ServiceType SPRING_WEBFLUX = ServiceTypeProvider.getByName("SPRING_WEBFLUX");
    public static final ServiceType SPRING_WEBFLUX_CLIENT = ServiceTypeProvider.getByName("SPRING_WEBFLUX_CLIENT");
    public static final String SPRING_WEBFLUX_DEFAULT_URI_ATTRIBUTE_KEYS[] = {"org.springframework.web.reactive.HandlerMapping.bestMatchingPattern"};
    public static final String[] SPRING_WEBFLUX_URI_USER_INPUT_ATTRIBUTE_KEYS = {"pinpoint.metric.uri-template"};

}
