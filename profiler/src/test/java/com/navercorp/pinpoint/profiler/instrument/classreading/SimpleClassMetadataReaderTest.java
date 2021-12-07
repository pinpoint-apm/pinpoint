/*
 * Copyright 2016 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.profiler.instrument.classreading;

import com.navercorp.pinpoint.common.util.ClassLoaderUtils;
import com.navercorp.pinpoint.profiler.util.BytecodeUtils;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.junit.Test;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;

/**
 * @author Woonduk Kang(emeroad)
 */
public class SimpleClassMetadataReaderTest {
    private final Logger logger = LogManager.getLogger(this.getClass());

    @Test
    public void testSimpleClassMetadata() {
        Class<?> clazz = String.class;
        byte[] classFile = BytecodeUtils.getClassFile(ClassLoaderUtils.getDefaultClassLoader(), clazz.getName());

        SimpleClassMetadata simpleClassMetadata = SimpleClassMetadataReader.readSimpleClassMetadata(classFile);
        // name.
        Assert.assertEquals(simpleClassMetadata.getClassName(), clazz.getName());

        // interfaces
        List<String> interfaceList = getInterfaceList(clazz.getInterfaces());
        List<String> interfaceNames = simpleClassMetadata.getInterfaceNames();
        MatcherAssert.assertThat(interfaceNames, containsInAnyOrder(interfaceList.toArray()));

        // super
        Assert.assertEquals(simpleClassMetadata.getSuperClassName(), "java.lang.Object");

        // access
        simpleClassMetadata.getAccessFlag();
        // version
        simpleClassMetadata.getVersion();
    }

    private List<String> getInterfaceList(Class<?>[] interfaces) {
        List<String> interfaceNames = new ArrayList<>();
        for (Class<?> interfaceClass : interfaces) {
            interfaceNames.add(interfaceClass.getName());
        }
        return interfaceNames;
    }
}