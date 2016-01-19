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

import java.util.ArrayList;
import java.util.List;

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

    public static final String SPRING_BEANS_MAX = "profiler.spring.beans.max";
    public static final int DEFAULT_SPRING_BEANS_MAX = 100;

    private final List<SpringBeansTarget> targets = new ArrayList<SpringBeansTarget>();

    public SpringBeansConfig(ProfilerConfig config) {
        int max = config.readInt(SPRING_BEANS_MAX, DEFAULT_SPRING_BEANS_MAX);

        for(int i = 0; i < max; i++) {
            final SpringBeansTarget target = new SpringBeansTarget();
            if(i == 0) {
                // backward compatibility
                final String namePatternRegexs =  config.readString(SPRING_BEANS_NAME_PATTERN, null);
                final String classPatternRegexs = config.readString(SPRING_BEANS_CLASS_PATTERN, null);
                final String annotations = config.readString(SPRING_BEANS_ANNOTATION, null);
                target.setNamePatterns(namePatternRegexs);
                target.setClassPatterns(classPatternRegexs);
                target.setAnnotation(annotations);
            } else {
                final String namePatternRegexs = config.readString(SPRING_BEANS_PREFIX + i + SPRING_BEANS_NAME_PATTERN_POSTFIX, null);
                final String classPatternRegexs = config.readString(SPRING_BEANS_PREFIX + i + SPRING_BEANS_CLASS_PATTERN_POSTFIX, null);
                final String annotations = config.readString(SPRING_BEANS_PREFIX + i + SPRING_BEANS_ANNOTATION_POSTFIX, null);
                target.setNamePatterns(namePatternRegexs);
                target.setClassPatterns(classPatternRegexs);
                target.setAnnotation(annotations);
            }

            if (target.isValid()) {
                targets.add(target);
            }
        }
    }

    public List<SpringBeansTarget> getTargets() {
        return targets;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SpringBeansConfig{");
        sb.append("targets=").append(targets);
        sb.append('}');
        return sb.toString();
    }
}
