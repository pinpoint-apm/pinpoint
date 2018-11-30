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

package com.navercorp.pinpoint.profiler.instrument.transformer;

import org.junit.Assert;
import org.junit.Test;

import java.lang.instrument.ClassFileTransformer;

import static org.mockito.Mockito.mock;


/**
 * @author Woonduk Kang(emeroad)
 */
public class PredefinedTransformerRegistryTest {

    @Test
    public void addRegistry_not_found() {
        PredefinedTransformerRegistry predefinedTransformerRegistry = new PredefinedTransformerRegistry();

        ClassFileTransformer non = predefinedTransformerRegistry.findTransformer(null, "non", new byte[0]);
        Assert.assertNull(non);

    }

    @Test
    public void addRegistry() {
        PredefinedTransformerRegistry predefinedTransformerRegistry = new PredefinedTransformerRegistry();
        ClassFileTransformer classFileTransformer = mock(ClassFileTransformer.class);
        predefinedTransformerRegistry.addRegistry("a", classFileTransformer);
        ClassFileTransformer success = predefinedTransformerRegistry.findTransformer(null, "a", new byte[0]);
        Assert.assertSame(classFileTransformer , success);

        ClassFileTransformer non = predefinedTransformerRegistry.findTransformer(null, "a", new byte[0]);
        Assert.assertNull(non);
    }
}