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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.spring.beans.SpringBeansConfig;

/**
 * 
 * @author Jongho Moon <jongho.moon@navercorp.com>
 *
 */
public class TargetBeanFilter {
    private final PLogger logger = PLoggerFactory.getLogger(getClass());

    private final List<Pattern> targetNamePatterns;
    private final List<Pattern> targetClassPatterns;
    private final List<String> targetAnnotationNames;
    private final ConcurrentMap<ClassLoader, List<Class<? extends Annotation>>> targetAnnotationMap = new ConcurrentHashMap<ClassLoader, List<Class<? extends Annotation>>>();

    private final Cache transformed = new Cache();
    private final Cache rejected = new Cache();

    public static TargetBeanFilter of(ProfilerConfig profilerConfig) {
        SpringBeansConfig config = new SpringBeansConfig(profilerConfig);
        
        List<String> targetNamePatternStrings = split(config.getSpringBeansNamePatterns());
        List<Pattern> beanNamePatterns = compilePattern(targetNamePatternStrings);

        List<String> targetClassPatternStrings = split(config.getSpringBeansClassPatterns());
        List<Pattern> beanClassPatterns = compilePattern(targetClassPatternStrings);

        List<String> targetAnnotationNames = split(config.getSpringBeansAnnotations());

        return new TargetBeanFilter(beanNamePatterns, beanClassPatterns, targetAnnotationNames);
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

    private TargetBeanFilter(List<Pattern> targetNamePatterns, List<Pattern> targetClassPatterns, List<String> targetAnnotationNames) {
        this.targetNamePatterns = targetNamePatterns;
        this.targetClassPatterns = targetClassPatterns;
        this.targetAnnotationNames = targetAnnotationNames;
    }

    public boolean isTarget(String beanName, Class<?> clazz) {
        if (transformed.contains(clazz)) {
            return false;
        }

        return isTarget(beanName) || isTarget(clazz);
    }

    private boolean isTarget(String beanName) {
        if (targetNamePatterns != null) {
            for (Pattern pattern : targetNamePatterns) {
                if (pattern.matcher(beanName).matches()) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isTarget(Class<?> clazz) {
        if (rejected.contains(clazz)) {
            return false;
        }

        if (targetAnnotationNames != null) {
            List<Class<? extends Annotation>> targetAnnotations = getTargetAnnotations(clazz.getClassLoader());

            for (Class<? extends Annotation> a : targetAnnotations) {
                if (clazz.isAnnotationPresent(a)) {
                    return true;
                }
            }

            for (Annotation a : clazz.getAnnotations()) {
                for (Class<? extends Annotation> ac : targetAnnotations) {
                    if (a.annotationType().isAnnotationPresent(ac)) {
                        return true;
                    }
                }
            }
        }

        if (targetClassPatterns != null) {
            String className = clazz.getName();

            for (Pattern pattern : targetClassPatterns) {
                if (pattern.matcher(className).matches()) {
                    return true;
                }
            }
        }

        rejected.put(clazz);
        return false;
    }

    public void addTransformed(Class<?> clazz) {
        transformed.put(clazz);
    }

    private List<Class<? extends Annotation>> getTargetAnnotations(ClassLoader classLoader) {
        ClassLoader nonNull = classLoader == null ? ClassLoader.getSystemClassLoader() : classLoader;
        
        List<Class<? extends Annotation>> targetAnnotations = targetAnnotationMap.get(nonNull);

        if (targetAnnotations == null) {
            targetAnnotations = loadTargetAnnotations(nonNull);
            targetAnnotationMap.put(nonNull, targetAnnotations);
        }

        return targetAnnotations;
    }

    private List<Class<? extends Annotation>> loadTargetAnnotations(ClassLoader loader) {
        if (targetAnnotationNames.isEmpty()) {
            return Collections.emptyList();
        }

        List<Class<? extends Annotation>> targetAnnotationClasses = new ArrayList<Class<? extends Annotation>>(targetAnnotationNames.size());
        for (String targetAnnotationName : targetAnnotationNames) {
            try {
                Class<?> clazz = loader.loadClass(targetAnnotationName);
                Class<? extends Annotation> ac = clazz.asSubclass(Annotation.class);
                targetAnnotationClasses.add(ac);
            } catch (ClassNotFoundException e) {
                logger.warn("Cannot find Spring beans profile target annotation class: {}. This configuration will be ignored.", targetAnnotationName, e);
            } catch (ClassCastException e) {
                logger.warn("Given Spring beans profile target annotation class is not subclass of Annotation: {}. This configuration will be ignored.", targetAnnotationName, e);
            }
        }

        return targetAnnotationClasses;
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
