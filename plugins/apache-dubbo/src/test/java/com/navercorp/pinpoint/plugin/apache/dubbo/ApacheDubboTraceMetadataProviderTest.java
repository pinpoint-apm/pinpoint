/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.apache.dubbo;

import com.navercorp.pinpoint.common.trace.TraceMetadataSetupContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ApacheDubboTraceMetadataProviderTest {

    @Mock
    TraceMetadataSetupContext context;

    @Test
    public void setup() {
        ApacheDubboTraceMetadataProvider provider = new ApacheDubboTraceMetadataProvider();

        provider.setup(context);

        verify(context).addServiceType(ApacheDubboConstants.DUBBO_PROVIDER_SERVICE_TYPE);
        verify(context).addServiceType(ApacheDubboConstants.DUBBO_CONSUMER_SERVICE_TYPE);
        verify(context).addServiceType(ApacheDubboConstants.DUBBO_PROVIDER_SERVICE_NO_STATISTICS_TYPE);
        verify(context).addAnnotationKey(ApacheDubboConstants.DUBBO_ARGS_ANNOTATION_KEY);
        verify(context).addAnnotationKey(ApacheDubboConstants.DUBBO_RESULT_ANNOTATION_KEY);
        verify(context).addAnnotationKey(ApacheDubboConstants.DUBBO_RPC_ANNOTATION_KEY);
    }
}