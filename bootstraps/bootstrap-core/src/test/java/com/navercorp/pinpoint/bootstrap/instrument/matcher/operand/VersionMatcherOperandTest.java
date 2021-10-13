/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.instrument.matcher.operand;

import com.navercorp.pinpoint.common.util.ClassUtils;
import org.junit.Assert;
import org.junit.Test;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class VersionMatcherOperandTest {

    @Test
    public void matchFileVersion() throws Exception {
        final String className = "undefine";
        final ClassLoader classLoader = getClass().getClassLoader();
        final URL codeSourceLoaction = new URL("file::/local/pinpoint/plugins/pinpoint-activemq-client-plugin-1.0.0.jar");

        List<String> versionRangeList = Arrays.asList("[1.0,2.0]", "[3.2.1]");
        List<String> resolverList = Arrays.asList("file-version");

        VersionMatcherOperand operand = new VersionMatcherOperand(versionRangeList, resolverList);
        assertTrue(operand.match(classLoader, className, codeSourceLoaction));
    }

    @Test
    public void matchFileVersionNotFound() throws Exception {
        final String className = "undefine";
        final ClassLoader classLoader = getClass().getClassLoader();
        final URL codeSourceLoaction = new URL("file::/local/pinpoint/plugins/pinpoint-activemq-client-plugin.jar");

        List<String> versionRangeList = Arrays.asList("[1.0,2.0]", "[3.2.1]");
        List<String> resolverList = Arrays.asList("file-version");

        VersionMatcherOperand operand = new VersionMatcherOperand(versionRangeList, resolverList);
        assertFalse(operand.match(classLoader, className, codeSourceLoaction));
    }

    @Test
    public void matchClassLoader() throws Exception {
        // org.junit.Assert
        final String classInternalName = ClassUtils.toInternalName(Assert.class.getName());
        final ClassLoader classLoader = getClass().getClassLoader();
        final URL codeSourceLoaction = new URL("file::/undefine");

        List<String> versionRangeList = Arrays.asList("[1.0,3.0]", "[4.1,4.max]");
        List<String> resolverList = Arrays.asList("classloader-package");

        VersionMatcherOperand operand = new VersionMatcherOperand(versionRangeList, resolverList);
        assertTrue(operand.match(classLoader, classInternalName, codeSourceLoaction));
    }

    @Test
    public void matchClassLoaderNotFound() throws Exception {
        // org.junit.Assert
        final String classInternalName = "notfound/Assert";
        final ClassLoader classLoader = getClass().getClassLoader();
        final URL codeSourceLoaction = new URL("file::/undefine");

        List<String> versionRangeList = Arrays.asList("[1.0,3.0]", "[4.1,4.max]");
        List<String> resolverList = Arrays.asList("classloader-package");

        VersionMatcherOperand operand = new VersionMatcherOperand(versionRangeList, resolverList);
        assertFalse(operand.match(classLoader, classInternalName, codeSourceLoaction));
    }

    @Test
    public void matchMetainf() throws Exception {
        // org.junit.Assert
        final String className = Assert.class.getName();
        final ClassLoader classLoader = getClass().getClassLoader();
        final URL codeSourceLoaction = Assert.class.getProtectionDomain().getCodeSource().getLocation();

        List<String> versionRangeList = Arrays.asList("[1.0,3.0]", "[4.1,4.max]");
        List<String> resolverList = Arrays.asList("metainf=Implementation-Version");

        VersionMatcherOperand operand = new VersionMatcherOperand(versionRangeList, resolverList);
        assertTrue(operand.match(classLoader, className, codeSourceLoaction));
    }

    @Test
    public void matchMetainfInvalidField() throws Exception {
        // org.junit.Assert
        final String className = Assert.class.getName();
        final ClassLoader classLoader = getClass().getClassLoader();
        final URL codeSourceLoaction = Assert.class.getProtectionDomain().getCodeSource().getLocation();

        List<String> versionRangeList = Arrays.asList("[1.0,3.0]", "[4.1,4.max]");
        List<String> resolverList = Arrays.asList("metainf=Implementation-Title");

        VersionMatcherOperand operand = new VersionMatcherOperand(versionRangeList, resolverList);
        assertFalse(operand.match(classLoader, className, codeSourceLoaction));
    }

    @Test
    public void matchMetainfNotFoundField() throws Exception {
        // org.junit.Assert
        final String className = Assert.class.getName();
        final ClassLoader classLoader = getClass().getClassLoader();
        final URL codeSourceLoaction = Assert.class.getProtectionDomain().getCodeSource().getLocation();

        List<String> versionRangeList = Arrays.asList("[1.0,3.0]", "[4.1,4.max]");
        List<String> resolverList = Arrays.asList("metainf=Not-Found");

        VersionMatcherOperand operand = new VersionMatcherOperand(versionRangeList, resolverList);
        assertFalse(operand.match(classLoader, className, codeSourceLoaction));
    }

    @Test
    public void forcedMatchMetainf() throws Exception {
        // org.junit.Assert
        final String className = Assert.class.getName();
        final ClassLoader classLoader = getClass().getClassLoader();
        final URL codeSourceLoaction = Assert.class.getProtectionDomain().getCodeSource().getLocation();

        List<String> versionRangeList = Arrays.asList("[1.0,3.0]", "[4.1,4.max]");
        List<String> resolverList = Arrays.asList("metainf=Unknown");

        VersionMatcherOperand operand = new VersionMatcherOperand(versionRangeList, resolverList, Boolean.TRUE);
        assertTrue(operand.match(classLoader, className, codeSourceLoaction));
    }
}