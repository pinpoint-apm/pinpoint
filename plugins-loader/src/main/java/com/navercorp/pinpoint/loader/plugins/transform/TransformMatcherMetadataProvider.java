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

package com.navercorp.pinpoint.loader.plugins.transform;

import com.navercorp.pinpoint.bootstrap.instrument.matcher.TransformMatcherMetadata;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Objects;

public class TransformMatcherMetadataProvider {
    private static final String TRANSFORM_MATCHER_DEF_ENTRY = "META-INF/pinpoint/transform-matcher.yml";
    private static final Logger logger = LoggerFactory.getLogger(TransformMatcherMetadataProvider.class);

    private final String metaFilePath;

    public TransformMatcherMetadataProvider() {
        this(TRANSFORM_MATCHER_DEF_ENTRY);
    }

    public TransformMatcherMetadataProvider(String metaFilePath) {
        this.metaFilePath = metaFilePath;
    }

    public TransformMatcherMetadata getTransformMatcherMetadata(final ProfilerPlugin plugin) {
        Objects.requireNonNull(plugin);

        final TransformMatcherMetadata.Builder builder = new TransformMatcherMetadata.Builder();
        final ParsedVersionMatcherMetadata metadata = fromMetaFiles(plugin.getClass().getClassLoader());
        if (metadata != null) {
            if (metadata.getVersionMatchers() != null) {
                for (ParsedVersionMatcher versionMatcher : metadata.getVersionMatchers()) {
                    if (versionMatcher.validate()) {
                        builder.versionMatcher(versionMatcher.getName(), versionMatcher.getRanges(), versionMatcher.getResolvers(), versionMatcher.isForcedMatch());
                    }
                }
            }

            if (metadata.getJarFileMatchers() != null) {
                for (ParsedJarFileMatcher jarFileMatcher : metadata.getJarFileMatchers()) {
                    if (jarFileMatcher.validate()) {
                        builder.jarFileMatcher(jarFileMatcher.getName(), jarFileMatcher.getPatterns(), jarFileMatcher.isForcedMatch());
                    }
                }
            }
        }

        return builder.build();
    }

    private ParsedVersionMatcherMetadata fromMetaFiles(ClassLoader classLoader) {
        final URL url = getResourceUrl(classLoader);
        if (url != null) {
            ParsedVersionMatcherMetadata parsedTraceMetadataProvider = parse(url);
            return parsedTraceMetadataProvider;
        }
        return null;
    }

    private URL getResourceUrl(ClassLoader classLoader) {
        try {
            return classLoader.getResource(this.metaFilePath);
        } catch (Exception ignored) {
            logger.info("I/O error getting version matcher operand definitions. resource={}", this.metaFilePath, ignored);
        }
        return null;
    }

    public ParsedVersionMatcherMetadata parse(URL url) {
        InputStream inputStream = null;
        try {
            inputStream = url.openStream();
            Yaml yaml = new Yaml();
            ParsedVersionMatcherMetadata parsedTraceMetadata = yaml.loadAs(inputStream, ParsedVersionMatcherMetadata.class);
            return parsedTraceMetadata;
        } catch (IOException ignored) {
            if (logger.isInfoEnabled()) {
                logger.info("Failed to open file={}", url.toString());
            } else if (logger.isDebugEnabled()) {
                logger.info("Failed to open file={}", url.toString(), ignored);
            }
        } catch (YAMLException ignored) {
            if (logger.isInfoEnabled()) {
                logger.info("Failed to load yml file={}", url.toString());
            } else if (logger.isDebugEnabled()) {
                logger.info("Failed to load yml file={}", url.toString(), ignored);
            }
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ignored) {
                    logger.debug("Failed to close file={}", url.toString(), ignored);
                }
            }
        }
        return null;
    }

    public static class ParsedVersionMatcherMetadata {
        private List<ParsedVersionMatcher> versionMatchers;
        private List<ParsedJarFileMatcher> jarFileMatchers;

        public List<ParsedVersionMatcher> getVersionMatchers() {
            return versionMatchers;
        }

        public void setVersionMatchers(List<ParsedVersionMatcher> versionMatchers) {
            this.versionMatchers = versionMatchers;
        }

        public List<ParsedJarFileMatcher> getJarFileMatchers() {
            return jarFileMatchers;
        }

        public void setJarFileMatchers(List<ParsedJarFileMatcher> jarFileMatchers) {
            this.jarFileMatchers = jarFileMatchers;
        }
    }

    public static class ParsedVersionMatcher {
        private String name;
        private List<String> ranges;
        private List<String> resolvers;
        private boolean forcedMatch;
        private boolean enable;

        public String getName() {
            return name;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public List<String> getRanges() {
            return ranges;
        }

        public void setRanges(List<String> ranges) {
            this.ranges = ranges;
        }

        public List<String> getResolvers() {
            return resolvers;
        }

        public void setResolvers(List<String> resolvers) {
            this.resolvers = resolvers;
        }

        public boolean isForcedMatch() {
            return forcedMatch;
        }

        public void setForcedMatch(boolean forcedMatch) {
            this.forcedMatch = forcedMatch;
        }

        public boolean isEnable() {
            return enable;
        }

        public void setEnable(boolean enable) {
            this.enable = enable;
        }

        public boolean validate() {
            if (name == null || name.isEmpty()) {
                logger.debug("not found version-matcher's name");
                return false;
            }
            if (ranges == null || ranges.isEmpty()) {
                logger.debug("not found version-matcher's ranges");
                return false;
            }
            if (resolvers == null || resolvers.isEmpty()) {
                logger.debug("not found version-matcher's resolvers");
                return false;
            }

            return enable;
        }
    }

    public static class ParsedJarFileMatcher {
        private String name;
        private List<String> patterns;
        private boolean forcedMatch;
        private boolean enable;

        public String getName() {
            return name;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public List<String> getPatterns() {
            return patterns;
        }

        public void setPatterns(List<String> patterns) {
            this.patterns = patterns;
        }

        public boolean isForcedMatch() {
            return forcedMatch;
        }

        public void setForcedMatch(boolean forcedMatch) {
            this.forcedMatch = forcedMatch;
        }

        public boolean isEnable() {
            return enable;
        }

        public void setEnable(boolean enable) {
            this.enable = enable;
        }

        public boolean validate() {
            if (name == null || name.isEmpty()) {
                logger.info("not found jarfile-matcher's name");
                return false;
            }

            if (patterns == null || patterns.isEmpty()) {
                logger.debug("not found jarfile-matcher's patterns");
                return false;
            }

            return enable;
        }
    }
}
