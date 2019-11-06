/*
 * Copyright 2014 NAVER Corp.
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

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.util.StringUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Jongho Moon
 * @author jaehong.kim
 */
public class SpringBeansConfig {
    public static final String SPRING_BEANS_MARK_ERROR = "profiler.spring.beans.mark.error";
    public static final String SPRING_BEANS_ANNOTATION = "profiler.spring.beans.annotation";
    public static final String SPRING_BEANS_CLASS_PATTERN = "profiler.spring.beans.class.pattern";
    public static final String SPRING_BEANS_NAME_PATTERN = "profiler.spring.beans.name.pattern";

    public static final String SPRING_BEANS_PREFIX = "profiler.spring.beans.";
    public static final String SPRING_BEANS_SCOPE_POSTFIX = ".scope";
    public static final String SPRING_BEANS_BASE_PACKAGES_POSTFIX = ".base-packages";
    public static final String SPRING_BEANS_ANNOTATION_POSTFIX = ".annotation";
    public static final String SPRING_BEANS_CLASS_PATTERN_POSTFIX = ".class.pattern";
    public static final String SPRING_BEANS_NAME_PATTERN_POSTFIX = ".name.pattern";

    private static final String PATTERN_REGEX = SpringBeansConfig.SPRING_BEANS_PREFIX + "[0-9]+" + "(" + SpringBeansConfig.SPRING_BEANS_SCOPE_POSTFIX + "|" + SpringBeansConfig.SPRING_BEANS_BASE_PACKAGES_POSTFIX + "|" + SpringBeansConfig.SPRING_BEANS_NAME_PATTERN_POSTFIX + "|" + SpringBeansConfig.SPRING_BEANS_CLASS_PATTERN_POSTFIX + "|" + SpringBeansConfig.SPRING_BEANS_ANNOTATION_POSTFIX + ")";

    private final PLogger logger = PLoggerFactory.getLogger(getClass());
    private final Map<Integer, SpringBeansTarget> targets = new HashMap<Integer, SpringBeansTarget>();

    private final boolean markError;

    public SpringBeansConfig(ProfilerConfig config) {
        // backward compatibility
        final Map<Integer, SpringBeansTarget> result = addBackwardCompatibilityTarget(config);
        // read pattern
        result.putAll(addTarget(config));

        // remove invalid target.
        for (Map.Entry<Integer, SpringBeansTarget> entry : result.entrySet()) {
            if (entry.getValue().isValid()) {
                this.targets.put(entry.getKey(), entry.getValue());
            }
        }
        this.markError = getMarkError(config);
    }

    public static boolean getMarkError(ProfilerConfig config) {
        return config.readBoolean(SPRING_BEANS_MARK_ERROR, false);
    }

    private Map<Integer, SpringBeansTarget> addBackwardCompatibilityTarget(ProfilerConfig config) {
        final Map<Integer, SpringBeansTarget> result = new HashMap<Integer, SpringBeansTarget>();
        final String namePatternRegexs = config.readString(SPRING_BEANS_NAME_PATTERN, null);
        // bean name.
        if (StringUtils.hasLength(namePatternRegexs)) {
            final SpringBeansTarget target = new SpringBeansTarget();
            target.setNamePatterns(namePatternRegexs);
            result.put(-1, target);
        }

        // class name.
        final String classPatternRegexs = config.readString(SPRING_BEANS_CLASS_PATTERN, null);
        if (StringUtils.hasLength(classPatternRegexs)) {
            final SpringBeansTarget target = new SpringBeansTarget();
            target.setClassPatterns(classPatternRegexs);
            result.put(-2, target);
        }

        // annotation.
        final String annotations = config.readString(SPRING_BEANS_ANNOTATION, null);
        if (StringUtils.hasLength(annotations)) {
            final SpringBeansTarget target = new SpringBeansTarget();
            target.setAnnotations(annotations);
            result.put(-3, target);
        }

        return result;
    }

    private Map<Integer, SpringBeansTarget> addTarget(ProfilerConfig config) {
        final Map<Integer, SpringBeansTarget> result = new HashMap<Integer, SpringBeansTarget>();
        final Map<String, String> patterns = config.readPattern(PATTERN_REGEX);

        for (Map.Entry<String, String> entry : patterns.entrySet()) {
            try {
                final String key = entry.getKey();
                if (key == null || !key.startsWith(SPRING_BEANS_PREFIX)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Invalid key format of spring-beans config {}={}", key, entry.getValue());
                    }
                    continue;
                }
                final int point = key.indexOf('.', SPRING_BEANS_PREFIX.length());
                if (point < 0) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Not found key number of spring-beans config {}={}", key, entry.getValue());
                    }
                    continue;
                }

                final int number = Integer.parseInt(key.substring(SPRING_BEANS_PREFIX.length(), point));
                SpringBeansTarget target = result.get(number);
                if (target == null) {
                    target = new SpringBeansTarget();
                    result.put(number, target);
                }

                if (key.endsWith(SPRING_BEANS_NAME_PATTERN_POSTFIX)) {
                    // bean name.
                    target.setNamePatterns(entry.getValue());
                } else if (key.endsWith(SPRING_BEANS_CLASS_PATTERN_POSTFIX)) {
                    // class name.
                    target.setClassPatterns(entry.getValue());
                } else if (key.endsWith(SPRING_BEANS_ANNOTATION_POSTFIX)) {
                    // annotation.
                    target.setAnnotations(entry.getValue());
                } else if (key.endsWith(SPRING_BEANS_SCOPE_POSTFIX)) {
                    // scope
                    target.setScope(entry.getValue());
                } else if (key.endsWith(SPRING_BEANS_BASE_PACKAGES_POSTFIX)) {
                    // base packages.
                    target.setBasePackages(entry.getValue());
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Unknown key format of spring-beans config. {}={}", key, entry.getValue());
                    }
                    continue;
                }
            } catch (Exception e) {
                if (logger.isInfoEnabled()) {
                    logger.info("Invalid spring-beans config. {}={}", entry.getKey(), entry.getValue());
                }
                continue;
            }
        }

        return result;
    }

    public SpringBeansTarget getTarget(int number) {
        return targets.get(number);
    }

    public Collection<SpringBeansTarget> getTargets() {
        return targets.values();
    }

    public boolean hasTarget(final SpringBeansTargetScope scope) {
        for (SpringBeansTarget target : this.targets.values()) {
            if (target.getScope() == scope) {
                return true;
            }
        }

        return false;
    }

    public boolean isMarkError() {
        return markError;
    }

    @Override
    public String toString() {
        return "SpringBeansConfig{" +
                "targets=" + targets +
                ", markError=" + markError +
                '}';
    }
}