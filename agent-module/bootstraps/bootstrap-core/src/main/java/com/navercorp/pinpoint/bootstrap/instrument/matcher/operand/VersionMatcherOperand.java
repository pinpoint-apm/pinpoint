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

import com.navercorp.pinpoint.bootstrap.util.apache.MavenVersion;
import com.navercorp.pinpoint.bootstrap.util.apache.MavenVersionRange;
import com.navercorp.pinpoint.bootstrap.util.apache.MavenVersionSchemeSupport;
import com.navercorp.pinpoint.common.util.ClassUtils;
import sun.net.www.protocol.jar.URLJarFile;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.jar.Manifest;

public class VersionMatcherOperand extends AbstractMatcherOperand {
    private static final ClassLoaderResolver CLASS_LOADER_RESOLVER = new ClassLoaderResolver();
    private static final ManifestResolver MANIFEST_RESOLVER = new ManifestResolver();
    private static final FileVersionResolver FILE_VERSION_RESOLVER = new FileVersionResolver();

    private final VersionRange versionRange;
    private final boolean forcedMatch;

    public VersionMatcherOperand(String range) {
        this(range, Boolean.FALSE);
    }

    public VersionMatcherOperand(String range, boolean forcedMatch) {
        Objects.requireNonNull(range);

        this.versionRange = new VersionRange(range);
        this.forcedMatch = forcedMatch;
    }

    public boolean match(final ClassLoader classLoader, final String classInternalName, final URL codeSourceLocation) {
        if (Boolean.TRUE == forcedMatch) {
            return true;
        }

        final String version = resolveToVersion(classLoader, classInternalName, codeSourceLocation);
        if (version != null) {
            return versionRange.match(version);
        }
        return false;
    }

    String resolveToVersion(final ClassLoader classLoader, final String classInternalName, final URL codeSourceLocation) {
        String version = CLASS_LOADER_RESOLVER.toVersion(classInternalName, classLoader);
        if (version == null) {
            version = MANIFEST_RESOLVER.toVersion(codeSourceLocation);
        }
        if (version == null) {
            version = FILE_VERSION_RESOLVER.toVersion(codeSourceLocation);
        }
        return version;
    }

    @Override
    public int getExecutionCost() {
        return 3;
    }

    @Override
    public boolean isIndex() {
        return false;
    }

    @Override
    public String toString() {
        return "{" +
                "versionRange=" + versionRange +
                ", forcedMatch=" + forcedMatch +
                '}';
    }

    static class VersionRange {
        private final List<MavenVersionRange> list;

        public VersionRange(String range) {
            MavenVersionSchemeSupport support = new MavenVersionSchemeSupport();
            list = support.parseVersionConstraint(range);
        }

        public boolean match(final String version) {
            for (MavenVersionRange range : list) {
                final MavenVersion mavenVersion = new MavenVersion(version);
                if (range.containsVersion(mavenVersion)) {
                    return true;
                }
            }

            return false;
        }

        @Override
        public String toString() {
            return "{" +
                    "versionRangeList=" + list +
                    '}';
        }
    }

    interface Resolver {
    }

    static class ClassLoaderResolver implements Resolver {

        public String toVersion(final String classInternalName, final ClassLoader classLoader) {
            final String className = ClassUtils.toName(classInternalName);
            final String packageName = ClassUtils.getPackageName(className, '.', className);
            final GetVersionClassLoader getVersionClassLoader = new GetVersionClassLoader((classLoader));
            return getVersionClassLoader.getVersion(packageName);
        }
    }

    static class GetVersionClassLoader extends ClassLoader {

        public GetVersionClassLoader(ClassLoader classLoader) {
            super(classLoader);
        }

        public String getVersion(String name) {
            final Package p = getPackage(name);
            if (p != null) {
                return p.getImplementationVersion();
            }
            return null;
        }
    }

    static class FileVersionResolver implements Resolver {

        public String toVersion(final URL codeSourceLocation) {
            if (codeSourceLocation != null) {
                final String locationStr = codeSourceLocation.toString();
                final int lastIndex = locationStr.lastIndexOf('-');
                if (lastIndex != -1) {
                    final String version = locationStr.substring(lastIndex + 1);
                    if (version.endsWith(".jar")) {
                        return version.substring(0, version.length() - 4);
                    }
                    return version;
                }
            }

            return null;
        }
    }

    static class ManifestResolver implements Resolver {
        static final String FIELD = "Implementation-Version";

        public String toVersion(final URL codeSourceLocation) {
            if (codeSourceLocation != null) {
                try {
                    String fileName = codeSourceLocation.getFile();
                    if (fileName != null) {
                        Manifest manifest;
                        try (URLJarFile jarFile = new URLJarFile(new File(fileName))) {
                            manifest = jarFile.getManifest();
                            if (manifest != null) {
                                return manifest.getMainAttributes().getValue(FIELD);
                            }
                        }
                    }
                } catch (Exception ignored) {
                }
            }

            return null;
        }
    }
}