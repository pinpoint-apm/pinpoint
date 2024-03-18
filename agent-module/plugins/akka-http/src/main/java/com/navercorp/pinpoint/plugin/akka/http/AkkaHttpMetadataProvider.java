/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.akka.http;

import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.AnnotationKeyMatchers;
import com.navercorp.pinpoint.common.trace.TraceMetadataProvider;
import com.navercorp.pinpoint.common.trace.TraceMetadataSetupContext;

/**
 * @author lopiter
 */
public class AkkaHttpMetadataProvider implements TraceMetadataProvider {

    @Override
    public void setup(TraceMetadataSetupContext context) {
        context.addServiceType(AkkaHttpConstants.AKKA_HTTP_SERVER);
        context.addServiceType(AkkaHttpConstants.AKKA_HTTP_SERVER_INTERNAL, AnnotationKeyMatchers.exact(AnnotationKey.HTTP_STATUS_CODE));
        // for backward compatibility
        context.addServiceType(AkkaHttpConstants.AKKA_HTTP_SERVER_INTERNAL_LEGACY, AnnotationKeyMatchers.exact(AnnotationKey.HTTP_STATUS_CODE));
    }

}
