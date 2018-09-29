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
package com.navercorp.pinpoint.plugin.cxf;

import com.navercorp.pinpoint.common.trace.*;

/**
 * @author barney
 * @author Victor.Zxy
 */
public final class CxfPluginConstants {
    private CxfPluginConstants() {
    }

    public static final ServiceType CXF_SERVER_SERVICE_TYPE = ServiceTypeFactory.of(1190, "CXF_SERVER", ServiceTypeProperty.RECORD_STATISTICS);
    public static final ServiceType CXF_CLIENT_SERVICE_TYPE = ServiceTypeFactory.of(9080, "CXF_CLIENT", ServiceTypeProperty.RECORD_STATISTICS);

    public static final AnnotationKey CXF_OPERATION = AnnotationKeyFactory.of(200, "cxf.operation", AnnotationKeyProperty.VIEW_IN_RECORD_SET);
    public static final AnnotationKey CXF_ARGS = AnnotationKeyFactory.of(201, "cxf.args", AnnotationKeyProperty.VIEW_IN_RECORD_SET);

    public static final AnnotationKey CXF_URI = AnnotationKeyFactory.of(202, "cxf.http.uri", AnnotationKeyProperty.VIEW_IN_RECORD_SET);
    public static final AnnotationKey CXF_METHOD = AnnotationKeyFactory.of(203, "cxf.request.method", AnnotationKeyProperty.VIEW_IN_RECORD_SET);
    public static final AnnotationKey CXF_TYPE = AnnotationKeyFactory.of(204, "cxf.content.type", AnnotationKeyProperty.VIEW_IN_RECORD_SET);

    public static final String CXF_CLIENT_SCOPE = "CxfClientScope";
}
