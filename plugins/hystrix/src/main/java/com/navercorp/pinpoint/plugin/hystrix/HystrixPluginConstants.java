/*
 * Copyright 2014 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.hystrix;

import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.AnnotationKeyFactory;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;

import static com.navercorp.pinpoint.common.trace.AnnotationKeyProperty.VIEW_IN_RECORD_SET;

/**
 * @author Jiaqi Feng
 * @author HyunGil Jeong
 */
public interface HystrixPluginConstants {
    ServiceType HYSTRIX_SERVICE_TYPE = ServiceTypeFactory.of(9120, "HYSTRIX_COMMAND");
    ServiceType HYSTRIX_INTERNAL_SERVICE_TYPE = ServiceTypeFactory.of(9121, "HYSTRIX_COMMAND_INTERNAL", "HYSTRIX_COMMAND");

    AnnotationKey HYSTRIX_COMMAND_ANNOTATION_KEY = AnnotationKeyFactory.of(110, "hystrix.command", VIEW_IN_RECORD_SET);
    AnnotationKey HYSTRIX_COMMAND_EXECUTION_ANNOTATION_KEY = AnnotationKeyFactory.of(111, "hystrix.command.execution", VIEW_IN_RECORD_SET);
    AnnotationKey HYSTRIX_FALLBACK_CAUSE_ANNOTATION_KEY = AnnotationKeyFactory.of(112, "hystrix.command.fallback.cause", VIEW_IN_RECORD_SET);

    String HYSTRIX_COMMAND_EXECUTION_SCOPE = "HystrixCommandExecutionScope";

    String HYSTRIX_COMMAND_GET_EXECUTION_EXCEPTION_METHOD_NAME = "getExecutionException";

    String EXECUTION_TYPE_EXECUTION = "run";
    String EXECUTION_TYPE_FALLBACK = "fallback";
}
