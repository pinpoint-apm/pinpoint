/*
 * Copyright 2018 NAVER Corp.
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
public final class HystrixPluginConstants {
    private HystrixPluginConstants() {
    }

    public static final ServiceType HYSTRIX_SERVICE_TYPE = ServiceTypeFactory.of(9120, "HYSTRIX_COMMAND");
    public static final ServiceType HYSTRIX_INTERNAL_SERVICE_TYPE = ServiceTypeFactory.of(9121, "HYSTRIX_COMMAND_INTERNAL", "HYSTRIX_COMMAND");

    public static final AnnotationKey HYSTRIX_COMMAND_ANNOTATION_KEY = AnnotationKeyFactory.of(110, "hystrix.command", VIEW_IN_RECORD_SET);
    @Deprecated
    public static final AnnotationKey HYSTRIX_COMMAND_EXECUTION_ANNOTATION_KEY = AnnotationKeyFactory.of(111, "hystrix.command.execution", VIEW_IN_RECORD_SET);
    public static final AnnotationKey HYSTRIX_FALLBACK_CAUSE_ANNOTATION_KEY = AnnotationKeyFactory.of(112, "hystrix.command.fallback.cause", VIEW_IN_RECORD_SET);
    public static final AnnotationKey HYSTRIX_FALLBACK_EXCEPTION_ANNOTATION_KEY = AnnotationKeyFactory.of(113, "hystrix.command.exception", VIEW_IN_RECORD_SET);

    public static final AnnotationKey HYSTRIX_COMMAND_KEY_ANNOTATION_KEY = AnnotationKeyFactory.of(115, "hystrix.command.key", VIEW_IN_RECORD_SET);
    public static final AnnotationKey HYSTRIX_COMMAND_GROUP_KEY_ANNOTATION_KEY = AnnotationKeyFactory.of(116, "hystrix.command.group.key", VIEW_IN_RECORD_SET);
    public static final AnnotationKey HYSTRIX_THREAD_POOL_KEY_ANNOTATION_KEY = AnnotationKeyFactory.of(117, "hystrix.thread.pool.key", VIEW_IN_RECORD_SET);
    public static final AnnotationKey HYSTRIX_COLLAPSER_KEY_ANNOTATION_KEY = AnnotationKeyFactory.of(118, "hystrix.collapser.key", VIEW_IN_RECORD_SET);

    public static final String HYSTRIX_COMMAND_EXECUTION_SCOPE = "HystrixCommandExecutionScope";
}
