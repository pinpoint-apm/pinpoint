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
    public static final String SPRING_BEANS_PREFIX = "profiler.spring.beans.target.";
    public static final String SPRING_BEANS_ANNOTATION_POSTFIX = ".annotation";
    public static final String SPRING_BEANS_CLASS_PATTERN_POSTFIX = ".class.pattern";
    public static final String SPRING_BEANS_NAME_PATTERN_POSTFIX = ".name.pattern";

    private final List<SpringBeansTarget> targets = new ArrayList<SpringBeansTarget>();

    public SpringBeansConfig(ProfilerConfig config) {

        int index = 1;
        while (true) {
            final String namePatternRegex = config.readString(SPRING_BEANS_PREFIX + index + SPRING_BEANS_NAME_PATTERN_POSTFIX, null);
            final String classPatternRegex = config.readString(SPRING_BEANS_PREFIX + index + SPRING_BEANS_CLASS_PATTERN_POSTFIX, null);
            final String annotation = config.readString(SPRING_BEANS_PREFIX + index + SPRING_BEANS_ANNOTATION_POSTFIX, null);
            if (namePatternRegex == null && classPatternRegex == null && annotation == null) {
                break;
            }

            final SpringBeansTarget target = new SpringBeansTarget();
            target.setNamePattern(namePatternRegex);
            target.setClassPattern(classPatternRegex);
            target.setAnnotation(annotation);
            if (target.isValid()) {
                targets.add(target);
            }
            index++;
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
