/**
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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Jongho Moon
 * @author jaehong.kim
 */
public class SpringBeansConfig {
    public static final String SPRING_BEANS_ANNOTATION = "profiler.spring.beans.annotation";
    public static final String SPRING_BEANS_CLASS_PATTERN = "profiler.spring.beans.class.pattern";
    public static final String SPRING_BEANS_NAME_PATTERN = "profiler.spring.beans.name.pattern";

    public static final String SPRING_BEANS_PREFIX = "profiler.spring.beans.";
    public static final String SPRING_BEANS_ANNOTATION_POSTFIX = ".annotation";
    public static final String SPRING_BEANS_CLASS_PATTERN_POSTFIX = ".class.pattern";
    public static final String SPRING_BEANS_NAME_PATTERN_POSTFIX = ".name.pattern";

    private static final String PATTERN_REGEX = SpringBeansConfig.SPRING_BEANS_PREFIX + "[0-9]+" + "(" + SpringBeansConfig.SPRING_BEANS_NAME_PATTERN_POSTFIX + "|" + SpringBeansConfig.SPRING_BEANS_CLASS_PATTERN_POSTFIX + "|" + SpringBeansConfig.SPRING_BEANS_ANNOTATION_POSTFIX + ")";

    private final PLogger logger = PLoggerFactory.getLogger(getClass());
    private final Map<Integer, SpringBeansTarget> targets = new HashMap<Integer, SpringBeansTarget>();

    public SpringBeansConfig(ProfilerConfig config) {
        // backward compatibility
        addBackwardCompatibilityTarget(config);

        // read pattern
        addTarget(config);

        if (logger.isInfoEnabled()) {
            logger.info("Add spring-beans targets {}", targets);
        }
    }

    private void addBackwardCompatibilityTarget(ProfilerConfig config) {
        final String namePatternRegexs = config.readString(SPRING_BEANS_NAME_PATTERN, null);
        if (namePatternRegexs != null && !namePatternRegexs.isEmpty()) {
            final SpringBeansTarget target = new SpringBeansTarget();
            target.setNamePatterns(namePatternRegexs);
            if (target.isValid()) {
                targets.put(-1, target);
            }
        }

        final String classPatternRegexs = config.readString(SPRING_BEANS_CLASS_PATTERN, null);
        if(classPatternRegexs != null && !classPatternRegexs.isEmpty()) {
            final SpringBeansTarget target = new SpringBeansTarget();
            target.setClassPatterns(classPatternRegexs);
            if (target.isValid()) {
                targets.put(-2, target);
            }
        }

        final String annotations = config.readString(SPRING_BEANS_ANNOTATION, null);
        if(annotations != null && !annotations.isEmpty()) {
            final SpringBeansTarget target = new SpringBeansTarget();
            target.setAnnotations(annotations);
            if (target.isValid()) {
                targets.put(-3, target);
            }
        }
    }

    private void addTarget(ProfilerConfig config) {
        Map<String, String> result = config.readPattern(PATTERN_REGEX);
        for (Map.Entry<String, String> entry : result.entrySet()) {
            try {
                final String key = entry.getKey();
                if (key == null || !key.startsWith(SPRING_BEANS_PREFIX)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Invalid key format of spring-beans target {}", key);
                    }
                    continue;
                }
                final int point = key.indexOf('.', SPRING_BEANS_PREFIX.length());
                if (point < 0) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Not found key number of spring-beans target {}", key);
                    }
                    continue;
                }

                final int number = Integer.parseInt(key.substring(SPRING_BEANS_PREFIX.length(), point));
                SpringBeansTarget target = targets.get(number);
                if (target == null) {
                    target = new SpringBeansTarget();
                    targets.put(number, target);
                }

                if (key.endsWith(SPRING_BEANS_NAME_PATTERN_POSTFIX)) {
                    target.setNamePatterns(entry.getValue());
                } else if (key.endsWith(SPRING_BEANS_CLASS_PATTERN_POSTFIX)) {
                    target.setClassPatterns(entry.getValue());
                } else if (key.endsWith(SPRING_BEANS_ANNOTATION_POSTFIX)) {
                    target.setAnnotations(entry.getValue());
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Unknown key format of spring-beans target {}", key);
                    }
                    continue;
                }
            } catch (Exception e) {
                if (logger.isInfoEnabled()) {
                    logger.info("Invalid target of spring-beans target {}={}", entry.getKey(), entry.getValue());
                }
                continue;
            }
        }
    }

    public Collection<SpringBeansTarget> getTargets() {
        return targets.values();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("targets=").append(targets);
        sb.append('}');
        return sb.toString();
    }
}