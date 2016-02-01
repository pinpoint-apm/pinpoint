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

package com.navercorp.pinpoint.profiler;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;

import static org.mockito.Mockito.*;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DynamicTransformServiceTest {

    @Test()
    public void testRetransform_Fail_memoryleak_prevent() throws Exception {
        final Instrumentation instrumentation = mock(Instrumentation.class);
        when(instrumentation.isModifiableClass(any(Class.class))).thenReturn(true);
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                throw new UnmodifiableClassException();
            }
        }).when(instrumentation).retransformClasses(any(Class.class));


        DefaultDynamicTransformerRegistry listener = new DefaultDynamicTransformerRegistry();

        final ClassFileTransformer classFileTransformer = mock(ClassFileTransformer.class);

        DynamicTransformService dynamicTransformService = new DynamicTransformService(instrumentation, listener);

        try {
            dynamicTransformService.retransform(String.class, classFileTransformer);
            Assert.fail("expected retransform fail");
        } catch (Exception e) {
        }
        Assert.assertEquals(listener.size(), 0);
    }

//    @Test
//    public void testAddClassFileTransformer() throws Exception {
//
//    }
}