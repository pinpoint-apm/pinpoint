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
import org.junit.Assert;
import org.junit.Test;

public class ApacheDubboProviderMethodDescriptorTest {

    @Test
    public void test() {
        ApacheDubboProviderMethodDescriptor descriptor = new ApacheDubboProviderMethodDescriptor();

        Assert.assertEquals(descriptor.getApiDescriptor(), "Apache Dubbo Provider Process");
        Assert.assertEquals(descriptor.getApiId(), 0);
        Assert.assertNull(descriptor.getClassName());
        Assert.assertEquals(descriptor.getFullName(), "com.navercorp.pinpoint.plugin.apache.dubbo.ApacheDubboProviderMethodDescriptor.invoke()");

        Assert.assertEquals(descriptor.getLineNumber(), -1);
        Assert.assertNull(descriptor.getMethodName());

        Assert.assertNull(descriptor.getParameterDescriptor());

        Assert.assertArrayEquals(descriptor.getParameterTypes(), new String[0]);

        Assert.assertArrayEquals(descriptor.getParameterVariableName(), new String[0]);
        Assert.assertEquals(descriptor.getType(), MethodType.WEB_REQUEST);
    }
}