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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.security.ProtectionDomain;


/**
 * @author Woonduk Kang(emeroad)
 */
public class ClassScannerFactoryTest {

    private final Logger logger = LogManager.getLogger(this.getClass());

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
        Assertions.assertTrue(exist);
    }

    @Test
    public void hasClass_Jar() {
        Class<?> testClass = Logger.class;

        Scanner scanner = ClassScannerFactory.newScanner(testClass.getProtectionDomain(), this.getClass().getClassLoader());
        String fileName = JavaAssistUtils.javaClassNameToJvmResourceName(testClass.getName());
        boolean exist = scanner.exist(fileName);
        scanner.close();
        Assertions.assertTrue(exist);
    }

    @Test
    public void hasClass_classLoader() {
        Class<?> testClass = String.class;

        Scanner scanner = ClassScannerFactory.newScanner(testClass.getProtectionDomain(), this.getClass().getClassLoader());
        String fileName = JavaAssistUtils.javaClassNameToJvmResourceName(testClass.getName());
        boolean exist = scanner.exist(fileName);
        scanner.close();
        Assertions.assertTrue(exist);
    }

    @Test
    public void hasClass_classLoader_notfound() {
        Class<?> testClass = String.class;

        Scanner scanner = ClassScannerFactory.newScanner(testClass.getProtectionDomain(), this.getClass().getClassLoader());
        String fileName = JavaAssistUtils.javaClassNameToJvmResourceName("test.Test");
        boolean exist = scanner.exist(fileName);
        scanner.close();
        Assertions.assertFalse(exist);
    }

    @Test
    public void isFileProtocol() {
        Assertions.assertTrue(ClassScannerFactory.isFileProtocol("file"));
        // for jboss vfs support
        Assertions.assertTrue(ClassScannerFactory.isFileProtocol("vfs"));

        Assertions.assertFalse(ClassScannerFactory.isFileProtocol("cd"));
    }

    @Test
    public void isJarExtension() {
        Assertions.assertTrue(ClassScannerFactory.isJarExtension(".jar"));
        Assertions.assertTrue(ClassScannerFactory.isJarExtension(".war"));
        Assertions.assertTrue(ClassScannerFactory.isJarExtension(".ear"));

        Assertions.assertFalse(ClassScannerFactory.isJarExtension(".zip"));
    }

    @Test
    public void isNestedJar() {
        Assertions.assertTrue(ClassScannerFactory.isNestedJar("file:/path/to/some.jar!/nested/another.jar!/"));

        Assertions.assertFalse(ClassScannerFactory.isNestedJar(null));
        Assertions.assertFalse(ClassScannerFactory.isNestedJar(""));
        Assertions.assertFalse(ClassScannerFactory.isNestedJar("/path/to/some.jar"));
        Assertions.assertFalse(ClassScannerFactory.isNestedJar("/path/to/some.jar!/"));
        Assertions.assertFalse(ClassScannerFactory.isNestedJar("file:/path/to/some.jar"));
        Assertions.assertFalse(ClassScannerFactory.isNestedJar("file:/path/to/some.jar!/"));
    }

    /*
     * https://github.com/naver/pinpoint/issues/6670
     */
    @Test
    public void jar_file_prefix_github_6670() throws IOException, ClassNotFoundException {
        Class<?> testClass = Logger.class;
//        jar:file:/jboss/jboss-eap-6.4/modules/com/oracle12/main/ojdbc7.jar!/
        CodeSource codeSource = testClass.getProtectionDomain().getCodeSource();
        URL classUrl = codeSource.getLocation();

        String jarURLSpec = ClassScannerFactory.JAR_URL_PREFIX + classUrl.toExternalForm() + ClassScannerFactory.JAR_URL_SEPARATOR;
        Assertions.assertTrue(jarURLSpec.startsWith(ClassScannerFactory.JAR_URL_PREFIX + ClassScannerFactory.FILE_URL_PREFIX));

        URL url = new URL(jarURLSpec);
        ProtectionDomain protectionDomain = newProtectionDomain(url);
        Scanner scanner = ClassScannerFactory.newScanner(protectionDomain, null);
        String fileName = JavaAssistUtils.javaClassNameToJvmResourceName(testClass.getName());
        boolean exist = scanner.exist(fileName);
        scanner.close();
        Assertions.assertTrue(exist);
    }

    private ProtectionDomain newProtectionDomain(URL url) {
        CodeSource codeSource = new CodeSource(url, (CodeSigner[]) null);
        return new ProtectionDomain(codeSource, null);
    }
}