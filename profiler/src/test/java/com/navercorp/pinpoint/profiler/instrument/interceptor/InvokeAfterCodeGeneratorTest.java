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

package com.navercorp.pinpoint.profiler.instrument.interceptor;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor0;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor3;
import com.navercorp.pinpoint.profiler.metadata.ApiMetaDataService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * @author emeroad
 */
public class InvokeAfterCodeGeneratorTest {

    private final InterceptorDefinitionFactory interceptorDefinitionFactory = new InterceptorDefinitionFactory();

    @Test
    public void testGenerate_AroundInterceptor3_catchClause() {

        final Class<AroundInterceptor3> aroundInterceptor3Class = AroundInterceptor3.class;
        final InterceptorDefinition interceptorDefinition = interceptorDefinitionFactory.createInterceptorDefinition(aroundInterceptor3Class);

        final InstrumentClass mockClass = mock(InstrumentClass.class);
        Mockito.when(mockClass.getName()).thenReturn("TestClass");

        final InstrumentMethod mockMethod = mock(InstrumentMethod.class);
        Mockito.when(mockMethod.getName()).thenReturn("TestMethod");
        Mockito.when(mockMethod.getParameterTypes()).thenReturn(new String[]{"java.lang.Object", "java.lang.Object", "java.lang.Object"});
        Mockito.when(mockMethod.getReturnType()).thenReturn("java.lang.Object");

        ApiMetaDataService apiMetaDataService = mock(ApiMetaDataService.class);


        final InvokeAfterCodeGenerator invokeAfterCodeGenerator = new InvokeAfterCodeGenerator(100, interceptorDefinition, mockClass, mockMethod, apiMetaDataService, false, true);
        final String generate = invokeAfterCodeGenerator.generate();

        assertThat(generate)
                .contains("($w)$1")
                .contains("($w)$2")
                .contains("($w)$3")
                .contains("$e");

    }

    @Test
    public void testGenerate_AroundInterceptor3_NoCatchClause() {

        final Class<AroundInterceptor3> aroundInterceptor3Class = AroundInterceptor3.class;
        final InterceptorDefinition interceptorDefinition = interceptorDefinitionFactory.createInterceptorDefinition(aroundInterceptor3Class);

        final InstrumentClass mockClass = mock(InstrumentClass.class);
        Mockito.when(mockClass.getName()).thenReturn("TestClass");

        final InstrumentMethod mockMethod = mock(InstrumentMethod.class);
        Mockito.when(mockMethod.getName()).thenReturn("TestMethod");
        Mockito.when(mockMethod.getParameterTypes()).thenReturn(new String[]{"java.lang.Object", "java.lang.Object", "java.lang.Object"});
        Mockito.when(mockMethod.getReturnType()).thenReturn("java.lang.Object");

        ApiMetaDataService apiMetaDataService = mock(ApiMetaDataService.class);

        final InvokeAfterCodeGenerator invokeAfterCodeGenerator = new InvokeAfterCodeGenerator(100, interceptorDefinition, mockClass, mockMethod, apiMetaDataService, false, false);
        final String generate = invokeAfterCodeGenerator.generate();

        assertThat(generate)
                .contains("($w)$1")
                .contains("($w)$2")
                .contains("($w)$3")
                .contains("($w)$_");

    }

    @Test
    public void testGenerate_AroundInterceptor3_methodParam2() {

        final Class<AroundInterceptor3> aroundInterceptor3Class = AroundInterceptor3.class;
        final InterceptorDefinition interceptorDefinition = interceptorDefinitionFactory.createInterceptorDefinition(aroundInterceptor3Class);

        final InstrumentClass mockClass = mock(InstrumentClass.class);
        Mockito.when(mockClass.getName()).thenReturn("TestClass");

        final InstrumentMethod mockMethod = mock(InstrumentMethod.class);
        Mockito.when(mockMethod.getName()).thenReturn("TestMethod");
        Mockito.when(mockMethod.getParameterTypes()).thenReturn(new String[]{"java.lang.Object", "java.lang.Object"});
        Mockito.when(mockMethod.getReturnType()).thenReturn("java.lang.Object");

        ApiMetaDataService apiMetaDataService = mock(ApiMetaDataService.class);


        final InvokeAfterCodeGenerator invokeAfterCodeGenerator = new InvokeAfterCodeGenerator(100, interceptorDefinition, mockClass, mockMethod, apiMetaDataService, false, true);
        final String generate = invokeAfterCodeGenerator.generate();

        assertThat(generate)
                .contains("($w)$1")
                .contains("($w)$2")
                .doesNotContain("($w)$3")
                .contains("$e");

    }

    @Test
    public void testGenerate_AroundInterceptor3_methodParam4() {

        final Class<AroundInterceptor3> aroundInterceptor3Class = AroundInterceptor3.class;
        final InterceptorDefinition interceptorDefinition = interceptorDefinitionFactory.createInterceptorDefinition(aroundInterceptor3Class);

        final InstrumentClass mockClass = mock(InstrumentClass.class);
        Mockito.when(mockClass.getName()).thenReturn("TestClass");

        final InstrumentMethod mockMethod = mock(InstrumentMethod.class);
        Mockito.when(mockMethod.getName()).thenReturn("TestMethod");
        Mockito.when(mockMethod.getParameterTypes()).thenReturn(new String[]{"java.lang.Object", "java.lang.Object", "java.lang.Object", "java.lang.Object"});
        Mockito.when(mockMethod.getReturnType()).thenReturn("java.lang.Object");

        ApiMetaDataService apiMetaDataService = mock(ApiMetaDataService.class);

        final InvokeAfterCodeGenerator invokeAfterCodeGenerator = new InvokeAfterCodeGenerator(100, interceptorDefinition, mockClass, mockMethod, apiMetaDataService, false, true);
        final String generate = invokeAfterCodeGenerator.generate();

        assertThat(generate)
                .contains("($w)$1")
                .contains("($w)$2")
                .contains("($w)$3")
                .doesNotContain("($w)$4")
                .contains("$e");
    }


    @Test
    public void testGenerate_AroundInterceptor0() {

        final Class<AroundInterceptor0> aroundInterceptor3Class = AroundInterceptor0.class;
        final InterceptorDefinition interceptorDefinition = interceptorDefinitionFactory.createInterceptorDefinition(aroundInterceptor3Class);

        final InstrumentClass mockClass = mock(InstrumentClass.class);
        Mockito.when(mockClass.getName()).thenReturn("TestClass");

        final InstrumentMethod mockMethod = mock(InstrumentMethod.class);
        Mockito.when(mockMethod.getName()).thenReturn("TestMethod");
        Mockito.when(mockMethod.getParameterTypes()).thenReturn(new String[]{});
        Mockito.when(mockMethod.getReturnType()).thenReturn("java.lang.Object");

        ApiMetaDataService apiMetaDataService = mock(ApiMetaDataService.class);

        final InvokeAfterCodeGenerator invokeAfterCodeGenerator = new InvokeAfterCodeGenerator(100, interceptorDefinition, mockClass, mockMethod, apiMetaDataService, false, true);
        final String generate = invokeAfterCodeGenerator.generate();

        assertThat(generate)
                .doesNotContain("($w)$1")
                .doesNotContain("($w)$2")
                .doesNotContain("($w)$3")
                .contains("$e");

    }
}