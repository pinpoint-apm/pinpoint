/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.instrument.lambda;

import com.navercorp.pinpoint.common.util.IOUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Woonduk Kang(emeroad)
 */
public class LambdaFactoryClassAdaptorTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    @Test
    public void transform() throws IOException {
        InputStream stream = ClassLoader.getSystemResourceAsStream("java/lang/invoke/InnerClassLambdaMetafactory.class");
        byte[] bytes = IOUtils.toByteArray(stream);

        LambdaFactoryClassAdaptor lambdaFactoryClassAdaptor = new LambdaFactoryClassAdaptor();
        byte[] transform = lambdaFactoryClassAdaptor.loadTransformedBytecode(bytes);

        ByteCodeDumper.verify(transform, ClassLoader.getSystemClassLoader());

    }


}