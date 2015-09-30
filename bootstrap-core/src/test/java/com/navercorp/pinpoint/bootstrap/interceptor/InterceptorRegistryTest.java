/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.interceptor;

import static org.mockito.Mockito.*;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.navercorp.pinpoint.bootstrap.interceptor.registry.InterceptorRegistry;
import com.navercorp.pinpoint.bootstrap.interceptor.registry.InterceptorRegistryAdaptor;

public class InterceptorRegistryTest {

    private InterceptorRegistryAdaptor registryAdaptor;

    @Before
    public void setUp() throws Exception {
        registryAdaptor = mock(InterceptorRegistryAdaptor.class);

        InterceptorRegistry.bind(registryAdaptor, null);
    }

    @After
    public void tearDown() throws Exception {
        InterceptorRegistry.unbind(null);
    }

    @Test
    public void testSimpleInterceptor() throws Exception {

        AroundInterceptor simpleAroundInterceptor = mock(AroundInterceptor.class);
        when(registryAdaptor.getInterceptor(0)).thenReturn(simpleAroundInterceptor);


        int findId = registryAdaptor.addInterceptor(simpleAroundInterceptor);
        Interceptor find = InterceptorRegistry.getInterceptor(findId);
        Assert.assertSame(find, simpleAroundInterceptor);

    }

    @Test
    public void testStaticInterceptor() throws Exception {

        StaticAroundInterceptor staticAroundInterceptor = mock(StaticAroundInterceptor.class);
        when(registryAdaptor.getInterceptor(0)).thenReturn(staticAroundInterceptor);

        int findId = registryAdaptor.addInterceptor(staticAroundInterceptor);
        Interceptor find = InterceptorRegistry.getInterceptor(findId);
        Assert.assertSame(find, staticAroundInterceptor);

    }
}