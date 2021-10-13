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
import com.navercorp.pinpoint.common.annotations.InterfaceStability;
import com.navercorp.pinpoint.common.util.ClassUtils;
import sun.net.www.protocol.jar.URLJarFile;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@InterfaceStability.Unstable
public class VersionMatcherOperand extends AbstractMatcherOperand {
    private final VersionRange versionRange;
    private final List<Resolver> resolverList = new ArrayList<>();
    private final boolean forcedMatch;

    public VersionMatcherOperand(List<String> versionRangeList, List<String> resolverList) {
        this(versionRangeList, resolverList, Boolean.FALSE);
    }

    public VersionMatcherOperand(List<String> versionRangeList, List<String> resolverList, boolean forcedMatch) {
        Objects.requireNonNull(versionRangeList);
        Objects.requireNonNull(resolverList);

        this.versionRange = new VersionRange(versionRangeList);
        for (String resolver : resolverList) {
            if (resolver.startsWith(FileVersionResolver.PREFIX)) {
                this.resolverList.add(new FileVersionResolver());
            } else if (resolver.startsWith(MetainfResolver.PREFIX)) {
                final int startIndex = resolver.indexOf('=');
                if (startIndex != -1) {
                    final String trimmed = resolver.substring(startIndex + 1).trim();
                    if (!trimmed.isEmpty()) {
                        this.resolverList.add(new MetainfResolver(trimmed));
                    }
                }
            } else if (resolver.startsWith(ClassLoaderResolver.PREFIX)) {
                this.resolverList.add(new ClassLoaderResolver());
            }
        }
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
        String version = null;
        for (Resolver resolver : this.resolverList) {
            if (resolver instanceof FileVersionResolver) {
                version = ((FileVersionResolver) resolver).toVersion(codeSourceLocation);
            } else if (resolver instanceof MetainfResolver) {
                version = ((MetainfResolver) resolver).toVersion(codeSourceLocation);
            } else if (resolver instanceof ClassLoaderResolver) {
                version = ((ClassLoaderResolver) resolver).toVersion(classInternalName, classLoader);
            }

            if (version != null) {
                return version;
            }
        }

        return null;
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
        return "VersionMatcherOperand{" +
                "versionRange=" + versionRange +
                ", resolverList=" + resolverList +
                '}';
    }

    class VersionRange {
        private List<MavenVersionRange> versionRangeList = new ArrayList<>();

        public VersionRange(List<String> versionRangeList) {
            for (String versionRange : versionRangeList) {
                this.versionRangeList.add(new MavenVersionRange(versionRange));
            }
        }

        public boolean match(final String version) {
            for (MavenVersionRange range : versionRangeList) {
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
                    "versionRangeList=" + versionRangeList +
                    '}';
        }
    }

    interface Resolver {
    }

    class ClassLoaderResolver implements Resolver {
        static final String PREFIX = "classloader-package";

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

    class FileVersionResolver implements Resolver {
        static final String PREFIX = "file-version";

        public String toVersion(final URL codeSourceLocation) {
            if (codeSourceLocation != null) {
                final String locationStr = codeSourceLocation.toString();
                final int lastIndex = locationStr.lastIndexOf('-');
                if (lastIndex != -1 && lastIndex < locationStr.length()) {
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

    class MetainfResolver implements Resolver {
        static final String PREFIX = "metainf";

        private String fieldName;

        public MetainfResolver(final String fieldName) {
            this.fieldName = fieldName;
        }

        public String toVersion(final URL codeSourceLocation) {
            if (codeSourceLocation != null) {
                try {
                    URLJarFile jarFile = new URLJarFile(new File(codeSourceLocation.getFile()));
                    return jarFile.getManifest().getMainAttributes().getValue(fieldName);
                } catch (Exception ignored) {
                }
            }

            return null;
        }

        @Override
        public String toString() {
            return "MetainfResolver{" +
                    "fieldName='" + fieldName + '\'' +
                    '}';
        }
    }
}
