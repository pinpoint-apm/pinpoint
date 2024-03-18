/*
 *  Copyright 2018 NAVER Corp.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.navercorp.pinpoint.plugin.cxf;

import com.navercorp.pinpoint.common.trace.TraceMetadataProvider;
import com.navercorp.pinpoint.common.trace.TraceMetadataSetupContext;

/**
 * @author barney
 * @author Victor.Zxy
 * @version 1.8.1
 * @since 2017/10/03
 */
public class CxfPluginTraceMetadataProvider implements TraceMetadataProvider {

    @Override
    public void setup(TraceMetadataSetupContext context) {
        context.addServiceType(CxfPluginConstants.CXF_CLIENT_SERVICE_TYPE);
        context.addAnnotationKey(CxfPluginConstants.CXF_OPERATION);
        context.addAnnotationKey(CxfPluginConstants.CXF_ARGS);

        context.addServiceType(CxfPluginConstants.CXF_SERVICE_INVOKER_SERVICE_TYPE);
        context.addServiceType(CxfPluginConstants.CXF_MESSAGE_SENDER_SERVICE_TYPE);
        context.addServiceType(CxfPluginConstants.CXF_LOGGING_IN_SERVICE_TYPE);
        context.addServiceType(CxfPluginConstants.CXF_LOGGING_OUT_SERVICE_TYPE);

        context.addAnnotationKey(CxfPluginConstants.CXF_ADDRESS);
        context.addAnnotationKey(CxfPluginConstants.CXF_RESPONSE_CODE);
        context.addAnnotationKey(CxfPluginConstants.CXF_CONTENT_TYPE);
        context.addAnnotationKey(CxfPluginConstants.CXF_ENCODING);
        context.addAnnotationKey(CxfPluginConstants.CXF_HTTP_METHOD);
        context.addAnnotationKey(CxfPluginConstants.CXF_HEADERS);
        context.addAnnotationKey(CxfPluginConstants.CXF_MESSAGES);
        context.addAnnotationKey(CxfPluginConstants.CXF_PAYLOAD);
    }

}