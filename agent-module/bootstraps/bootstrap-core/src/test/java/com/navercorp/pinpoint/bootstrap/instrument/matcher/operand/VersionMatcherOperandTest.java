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
import org.apache.logging.log4j.core.util.Assert;
import org.junit.jupiter.api.Test;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VersionMatcherOperandTest {

    @Test
    public void matchFileVersion() throws Exception {
        final String className = "undefine";
        final ClassLoader classLoader = getClass().getClassLoader();
        final URL codeSourceLoaction = new URL("file::/local/pinpoint/plugins/pinpoint-activemq-client-plugin-1.0.0.jar");

        VersionMatcherOperand operand = new VersionMatcherOperand("[1.0,2.0],[3.2.1]");
        assertTrue(operand.match(classLoader, className, codeSourceLoaction));
    }

    @Test
    public void matchFileVersionNotFound() throws Exception {
        final String className = "undefine";
        final ClassLoader classLoader = getClass().getClassLoader();
        final URL codeSourceLoaction = new URL("file::/local/pinpoint/plugins/pinpoint-activemq-client-plugin.jar");

        VersionMatcherOperand operand = new VersionMatcherOperand("[1.0,2.0], [3.2.1]");
        assertFalse(operand.match(classLoader, className, codeSourceLoaction));
    }

    @Test
    public void matchClassLoader() throws Exception {
        // org.junit.Assert
        final String classInternalName = ClassUtils.toInternalName(Assert.class.getName());
        final ClassLoader classLoader = getClass().getClassLoader();
        final URL codeSourceLoaction = new URL("file::/undefine");

        VersionMatcherOperand operand = new VersionMatcherOperand("[1.0,3.0],[4.1,4.max]");
        assertTrue(operand.match(classLoader, classInternalName, codeSourceLoaction));
    }

    @Test
    public void matchClassLoaderNotFound() throws Exception {
        // org.junit.Assert
        final String classInternalName = "notfound/Assert";
        final ClassLoader classLoader = getClass().getClassLoader();
        final URL codeSourceLoaction = new URL("file::/undefine");

        VersionMatcherOperand operand = new VersionMatcherOperand("[1.0,3.0],[4.1,4.max]");
        assertFalse(operand.match(classLoader, classInternalName, codeSourceLoaction));
    }

    @Test
    public void matchMetainf() throws Exception {
        // org.junit.Assert
        final String className = Assert.class.getName();
        final ClassLoader classLoader = getClass().getClassLoader();
        final URL codeSourceLoaction = Assert.class.getProtectionDomain().getCodeSource().getLocation();

        VersionMatcherOperand.ManifestResolver manifestResolver = new VersionMatcherOperand.ManifestResolver();
        String version = manifestResolver.toVersion(codeSourceLoaction);
        assertNotNull(version);
    }

    @Test
    public void matchMetainfInvalidField() throws Exception {
        // org.junit.Assert
        final String className = String.class.getName();
        final ClassLoader classLoader = getClass().getClassLoader();

        VersionMatcherOperand operand = new VersionMatcherOperand("[1.0,1.5]");
        assertFalse(operand.match(classLoader, className, null));
    }

    @Test
    public void forcedMatchMetainf() throws Exception {
        // org.junit.Assert
        final String className = Assert.class.getName();
        final ClassLoader classLoader = getClass().getClassLoader();
        final URL codeSourceLoaction = Assert.class.getProtectionDomain().getCodeSource().getLocation();

        VersionMatcherOperand operand = new VersionMatcherOperand("[1.0,3.0],[4.1,4.max]", Boolean.TRUE);
        assertTrue(operand.match(classLoader, className, codeSourceLoaction));
    }
}