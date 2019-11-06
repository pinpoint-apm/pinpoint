/*
 * Copyright 2018 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.grpc;

import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.AnnotationKeyMatchers;
import com.navercorp.pinpoint.common.trace.TraceMetadataProvider;
import com.navercorp.pinpoint.common.trace.TraceMetadataSetupContext;

public class GrpcMetadataProvider implements TraceMetadataProvider {

    /**
     * @see TraceMetadataProvider#setup(TraceMetadataSetupContext)
     */
    @Override
    public void setup(TraceMetadataSetupContext context) {
        context.addServiceType(GrpcConstants.SERVICE_TYPE, AnnotationKeyMatchers.exact(AnnotationKey.HTTP_URL));
        context.addServiceType(GrpcConstants.SERVICE_TYPE_INTERNAL, AnnotationKeyMatchers.exact(GrpcConstants.CLIENT_STATUS_ANNOTATION));

        context.addServiceType(GrpcConstants.SERVER_SERVICE_TYPE);
        context.addServiceType(GrpcConstants.SERVER_SERVICE_TYPE_INTERNAL);
    }

}
