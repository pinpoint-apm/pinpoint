/*
 * Copyright 2016 NAVER Corp.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.plugin.spring.beans;

import com.navercorp.pinpoint.bootstrap.util.AntPathMatcher;
import com.navercorp.pinpoint.bootstrap.util.PathMatcher;
import com.navercorp.pinpoint.bootstrap.util.RegexPathMatcher;
import com.navercorp.pinpoint.common.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author jaehong.kim
 */
public class SpringBeansTarget {
    public static final String ANT_STYLE_PATTERN_PREFIX = "antstyle";
    public static final String REGEX_PATTERN_PREFIX = "regex";

    private SpringBeansTargetScope scope = SpringBeansTargetScope.COMPONENT_SCAN;
    private List<String> basePackages;
    private List<PathMatcher> namePatterns;
    private List<PathMatcher> classPatterns;
    private List<String> annotations;

    public boolean isValid() {
        if (CollectionUtils.hasLength(basePackages)) {
            return true;
        }

        if (CollectionUtils.hasLength(namePatterns)) {
            return true;
        }

        if (CollectionUtils.hasLength(classPatterns)) {
            return true;
        }

        if (CollectionUtils.hasLength(annotations)) {
            return true;
        }

        return false;
    }

    public void setScope(final String scope) {
        this.scope = SpringBeansTargetScope.get(scope);
    }

    public SpringBeansTargetScope getScope() {
        return scope;
    }

    public void setBasePackages(final String basePackages) {
        this.basePackages = split(basePackages);
    }

    public List<String> getBasePackages() {
        return basePackages;
    }

    public void setNamePatterns(final String namePatternRegex) {
        this.namePatterns = compilePattern(split(namePatternRegex), "/");
    }

    public List<PathMatcher> getNamePatterns() {
        return namePatterns;
    }

    public void setClassPatterns(final String classPatternRegex) {
        this.classPatterns = compilePattern(split(classPatternRegex), ".");
    }

    public List<PathMatcher> getClassPatterns() {
        return classPatterns;
    }

    public void setAnnotations(final String annotations) {
        this.annotations = split(annotations);
    }

    public List<String> getAnnotations() {
        return annotations;
    }

    List<String> split(final String values) {
        if (values == null) {
            return Collections.emptyList();
        }

        final String[] tokens = values.split(",");
        final List<String> result = new ArrayList<String>(tokens.length);

        for (String token : tokens) {
            final String trimmed = token.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }

        return result;
    }

    List<PathMatcher> compilePattern(List<String> patternStrings, final String separator) {
        if (CollectionUtils.isEmpty(patternStrings)) {
            return null;
        }

        final List<PathMatcher> pathMatchers = new ArrayList<PathMatcher>(patternStrings.size());
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
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("scope=").append(scope);
        sb.append(", basePackages=").append(basePackages);
        sb.append(", namePatterns=").append(namePatterns);
        sb.append(", classPatterns=").append(classPatterns);
        sb.append(", annotations=").append(annotations);
        sb.append('}');
        return sb.toString();
    }
}