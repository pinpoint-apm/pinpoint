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

import com.navercorp.pinpoint.common.util.CodeSourceUtils;

import java.net.URL;
import java.security.ProtectionDomain;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ClassScannerFactory {

    private static final String FORCE_CLASS_LOADER_SCANNER_PROPERTY_KEY = "pinpoint.force.classloader.scanner";
    private static final boolean FORCE_CLASS_LOADER_SCANNER = forceClassLoaderScanner();

    public static final String JAR_URL_PREFIX = "jar:";
    public static final String FILE_URL_PREFIX = "file:";
    public static final String JAR_URL_SEPARATOR = "!/";

    // jboss vfs support
    private static final String[] FILE_PROTOCOLS = {"file", "vfs", "jar"};
    private static final String[] JAR_EXTENSIONS = {".jar", ".war", ".ear"};

    public static Scanner newScanner(ProtectionDomain protectionDomain, ClassLoader classLoader) {
        final URL codeLocation = CodeSourceUtils.getCodeLocation(protectionDomain);
        if (codeLocation == null) {
            return new ClassLoaderScanner(classLoader);
        }

        final Scanner scanner = newURLScanner(codeLocation);
        if (scanner != null) {
            return scanner;
        }

        // workaround for scanning for classes in nested jars - see newURLScanner(URL) below.
        if (FORCE_CLASS_LOADER_SCANNER || isNestedJar(codeLocation.getPath())) {
            ClassLoader protectionDomainClassLoader = protectionDomain.getClassLoader();
            if (protectionDomainClassLoader != null) {
                return new ClassLoaderScanner(protectionDomainClassLoader);
            }
        }

        throw new IllegalArgumentException("unknown scanner type classLoader:" + classLoader + " protectionDomain:" + protectionDomain);
    }

    public static Scanner newScanner(ProtectionDomain protectionDomain) {
        final URL codeLocation = CodeSourceUtils.getCodeLocation(protectionDomain);
        if (codeLocation == null) {
            return null;
        }

        final Scanner scanner = newURLScanner(codeLocation);
        if (scanner != null) {
            return scanner;
        }
        return null;
    }

    private static Scanner newURLScanner(URL codeLocation) {
        final String protocol = codeLocation.getProtocol();
        if (isFileProtocol(protocol)) {
            final String path = cleanupPath(codeLocation.getPath());
            final boolean isJarFile = isJarExtension(path);
            if (isJarFile) {
                return new JarFileScanner(path);
            }
            final boolean isDirectory = path.endsWith("/");
            if (isDirectory) {
                return new DirectoryScanner(path);
            }
        }
        // TODO consider a scanner for nested jars
        // Though the workaround above should work for current use cases, adding a scanner for nested jars would
        // be the "correct" way of handling Spring Boot or One-jar executable jars. However, there doesn't seem
        // to be a way to efficiently handle them.
        // Spring Boot loader's JarFile and JarFileEntries implementations look like a great reference for this.
        return null;
    }

    private static String cleanupPath(String path) {
        final int index = path.indexOf(JAR_URL_SEPARATOR);
        if (index == -1) {
            return path;
        }
        if (path.startsWith(FILE_URL_PREFIX)) {
            return path.substring(FILE_URL_PREFIX.length(), index);
        }
        return path.substring(0, index);
    }

    static boolean isJarExtension(String path) {
        if (path == null) {
            return false;
        }
        for (String extension : JAR_EXTENSIONS) {
            if (path.endsWith(extension)) {
                return true;
            }
        }
        return false;
    }

    static boolean isFileProtocol(String protocol) {
        for (String fileProtocol : FILE_PROTOCOLS) {
            if (fileProtocol.equals(protocol)) {
                return true;
            }
        }
        return false;
    }

    static boolean isNestedJar(String path) {
        if (path == null) {
            return false;
        }
        final String separator = "!/";
        if (!path.endsWith(separator)) {
            return false;
        }
        String subPath = path.substring(0, path.lastIndexOf(separator));
        return subPath.contains(separator);
    }

    private static boolean forceClassLoaderScanner() {
        String forceClassLoaderScanner = System.getProperty(FORCE_CLASS_LOADER_SCANNER_PROPERTY_KEY);
        return Boolean.parseBoolean(forceClassLoaderScanner);
    }
}
