/*
 * Copyright 2022 NAVER Corp.
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
package com.navercorp.pinpoint.bootstrap.java9.module.merger;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.module.ModuleFinder;
import java.net.URI;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

/**
 * @author youngjin.kim2
 */
public class JarMerger {

    private final Set<String> entries = new HashSet<>();
    private final Map<String, String> providesMap = new HashMap<>();

    private final JarOutputStream jarStream;
    private final ByteArrayOutputStream byteStream;
    private final String moduleName;

    private JarMerger(String moduleName, List<URI> uris) throws IOException {
        this.moduleName = moduleName;
        this.byteStream = new ByteArrayOutputStream();
        this.jarStream = new JarOutputStream(byteStream);

        entries.add("META-INF/MANIFEST.MF");
        entries.add("module-info.class");

        for (final URI uri: uris) {
            add(uri);
        }

        addManifest();
        addProvides();
        this.jarStream.close();
    }

    private void add(URI uri) throws IOException {
        if (!uri.toString().endsWith(".jar")) {
            return;
        }

        final File file = new File(uri);
        try (final JarFile jarFile = new JarFile(file)) {
            final Enumeration<JarEntry> newEntries = jarFile.entries();
            while (newEntries.hasMoreElements()) {
                final JarEntry newEntry = newEntries.nextElement();
                final String name = newEntry.getName();

                if (name.startsWith("META-INF/services/")) {
                    final String provides = new String(jarFile.getInputStream(newEntry).readAllBytes());
                    provides(name, provides);
                }

                if (entries.contains(name)) {
                    continue;
                }
                entries.add(newEntry.getName());

                addEntry(name, jarFile.getInputStream(newEntry).readAllBytes());
            }
        }
    }

    private void provides(String name, String provides) {
        providesMap.put(name, providesMap.getOrDefault(name, "") + provides);
    }

    public static ModuleFinder build(String moduleName, List<URI> uris) throws IOException {
        return (new JarMerger(moduleName, uris)).getModuleFinder();
    }

    private ModuleFinder getModuleFinder() {
        return ModuleFinder.of(SingleFileSystem.byteArrayToPath("combined.jar", byteStream.toByteArray()));
    }

    private void addManifest() throws IOException {
        addEntry("META-INF/MANIFEST.MF", getManifestContent().getBytes());
    }

    private void addProvides() throws IOException {
        for (Map.Entry<String, String> e: providesMap.entrySet()) {
            addEntry("META-INF/services/" + e.getKey(), e.getValue().getBytes());
        }
    }

    private void addEntry(String name, byte[] bytes) throws IOException {
        jarStream.putNextEntry(new JarEntry(name));
        jarStream.write(bytes);
        jarStream.closeEntry();
    }

    private String getManifestContent() {
        return "Manifest-Version: 1.0\r\nAutomatic-Module-Name: " + moduleName + "\r\n\r\n";
    }

}
