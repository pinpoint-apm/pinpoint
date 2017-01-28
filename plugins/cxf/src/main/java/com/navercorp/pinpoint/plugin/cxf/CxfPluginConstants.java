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
package com.navercorp.pinpoint.plugin.cxf;

import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.AnnotationKeyFactory;
import com.navercorp.pinpoint.common.trace.AnnotationKeyProperty;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;
import com.navercorp.pinpoint.common.trace.ServiceTypeProperty;

/**
 * @author barney
 *
 */
public interface CxfPluginConstants {

    ServiceType CXF_CLIENT_SERVICE_TYPE = ServiceTypeFactory.of(9080, "CXF_CLIENT", ServiceTypeProperty.RECORD_STATISTICS);

    AnnotationKey CXF_OPERATION = AnnotationKeyFactory.of(200, "cxf.operation", AnnotationKeyProperty.VIEW_IN_RECORD_SET);
    AnnotationKey CXF_ARGS = AnnotationKeyFactory.of(201, "cxf.args", AnnotationKeyProperty.VIEW_IN_RECORD_SET);

    String CXF_CLIENT_SCOPE = "CxfClientScope";
}
