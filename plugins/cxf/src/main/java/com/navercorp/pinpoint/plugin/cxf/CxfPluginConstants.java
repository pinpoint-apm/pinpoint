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
 * @version 1.8.1
 * @since 2017/10/03
 */
public final class CxfPluginConstants {
    private CxfPluginConstants() {
    }

    @Deprecated
    public static final ServiceType CXF_CLIENT_SERVICE_TYPE = ServiceTypeFactory.of(9080, "CXF_CLIENT", ServiceTypeProperty.RECORD_STATISTICS);
    @Deprecated
    public static final AnnotationKey CXF_OPERATION = AnnotationKeyFactory.of(200, "cxf.operation", AnnotationKeyProperty.VIEW_IN_RECORD_SET);
    @Deprecated
    public static final AnnotationKey CXF_ARGS = AnnotationKeyFactory.of(201, "cxf.args", AnnotationKeyProperty.VIEW_IN_RECORD_SET);
    @Deprecated
    public static final String CXF_CLIENT_SCOPE = "CxfClientScope";

    public static final ServiceType CXF_SERVICE_INVOKER_SERVICE_TYPE = ServiceTypeFactory.of(9081, "CXF_SERVICE_INVOKER");
    public static final ServiceType CXF_MESSAGE_SENDER_SERVICE_TYPE = ServiceTypeFactory.of(9082, "CXF_MESSAGE_SENDER");
    public static final ServiceType CXF_LOGGING_IN_SERVICE_TYPE = ServiceTypeFactory.of(9083, "CXF_LOGGING_IN");
    public static final ServiceType CXF_LOGGING_OUT_SERVICE_TYPE = ServiceTypeFactory.of(9084, "CXF_LOGGING_OUT");

    public static final AnnotationKey CXF_ADDRESS = AnnotationKeyFactory.of(203, "cxf.address", AnnotationKeyProperty.VIEW_IN_RECORD_SET);
    public static final AnnotationKey CXF_RESPONSE_CODE = AnnotationKeyFactory.of(204, "cxf.response.code", AnnotationKeyProperty.VIEW_IN_RECORD_SET);
    public static final AnnotationKey CXF_ENCODING = AnnotationKeyFactory.of(205, "cxf.encoding", AnnotationKeyProperty.VIEW_IN_RECORD_SET);
    public static final AnnotationKey CXF_HTTP_METHOD = AnnotationKeyFactory.of(206, "cxf.http.method", AnnotationKeyProperty.VIEW_IN_RECORD_SET);
    public static final AnnotationKey CXF_CONTENT_TYPE = AnnotationKeyFactory.of(207, "cxf.content.type", AnnotationKeyProperty.VIEW_IN_RECORD_SET);
    public static final AnnotationKey CXF_HEADERS = AnnotationKeyFactory.of(208, "cxf.headers", AnnotationKeyProperty.VIEW_IN_RECORD_SET);
    public static final AnnotationKey CXF_MESSAGES = AnnotationKeyFactory.of(209, "cxf.messages", AnnotationKeyProperty.VIEW_IN_RECORD_SET);
    public static final AnnotationKey CXF_PAYLOAD = AnnotationKeyFactory.of(210, "cxf.payload", AnnotationKeyProperty.VIEW_IN_RECORD_SET);

    public static final String CXF_SCOPE = "CXF_SCOPE";

}