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

package com.navercorp.pinpoint.plugin.dubbo;

import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.AnnotationKeyFactory;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;

import static com.navercorp.pinpoint.common.trace.AnnotationKeyProperty.VIEW_IN_RECORD_SET;
import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.RECORD_STATISTICS;

/**
 * @author Jinkai.Ma
 */
public final class DubboConstants {
    private DubboConstants() {
    }

    public static final ServiceType DUBBO_PROVIDER_SERVICE_TYPE = ServiceTypeFactory.of(1110, "DUBBO_PROVIDER", RECORD_STATISTICS);
    public static final ServiceType DUBBO_CONSUMER_SERVICE_TYPE = ServiceTypeFactory.of(9110, "DUBBO_CONSUMER", RECORD_STATISTICS);
    public static final ServiceType DUBBO_PROVIDER_SERVICE_NO_STATISTICS_TYPE = ServiceTypeFactory.of(9111, "DUBBO");
    public static final AnnotationKey DUBBO_ARGS_ANNOTATION_KEY = AnnotationKeyFactory.of(90, "dubbo.args");
    public static final AnnotationKey DUBBO_RESULT_ANNOTATION_KEY = AnnotationKeyFactory.of(91, "dubbo.result");
    public static final AnnotationKey DUBBO_RPC_ANNOTATION_KEY = AnnotationKeyFactory.of(92, "dubbo.rpc", VIEW_IN_RECORD_SET);

    public static final String META_DO_NOT_TRACE = "_DUBBO_DO_NOT_TRACE";
    public static final String META_TRANSACTION_ID = "_DUBBO_TRASACTION_ID";
    public static final String META_SPAN_ID = "_DUBBO_SPAN_ID";
    public static final String META_PARENT_SPAN_ID = "_DUBBO_PARENT_SPAN_ID";
    public static final String META_PARENT_APPLICATION_NAME = "_DUBBO_PARENT_APPLICATION_NAME";
    public static final String META_PARENT_APPLICATION_TYPE = "_DUBBO_PARENT_APPLICATION_TYPE";
    public static final String META_FLAGS = "_DUBBO_FLAGS";
    public static final String META_HOST = "_DUBBO_HOST";

    public static final String MONITOR_SERVICE_FQCN = "com.alibaba.dubbo.monitor.MonitorService";
}
