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
import com.navercorp.pinpoint.plugin.spring.beans.SpringBeansTarget;

/**
 * @author Jongho Moon <jongho.moon@navercorp.com>
 * @author jaehong.kim
 */
public class TargetBeanFilter {
    private final List<SpringBeansTarget> targets;

    private final Cache transformed = new Cache();
    private final Cache rejected = new Cache();

    public static TargetBeanFilter of(ProfilerConfig profilerConfig) {
        SpringBeansConfig config = new SpringBeansConfig(profilerConfig);
        return new TargetBeanFilter(config.getTargets());
    }

    private TargetBeanFilter(List<SpringBeansTarget> targets) {
        this.targets = targets;
    }

    public boolean isTarget(String beanName, Class<?> clazz) {
        if (transformed.contains(clazz)) {
            return false;
        }

        for (SpringBeansTarget target : targets) {
            boolean find = false;
            if (target.getNamePattern() != null) {
                if (isBeanNameTarget(target, beanName)) {
                    find = true;
                } else {
                    continue;
                }
            }

            if (rejected.contains(clazz)) {
                continue;
            }

            if (target.getClassPattern() != null) {
                if (isClassNameTarget(target, clazz)) {
                    find = true;
                } else {
                    continue;
                }
            }

            if (target.getAnnotation() != null && !target.getAnnotation().isEmpty()) {
                if (isAnnotationTarget(target, clazz)) {
                    find = true;
                } else {
                    continue;
                }
            }

            if(find) {
                return true;
            }
        }

        if (!rejected.contains(clazz)) {
            this.rejected.put(clazz);
        }

        return false;
    }

    private boolean isBeanNameTarget(final SpringBeansTarget target, final String beanName) {
        return target.getNamePattern().matcher(beanName).matches();
    }

    private boolean isClassNameTarget(final SpringBeansTarget target, final Class<?> clazz) {
        return target.getClassPattern().matcher(clazz.getName()).matches();
    }

    private boolean isAnnotationTarget(final SpringBeansTarget target, final Class<?> clazz) {
        for (Annotation a : clazz.getAnnotations()) {
            if (target.getAnnotation().equals(a.annotationType().getName())) {
                return true;
            }
        }

        for (Annotation a : clazz.getAnnotations()) {
            for (Annotation ac : a.annotationType().getAnnotations()) {
                if (target.getAnnotation().equals(ac.annotationType().getName())) {
                    return true;
                }
            }
        }

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