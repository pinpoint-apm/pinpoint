/*
 * Copyright 2015 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.thrift;

import com.navercorp.pinpoint.common.trace.AnnotationKeyMatchers;
import com.navercorp.pinpoint.common.trace.TraceMetadataProvider;
import com.navercorp.pinpoint.common.trace.TraceMetadataSetupContext;

/**
 * @author HyunGil Jeong
 */
public class ThriftTypeProvider implements TraceMetadataProvider {

    @Override
    public void setup(TraceMetadataSetupContext context) {
        context.addServiceType(ThriftConstants.THRIFT_SERVER);
        context.addServiceType(ThriftConstants.THRIFT_CLIENT, AnnotationKeyMatchers.exact(ThriftConstants.THRIFT_URL));
        context.addServiceType(ThriftConstants.THRIFT_SERVER_INTERNAL, AnnotationKeyMatchers.ARGS_MATCHER);
        context.addServiceType(ThriftConstants.THRIFT_CLIENT_INTERNAL, AnnotationKeyMatchers.ARGS_MATCHER);
        context.addAnnotationKey(ThriftConstants.THRIFT_URL);
        context.addAnnotationKey(ThriftConstants.THRIFT_ARGS);
        context.addAnnotationKey(ThriftConstants.THRIFT_RESULT);
    }

}
