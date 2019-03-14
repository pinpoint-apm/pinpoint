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

package com.navercorp.pinpoint.profiler.instrument.scanner;

import com.navercorp.pinpoint.profiler.util.JavaAssistUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Woonduk Kang(emeroad)
 */
public class ClassScannerFactoryTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void newScanner() {

    }

    @Test
    public void hasClass_directory() {

        Class<?> testClass = ClassScannerFactoryTest.class;
        Scanner scanner = ClassScannerFactory.newScanner(testClass.getProtectionDomain(), this.getClass().getClassLoader());
        String fileName = JavaAssistUtils.javaClassNameToJvmResourceName(testClass.getName());
        boolean exist = scanner.exist(fileName);
        scanner.close();
        Assert.assertTrue(exist);
    }

    @Test
    public void hasClass_Jar() {
        Class<?> testClass = Logger.class;

        Scanner scanner = ClassScannerFactory.newScanner(testClass.getProtectionDomain(), this.getClass().getClassLoader());
        String fileName = JavaAssistUtils.javaClassNameToJvmResourceName(testClass.getName());
        boolean exist = scanner.exist(fileName);
        scanner.close();
        Assert.assertTrue(exist);
    }

    @Test
    public void hasClass_classLoader() {
        Class<?> testClass = String.class;

        Scanner scanner = ClassScannerFactory.newScanner(testClass.getProtectionDomain(), this.getClass().getClassLoader());
        String fileName = JavaAssistUtils.javaClassNameToJvmResourceName(testClass.getName());
        boolean exist = scanner.exist(fileName);
        scanner.close();
        Assert.assertTrue(exist);
    }

    @Test
    public void hasClass_classLoader_notfound() {
        Class<?> testClass = String.class;

        Scanner scanner = ClassScannerFactory.newScanner(testClass.getProtectionDomain(), this.getClass().getClassLoader());
        String fileName = JavaAssistUtils.javaClassNameToJvmResourceName("test.Test");
        boolean exist = scanner.exist(fileName);
        scanner.close();
        Assert.assertFalse(exist);
    }

    @Test
    public void isFileProtocol() {
        Assert.assertTrue(ClassScannerFactory.isFileProtocol("file"));
        // for jboss vfs support
        Assert.assertTrue(ClassScannerFactory.isFileProtocol("vfs"));

        Assert.assertFalse(ClassScannerFactory.isFileProtocol("cd"));
    }

    @Test
    public void isJarExtension() {
        Assert.assertTrue(ClassScannerFactory.isJarExtension(".jar"));
        Assert.assertTrue(ClassScannerFactory.isJarExtension(".war"));
        Assert.assertTrue(ClassScannerFactory.isJarExtension(".ear"));

        Assert.assertFalse(ClassScannerFactory.isJarExtension(".zip"));
    }

    @Test
    public void isNestedJar() {
        Assert.assertTrue(ClassScannerFactory.isNestedJar("file:/path/to/some.jar!/nested/another.jar!/"));

        Assert.assertFalse(ClassScannerFactory.isNestedJar(null));
        Assert.assertFalse(ClassScannerFactory.isNestedJar(""));
        Assert.assertFalse(ClassScannerFactory.isNestedJar("/path/to/some.jar"));
        Assert.assertFalse(ClassScannerFactory.isNestedJar("/path/to/some.jar!/"));
        Assert.assertFalse(ClassScannerFactory.isNestedJar("file:/path/to/some.jar"));
        Assert.assertFalse(ClassScannerFactory.isNestedJar("file:/path/to/some.jar!/"));
    }
}