/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.spring.beans.interceptor;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.plugin.spring.beans.SpringBeansConfig;

/**
 * @author Jongho Moon <jongho.moon@navercorp.com>
 * @author jaehong.kim
 */
public class TargetBeanFilter {
    private final List<Pattern> targetNamePatterns;
    private final List<Pattern> targetClassPatterns;
    private final Set<String> targetAnnotationNames;
    private final boolean targetInterception;

    private final Cache transformed = new Cache();
    private final Cache rejectedAnnotationName = new Cache();
    private final Cache rejectedClassName = new Cache();

    public static TargetBeanFilter of(ProfilerConfig profilerConfig) {
        SpringBeansConfig config = new SpringBeansConfig(profilerConfig);

        List<String> targetNamePatternStrings = split(config.getSpringBeansNamePatterns());
        List<Pattern> beanNamePatterns = compilePattern(targetNamePatternStrings);

        List<String> targetClassPatternStrings = split(config.getSpringBeansClassPatterns());
        List<Pattern> beanClassPatterns = compilePattern(targetClassPatternStrings);

        List<String> targetAnnotationNames = split(config.getSpringBeansAnnotations());

        return new TargetBeanFilter(beanNamePatterns, beanClassPatterns, targetAnnotationNames, config.isSpringBeansIntersection());
    }

    private static List<Pattern> compilePattern(List<String> patternStrings) {
        if (patternStrings == null || patternStrings.isEmpty()) {
            return null;
        }
        List<Pattern> beanNamePatterns = new ArrayList<Pattern>(patternStrings.size());
        for (String patternString : patternStrings) {
            Pattern pattern = Pattern.compile(patternString);
            beanNamePatterns.add(pattern);
        }
        return beanNamePatterns;
    }

    private TargetBeanFilter(List<Pattern> targetNamePatterns, List<Pattern> targetClassPatterns, List<String> targetAnnotationNames, boolean targetInterception) {
        this.targetNamePatterns = targetNamePatterns;
        this.targetClassPatterns = targetClassPatterns;
        this.targetAnnotationNames = targetAnnotationNames == null ? null : new HashSet<String>(targetAnnotationNames);
        this.targetInterception = targetInterception;
    }

    public boolean isTarget(String beanName, Class<?> clazz) {
        if (transformed.contains(clazz)) {
            return false;
        }

        boolean target = false;
        if (targetInterception) {
            // interception.
            if(hasConfig(targetNamePatterns)) {
                if(isBeanNameTarget(targetNamePatterns, beanName)) {
                    target = true;
                } else {
                    return false;
                }
            }

            if(hasConfig(targetAnnotationNames)) {
                if(isAnnotationNameTarget(targetAnnotationNames, clazz)) {
                    target = true;
                } else {
                    return false;
                }
            }

            if(hasConfig(targetClassPatterns)) {
                if(isClassNameTarget(targetClassPatterns, clazz)) {
                    target = true;
                } else {
                    return false;
                }
            }


            return target;
        }

        // union.
        if(hasConfig(targetNamePatterns)) {
            if(isBeanNameTarget(targetNamePatterns, beanName)) {
                return true;
            }
        }

        if(hasConfig(targetAnnotationNames)) {
            if(isAnnotationNameTarget(targetAnnotationNames, clazz)) {
                return true;
            }
        }

        if(hasConfig(targetClassPatterns)) {
            if(isClassNameTarget(targetClassPatterns, clazz)) {
                return true;
            }
        }

        // not found target.
        return false;
    }

    private boolean hasConfig(Collection collection) {
        if (collection == null || collection.isEmpty()) {
            return false;
        }

        return true;
    }


    private boolean isBeanNameTarget(final List<Pattern> targetNamePatterns, String beanName) {
        for (Pattern pattern : targetNamePatterns) {
            if (pattern.matcher(beanName).matches()) {
                return true;
            }
        }

        return false;
    }

    private boolean isAnnotationNameTarget(final Set<String> targetAnnotationNames, Class<?> clazz) {
        if (rejectedAnnotationName.contains(clazz)) {
            return false;
        }

        for (Annotation a : clazz.getAnnotations()) {
            if (targetAnnotationNames.contains(a.annotationType().getName())) {
                return true;
            }
        }

        for (Annotation a : clazz.getAnnotations()) {
            for (Annotation ac : a.annotationType().getAnnotations()) {
                if (targetAnnotationNames.contains(ac.annotationType().getName())) {
                    return true;
                }
            }
        }

        rejectedAnnotationName.put(clazz);
        return false;
    }

    private boolean isClassNameTarget(final List<Pattern> targetClassPatterns, Class<?> clazz) {
        if (rejectedClassName.contains(clazz)) {
            return false;
        }

        final String className = clazz.getName();
        for (Pattern pattern : targetClassPatterns) {
            if (pattern.matcher(className).matches()) {
                return true;
            }
        }

        rejectedClassName.put(clazz);
        return false;
    }


    public void addTransformed(Class<?> clazz) {
        transformed.put(clazz);
    }

    private static List<String> split(String values) {
        if (values == null) {
            return Collections.emptyList();
        }

        String[] tokens = values.split(",");
        List<String> result = new ArrayList<String>(tokens.length);

        for (String token : tokens) {
            String trimmed = token.trim();

            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }

        return result;
    }
}
