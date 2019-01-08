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
    // jboss vfs support
    private static final String[] FILE_PROTOCOLS = {"file", "vfs"};
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
            final String path = codeLocation.getPath();
            final boolean isJarFile = isJarExtension(path);
            if (isJarFile) {
                return new JarFileScanner(path);
            }
            final boolean isDirectory = path.endsWith("/");
            if (isDirectory) {
                return new DirectoryScanner(path);
            }
        }
        return null;
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


}
