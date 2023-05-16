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

import com.navercorp.pinpoint.bootstrap.interceptor.registry.InterceptorRegistry;
import com.navercorp.pinpoint.bootstrap.interceptor.registry.InterceptorRegistryAdaptor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class InterceptorRegistryTest {

    private InterceptorRegistryAdaptor registryAdaptor;

    @BeforeEach
    public void setUp() throws Exception {
        registryAdaptor = mock(InterceptorRegistryAdaptor.class);

        InterceptorRegistry.bind(registryAdaptor, null);
    }

    @AfterEach
    public void tearDown() throws Exception {
        InterceptorRegistry.unbind(null);
    }

    @Test
    public void testSimpleInterceptor() {

        AroundInterceptor simpleAroundInterceptor = mock(AroundInterceptor.class);
        when(registryAdaptor.getInterceptor(0)).thenReturn(simpleAroundInterceptor);


        int findId = registryAdaptor.addInterceptor(simpleAroundInterceptor);
        Interceptor find = InterceptorRegistry.getInterceptor(findId);
        Assertions.assertSame(find, simpleAroundInterceptor);

    }

    @Test
    public void testStaticInterceptor() {

        StaticAroundInterceptor staticAroundInterceptor = mock(StaticAroundInterceptor.class);
        when(registryAdaptor.getInterceptor(0)).thenReturn(staticAroundInterceptor);

        int findId = registryAdaptor.addInterceptor(staticAroundInterceptor);
        Interceptor find = InterceptorRegistry.getInterceptor(findId);
        Assertions.assertSame(find, staticAroundInterceptor);

    }
}