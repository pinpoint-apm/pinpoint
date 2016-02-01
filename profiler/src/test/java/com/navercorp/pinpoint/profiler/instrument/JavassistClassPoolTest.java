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

package com.navercorp.pinpoint.profiler.instrument;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.profiler.interceptor.registry.InterceptorRegistryBinder;
import com.navercorp.pinpoint.test.TestInterceptorRegistryBinder;
import com.navercorp.pinpoint.test.util.BytecodeUtils;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.bytecode.ConstPool;
import javassist.bytecode.MethodInfo;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * @author Woonduk Kang(emeroad)
 */
public class JavassistClassPoolTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String mock = "com.navercorp.pinpoint.profiler.instrument.JavassistClassPoolTestMock";

    @Test
    public void testGetClass_original() throws Exception {
        InterceptorRegistryBinder binder = new TestInterceptorRegistryBinder();
        JavassistClassPool pool = new JavassistClassPool(binder, null);


        final byte[] originalByteCode = BytecodeUtils.getClassFile(null, mock);
        final InstrumentClass transformClass = pool.getClass(null, mock, originalByteCode);

        Assert.assertNotNull(transformClass.getDeclaredMethod("test"));
        Assert.assertNull("transform method", transformClass.getDeclaredMethod("transformMethod"));

    }

    @Test
    public void testGetClass_transform() throws Exception {
        InterceptorRegistryBinder binder = new TestInterceptorRegistryBinder();
        JavassistClassPool pool = new JavassistClassPool(binder, null);


        final byte[] transformByteCode = getTransformByteCode();
        final InstrumentClass transformClass = pool.getClass(null, mock, transformByteCode);

        Assert.assertNotNull(transformClass.getDeclaredMethod("test"));
        Assert.assertNotNull("transform method", transformClass.getDeclaredMethod("transformMethod"));
    }

    public byte[] getTransformByteCode()  {
        try {
            final ClassPool pool = new ClassPool(true);

            final CtClass ctClass = pool.get(mock);

            final ConstPool constPool = ctClass.getClassFile2().getConstPool();

            MethodInfo info = new MethodInfo(constPool, "transformMethod", "()V");
            final CtMethod newMethod = CtMethod.make(info, ctClass);
            ctClass.addMethod(newMethod);
            return ctClass.toBytecode();
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

}

