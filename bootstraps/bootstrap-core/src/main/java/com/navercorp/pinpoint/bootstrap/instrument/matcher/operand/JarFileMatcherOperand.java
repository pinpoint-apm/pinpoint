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

import com.navercorp.pinpoint.bootstrap.util.AntPathMatcher;
import com.navercorp.pinpoint.bootstrap.util.PathMatcher;
import com.navercorp.pinpoint.bootstrap.util.RegexPathMatcher;
import com.navercorp.pinpoint.common.annotations.InterfaceStability;
import com.navercorp.pinpoint.common.util.CollectionUtils;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

@InterfaceStability.Unstable
public class JarFileMatcherOperand extends AbstractMatcherOperand {
    static final String ANT_STYLE_PATTERN_PREFIX = "antstyle";
    static final String REGEX_PATTERN_PREFIX = "regex";

    private final List<PathMatcher> namePatterns;
    private final boolean forcedMatch;

    public JarFileMatcherOperand(final List<String> namePatternRegex) {
        this(namePatternRegex, Boolean.FALSE);
    }

    public JarFileMatcherOperand(final List<String> namePatternRegex, final boolean forcedMatch) {
        Objects.requireNonNull(namePatternRegex);
        this.namePatterns = compilePattern(namePatternRegex, "/");
        this.forcedMatch = forcedMatch;
    }

    List<PathMatcher> compilePattern(List<String> patternStrings, final String separator) {
        final List<PathMatcher> pathMatchers = new ArrayList<>(patternStrings.size());
        for (String patternString : patternStrings) {
            final int prefixEnd = patternString.indexOf(":");
            if (prefixEnd != -1) {
                final String prefix = patternString.substring(0, prefixEnd).trim();
                if (prefix.equals(ANT_STYLE_PATTERN_PREFIX)) {
                    final String trimmed = patternString.substring(prefixEnd + 1).trim();
                    if (!trimmed.isEmpty()) {
                        pathMatchers.add(new AntPathMatcher(trimmed, separator));
                    }
                    continue;
                } else if (prefix.equals(REGEX_PATTERN_PREFIX)) {
                    final String trimmed = patternString.substring(prefixEnd + 1).trim();
                    if (!trimmed.isEmpty()) {
                        final Pattern pattern = Pattern.compile(trimmed);
                        pathMatchers.add(new RegexPathMatcher(pattern));
                    }
                    continue;
                }
            }

            final Pattern pattern = Pattern.compile(patternString);
            pathMatchers.add(new RegexPathMatcher(pattern));
        }

        return pathMatchers;
    }

    @Override
    public int getExecutionCost() {
        return 3;
    }

    @Override
    public boolean isIndex() {
        return false;
    }

    public boolean match(final URL codeSourceLocation) {
        if (Boolean.TRUE == forcedMatch) {
            return true;
        }

        String fileName = toFileName(codeSourceLocation);
        if (fileName == null) {
            return false;
        }

        final int extensionPoint = fileName.lastIndexOf(".jar");
        if (extensionPoint != -1 && extensionPoint > 0) {
            fileName = fileName.substring(0, extensionPoint);
        }

        if (CollectionUtils.hasLength(namePatterns)) {
            for (PathMatcher pathMatcher : this.namePatterns) {
                if (pathMatcher.isMatched(fileName)) {
                    return true;
                }
            }
        }
        return false;
    }

    String toFileName(final URL codeSourceLocation) {
        try {
            if (codeSourceLocation != null) {
                final String locationStr = codeSourceLocation.toString();
                // now lets remove all but the file name
                String result = parseLocation(locationStr, '/');
                if (result != null) {
                    return result;
                }
                return parseLocation(locationStr, '\\');
            }
        } catch (Exception ignored) {
        }

        return null;
    }

    private String parseLocation(String locationStr, char separator) {
        int idx = locationStr.lastIndexOf(separator);
        if (isFolder(idx, locationStr)) {
            idx = locationStr.lastIndexOf(separator, idx - 1);
            return locationStr.substring(idx + 1);
        } else if (idx > 0) {
            return locationStr.substring(idx + 1);
        }
        return null;
    }

    private boolean isFolder(int idx, String text) {
        return (idx != -1 && idx + 1 == text.length());
    }

    @Override
    public String toString() {
        return "JarFileMatcherOperand{" +
                "namePatterns=" + namePatterns +
                '}';
    }
}
