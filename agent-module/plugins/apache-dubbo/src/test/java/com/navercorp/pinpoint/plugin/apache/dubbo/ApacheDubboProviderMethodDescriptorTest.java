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

import com.navercorp.pinpoint.common.trace.MethodType;
import com.navercorp.pinpoint.common.util.LineNumber;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ApacheDubboProviderMethodDescriptorTest {

    @Test
    public void test() {
        ApacheDubboProviderMethodDescriptor descriptor = new ApacheDubboProviderMethodDescriptor();

        Assertions.assertEquals(descriptor.getApiDescriptor(), "Apache Dubbo Provider Process");
        Assertions.assertEquals(descriptor.getApiId(), 0);
        Assertions.assertNull(descriptor.getClassName());
        Assertions.assertEquals(descriptor.getFullName(), "com.navercorp.pinpoint.plugin.apache.dubbo.ApacheDubboProviderMethodDescriptor.invoke()");

        Assertions.assertEquals(descriptor.getLineNumber(), LineNumber.NO_LINE_NUMBER);
        Assertions.assertNull(descriptor.getMethodName());

        Assertions.assertNull(descriptor.getParameterDescriptor());

        Assertions.assertArrayEquals(descriptor.getParameterTypes(), new String[0]);

        Assertions.assertArrayEquals(descriptor.getParameterVariableName(), new String[0]);
        Assertions.assertEquals(descriptor.getType(), MethodType.WEB_REQUEST);
    }
}